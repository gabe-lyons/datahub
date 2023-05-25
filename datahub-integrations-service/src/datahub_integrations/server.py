from fastapi import HTTPException, status
from pydantic import BaseModel

from datahub_integrations.app import app
from datahub_integrations.slack.slack import (
    SlackLinkPreview,
    external_router,
    get_slack_link_preview,
    internal_router,
    reload_slack_credentials,
)


@internal_router.post("/reload_credentials")
def reload_credentials() -> None:
    """Reloads all integration credentials from GMS."""

    reload_slack_credentials()


class GetLinkPreviewInput(BaseModel):
    type: str
    url: str


@internal_router.post("/get_link_preview", response_model=SlackLinkPreview)
def get_link_preview(input: GetLinkPreviewInput) -> SlackLinkPreview:
    """Get a link preview."""

    if input.type == "SLACK_MESSAGE":
        return get_slack_link_preview(input.url)
    else:
        raise HTTPException(status.HTTP_400_BAD_REQUEST, f"Unknown link type: {type}")


app.include_router(internal_router, prefix="/private")
app.include_router(external_router, prefix="/public")

# TODO: [temporary hack] mounting the external router twice so that it mirrors the
# route used by the frontend.
app.include_router(external_router, prefix="/integrations")
