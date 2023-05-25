import pathlib
from typing import Optional

from datahub.ingestion.graph.client import DataHubGraph
from typing_extensions import TypedDict

GRAPHQL_ENTITIES_SOCIAL_DETAILS_FRAGMENT = (
    pathlib.Path(__file__).parent / "social_entity_details.gql"
).read_text()


class EntitySocialDetails(TypedDict, total=False):
    urn: str
    type: str
    properties: dict
    editableProperties: dict
    platform: dict
    subTypes: dict
    glossaryTerms: dict
    ownership: dict
    domain: dict


def get_entity(graph: DataHubGraph, urn: str) -> Optional[EntitySocialDetails]:
    info = graph.execute_graphql(
        f"{GRAPHQL_ENTITIES_SOCIAL_DETAILS_FRAGMENT}"
        """
query entitiesSocial($urns: [String!]!) {
  entities(urns: $urns) {
    ...EntitySocialDetails
  }
}
""",
        variables={"urns": [urn]},
    )

    if not info["entities"]:
        return None

    return info["entities"][0]
