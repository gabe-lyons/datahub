import contextlib
import functools
import re
from datetime import datetime, timezone
from typing import Optional, Tuple

import fastapi
import slack_bolt
import slack_sdk.errors
import slack_sdk.web
from datahub.utilities.urns.urn import Urn, guess_entity_type
from fastapi import HTTPException, status
from fastapi.responses import RedirectResponse
from loguru import logger
from pydantic import BaseModel
from slack_bolt.adapter.fastapi import SlackRequestHandler
from slack_sdk.oauth import AuthorizeUrlGenerator
from slack_sdk.oauth.state_store import FileOAuthStateStore

from datahub_integrations.app import DATAHUB_FRONTEND_URL, graph
from datahub_integrations.graphql.social_query import get_entity
from datahub_integrations.slack.app_manifest import (
    get_slack_app_manifest,
    slack_bot_scopes,
    upsert_app_with_manifest,
)
from datahub_integrations.slack.config import SlackConnection, slack_config

external_router = fastapi.APIRouter()
internal_router = fastapi.APIRouter(
    dependencies=[
        # TODO: Add middleware for requiring system auth here.
    ]
)

_state_store = FileOAuthStateStore(expiration_seconds=300, base_dir="./data-state")


def get_oauth_url_generator(config: SlackConnection) -> AuthorizeUrlGenerator:
    assert config.app_details

    return AuthorizeUrlGenerator(
        client_id=config.app_details.client_id,
        scopes=slack_bot_scopes,
        redirect_uri=f"{DATAHUB_FRONTEND_URL}/integrations/slack/oauth_callback",
    )


@internal_router.post("/slack/reload_credentials")
def reload_slack_credentials() -> None:
    """Reload Slack credentials from GMS and refreshes existing services appropriately."""

    slack_config.reload()


@external_router.get("/slack/install")
def install_slack_app() -> RedirectResponse:
    config = slack_config.reload()

    # Create / update the Slack app manifest before attempting to install.
    manifest = get_slack_app_manifest()
    config = upsert_app_with_manifest(config, manifest)
    slack_config.save_config(config)
    assert config.app_details, "App details should be present after provisioning."

    # Generate the OAuth URL and redirect to it.
    authorize_url_generator = get_oauth_url_generator(config)
    state = _state_store.issue()

    url = authorize_url_generator.generate(state=state)
    logger.debug(f"Redirecting to {url}")

    return RedirectResponse(url=url)


@external_router.get("/slack/oauth_callback")
def oauth_callback(
    state: str,
    code: Optional[str] = None,
    error: Optional[str] = None,
    error_description: Optional[str] = None,
) -> RedirectResponse:
    config = slack_config.get_config()
    assert config.app_details, "App details should be present after provisioning."

    if not _state_store.consume(state):
        raise fastapi.HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Invalid state parameter.",
        )

    if error:
        raise fastapi.HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Error: {error}. Description: {error_description}",
        )
    assert code

    # Logic based on https://slack.dev/python-slack-sdk/oauth/.
    slack_client = slack_sdk.web.WebClient()  # no token required

    authorize_url_generator = get_oauth_url_generator(config)
    oauth_response = slack_client.oauth_v2_access(
        client_id=config.app_details.client_id,
        client_secret=config.app_details.client_secret,
        redirect_uri=authorize_url_generator.redirect_uri,
        code=code,
    ).validate()

    authed_user = oauth_response["authed_user"]
    logger.info(
        f'Completed app install for team {oauth_response.get("team")}, approved by {authed_user}'
    )

    # Save the new bot token.
    bot_token = oauth_response["access_token"]
    new_config = config.copy(
        update=dict(
            bot_token=bot_token,
        )
    )
    slack_config.save_config(new_config)

    # Send a welcome message to the user who just installed us.
    # TODO: Add more context to this message + a link back to the integrations page.
    app = get_slack_app(new_config)
    app.client.chat_postMessage(
        channel=authed_user["id"], text="Acryl has been connected to Slack!"
    )

    return RedirectResponse(url="/settings/integrations/slack")


