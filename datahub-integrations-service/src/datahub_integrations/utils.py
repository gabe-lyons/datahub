from typing import Optional

from datahub.utilities.urns.urn import Urn, guess_entity_type

from datahub_integrations.app import DATAHUB_FRONTEND_URL


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
