import json
from dataclasses import dataclass
from datetime import datetime, timedelta, timezone
from typing import Optional

import pydantic
from datahub.configuration.common import ConnectionModel
from loguru import logger

from datahub_integrations.app import graph

_SLACK_CONFIG_ID = "__system_slack-0"
_SLACK_CONFIG_URN = f"urn:li:dataHubConnection:{_SLACK_CONFIG_ID}"


class _FrozenConnectionModel(ConnectionModel, frozen=True):
    pass


class SlackAppConfigCredentials(_FrozenConnectionModel):
    """Used for creating and updating the App Manifest"""

    access_token: str
    refresh_token: str

    # Default expiry is 12 hours from now.
    exp: datetime = pydantic.Field(
        default_factory=lambda: datetime.now(tz=timezone.utc) + timedelta(hours=12)
    )

    def is_expired(self):
        return datetime.now(tz=timezone.utc) > self.exp


class SlackAppDetails(_FrozenConnectionModel):
    app_id: str
    client_id: str
    client_secret: str
    signing_secret: str
    verification_token: str


class SlackConnection(_FrozenConnectionModel):
    app_config_tokens: Optional[SlackAppConfigCredentials] = None

    app_details: Optional[SlackAppDetails] = None

    bot_token: Optional[str] = None

    # TODO: Maybe add a needs_reinstall flag here?
    # TODO: Add workspace_id here?


def _get_current_slack_config() -> SlackConnection:
    """Gets the current slack config from DataHub."""

    res = graph.execute_graphql(
        query="""
query GetSlackConnection($urn: String!) {
  connection(urn: $urn) {
    urn
    details {
      type
      json {
        blob
      }
    }
  }
}
""".strip(),
        variables={
            "urn": _SLACK_CONFIG_URN,
        },
    )

    if not res["connection"]:
        logger.debug("No slack config found, returning an empty config")
        return SlackConnection()

    blob = res["connection"]["details"]["json"]["blob"]
    config = SlackConnection.parse_obj(json.loads(blob))

    return config


def _set_current_slack_config(config: SlackConnection) -> None:
    """Sets the current slack config in DataHub."""

    blob = config.json()

    res = graph.execute_graphql(
        query="""
mutation SetSlackConnection($id: String!, $blob: String!) {
  upsertConnection(
    input: {
      id: $id,
      type: JSON,
      platformUrn: "urn:li:dataPlatform:slack",
      json: {blob: $blob}
    }
  ) {
    urn
  }
}
""".strip(),
        variables={
            "id": _SLACK_CONFIG_ID,
            "blob": blob,
        },
    )

    assert res["upsertConnection"]["urn"] == _SLACK_CONFIG_URN


@dataclass
class _SlackConfigManager:
    """A caching wrapper around the Slack config."""

    _config: Optional[SlackConnection] = None

    def get_config(self, force_refresh: bool = False) -> SlackConnection:
        if self._config is None or force_refresh:
            logger.info("Getting slack config")
            self._config = _get_current_slack_config()

        return self._config

    def reload(self) -> SlackConnection:
        logger.info("Reloading slack config")
        self._config = _get_current_slack_config()
        return self._config

    def save_config(self, config: SlackConnection) -> None:
        logger.info("Setting slack config")
        _set_current_slack_config(config)
        self._config = config


slack_config = _SlackConfigManager()