@functools.lru_cache(maxsize=1)
def get_slack_app(config: SlackConnection) -> slack_bolt.App:
    if not config.app_details:
        raise fastapi.HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="The Slack app manifest has not been provisioned yet.",
        )
    if config.bot_token is None:
        raise fastapi.HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="The Slack app has not been installed into a workspace yet.",
        )

    logger.info("Initializing Slack bolt sdk app.")

    # Initializes your app with your bot token and signing secret
    app = slack_bolt.App(
        token=config.bot_token,
        signing_secret=config.app_details.signing_secret,
    )

    # Listen for unfurl events
    @app.event("link_shared")
    def handle_link_shared(ack, body):
        ack()

        logger.info(f"Link unfurl request: {body}")
        event = body["event"]

        # TODO: unfurl multiple links

        # Get the link URL from the event body
        link_url: str = event["links"][0]["url"]

        # https://<frontend_url>/<entity_type>/<urn>[/asdf]?<suffix_with_slashes>
        urn = link_url.split("/")[4]
        logger.debug(f"URN: {urn}")

        # Call the Slack API method to unfurl the link.
        # See https://api.slack.com/docs/message-link-unfurling#link_unfurling_with_api
        response = app.client.chat_unfurl(
            channel=event["channel"],
            ts=event["message_ts"],
            unfurls={link_url: make_slack_preview(urn)},  # type: ignore
            # user_auth_url='...',
        )

        # Log the API call response
        print(response)

    @app.message("hello")
    def message_hello(message, say):
        logger.info(message)
        say(f'Hello <@{message["user"]}>!')

    @app.event("message")
    def handle_message_events(body):
        # logger.info(f"message handler: {body}")
        pass

    @app.event("app_mention")
    def handle_app_mention_events(body):
        logger.info(body)

    return app


@external_router.post("/slack/events")
async def slack_event_endpoint(req: fastapi.Request) -> fastapi.Response:
    # Attach the slack event handler.
    app = get_slack_app(slack_config.get_config())
    app_handler = SlackRequestHandler(app)
    return await app_handler.handle(req)


def datahub_url_from_urn(urn: str, suffix: Optional[str] = None) -> str:
    # TODO: copied from the dbt action
    entity_type = guess_entity_type(urn)
    if entity_type == "dataJob":
        entity_type = "tasks"
    elif entity_type == "dataFlow":
        entity_type = "pipelines"

    url = f"{DATAHUB_FRONTEND_URL}/{entity_type}/{Urn.url_encode(urn)}"
    if suffix:
        url += f"/{suffix}"
    return url


def make_slack_preview(urn: str) -> Optional[dict]:
    entity = get_entity(graph, urn)
    logger.debug(f"entity: {entity}")
    if not entity:
        return None

    # Example entity:
    # {'glossaryTerms': None,
    #  'ownership': None,
    #  'platform': {'properties': {'displayName': 'BigQuery',
    #                              'logoUrl': '/assets/platforms/bigquerylogo.png'}},
    #  'properties': {'description': None, 'name': 'lineage_from_base'},
    #  'siblings': None,
    #  'subTypes': {'typeNames': ['Table']},
    #  'type': 'DATASET',
    #  'urn': 'urn:li:dataset:(urn:li:dataPlatform:bigquery,acryl-staging.smoke_test_db.lineage_from_base,PROD)'}

    # Generate a rich slack preview.
    # See here for docs on syntax: https://app.slack.com/block-kit-builder.

    platform_name = entity["platform"]["properties"]["displayName"]
    platform_icon = entity["platform"]["properties"]["logoUrl"]
    if platform_icon.startswith("/"):
        platform_icon = f"{DATAHUB_FRONTEND_URL}{platform_icon}"

    subtype = entity["type"]
    with contextlib.suppress(KeyError, TypeError):
        subtype = entity["subTypes"]["typeNames"][0]

    # Set up the unfurling payload.
    blocks = [
        # Entity name, type, and logo.
        {
            "type": "header",
            "text": {
                "type": "plain_text",
                "text": f"{entity['properties']['name']}",
                "emoji": True,
            },
        },
        {
            "type": "context",
            "elements": [
                {
                    "type": "image",
                    "image_url": platform_icon,
                    "alt_text": "",
                },
                {"type": "mrkdwn", "text": f"{platform_name} {subtype}"},
            ],
        },
        {"type": "divider"},
    ]

    # Description section.
    description = None
    with contextlib.suppress(TypeError):
        description = entity["properties"]["description"]
    with contextlib.suppress(TypeError):
        description = entity["editableProperties"]["description"]
    if description:
        blocks.append(
            {
                "type": "section",
                "text": {
                    "type": "mrkdwn",
                    "text": f"{description}",
                },
            }
        )

    facts = []

    # Domain section.
    with contextlib.suppress(TypeError):
        domain = entity["domain"]["domain"]["properties"]["name"]
        if domain:
            facts.append(
                {
                    "type": "mrkdwn",
                    "text": f"*Domain*: {domain}",
                }
            )

    # Owners section.
    with contextlib.suppress(TypeError):
        # TODO: Replace these with mentions? We'd need to be careful not to spam people though.
        owners = entity["ownership"]["owners"]
        facts.append(
            {
                "type": "mrkdwn",
                "text": f"*Owners*: {', '.join(owner['owner']['properties']['displayName'] for owner in owners)}",
            }
        )

    # Terms section.
    with contextlib.suppress(TypeError):
        terms = entity["glossaryTerms"]["terms"]
        facts.append(
            {
                "type": "mrkdwn",
                "text": f"*Terms*: {', '.join(term['term']['properties']['name'] for term in terms)}",
            }
        )

    if facts:
        blocks.append(
            {
                "type": "section",
                "fields": facts,
            }
        )

    return {
        "blocks": blocks,
        # Optional: We can customize the message composer preview too.
        # "preview": {
        #     "title": {"type": "plain_text", "text": "custom preview"},
        #     "icon_url": "...",
        # },
    }


def parse_slack_message_url(url: str) -> Optional[Tuple[str, str, Optional[str]]]:
    # Parse the url using regex.
    # https://regex101.com/r/QxS5d3/2

    regex = r"^https://([a-zA-Z0-9_\-]+)\.slack\.com/archives/([CD]\w+)/p(\d+)(?:\?.*thread_ts=([\d.]+).*)?$"

    matches = re.fullmatch(regex, url)
    if not matches:
        return None

    _workspace_name, conversation_id, message_id, thread_ts = matches.groups()

    return conversation_id, message_id, thread_ts


class SlackLinkPreview(BaseModel):
    url: str

    timestamp: int  # unix timestamp in milliseconds
    text: str

    authorName: str
    authorImageUrl: Optional[str]

    channelName: str

    # Only present if the message is in a thread.
    # These fields refer to the entire thread.
    isPartOfThread: bool = False
    replyCount: Optional[int]
    threadBaseMessageText: Optional[str]


def get_slack_link_preview(url: str) -> SlackLinkPreview:
    app = get_slack_app(slack_config.get_config())

    parsed_url = parse_slack_message_url(url)
    if not parsed_url:
        raise HTTPException(status.HTTP_400_BAD_REQUEST, "Invalid slack message url")

    converation_id, message_id, thread_ts = parsed_url

    try:
        # First, get the conversation (channel/DM/MPIM) info.
        # TODO: Add caching around this.
        conversation = app.client.conversations_info(channel=converation_id).validate()
        conversation_name = conversation["channel"]["name"]

        # Next, get the message info.
        slack_message_ts: str = message_id[:-6] + "." + message_id[-6:]
        oldest = None
        if thread_ts:
            slack_message_ts, oldest = thread_ts, slack_message_ts

        messages = app.client.conversations_replies(
            channel=converation_id,
            ts=slack_message_ts,
            oldest=oldest,
            limit=1,
            inclusive=True,
            include_all_metadata=True,
        ).validate()["messages"]

        if thread_ts and len(messages) == 2:
            thread_base_message, message = messages
        else:
            thread_base_message = None
            message = messages[0]

        # Get more details about the message author.
        # TODO: Add caching around this.
        user_id = message["user"]
        user_details = app.client.users_info(user=user_id).validate()["user"]
        author_name = (
            user_details["profile"]["display_name_normalized"]
            or user_details["profile"]["display_name"]
            or user_details["profile"]["real_name_normalized"]
            or user_details["profile"]["real_name"]
        )
        author_image_url = user_details["profile"]["image_72"]

        preview = SlackLinkPreview(
            url=url,
            timestamp=int(
                datetime.fromtimestamp(
                    float(message["ts"]), tz=timezone.utc
                ).timestamp()
                * 1000
            ),
            text=message["text"],
            authorName=author_name,
            authorImageUrl=author_image_url,
            channelName=conversation_name,
        )
        if thread_base_message:
            preview.isPartOfThread = True
            preview.replyCount = thread_base_message["reply_count"]
            preview.threadBaseMessageText = thread_base_message["text"]

        return preview
    except slack_sdk.errors.SlackApiError as e:
        raise HTTPException(
            status.HTTP_400_BAD_REQUEST,
            f"You do not have permission to view conversation {converation_id} message {message_id}: {e.response['error']}",
        ) from e
