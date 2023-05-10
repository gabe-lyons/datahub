from typing import Optional

from datahub.ingestion.graph.client import DataHubGraph
from typing_extensions import TypedDict

GRAPHQL_ENTITIES_SOCIAL_DETAILS_FRAGMENT = """
fragment PlatformInfo on DataPlatform {
  properties {
    displayName
    logoUrl
  }
}

fragment TermsInfo on GlossaryTerms {
  terms {
    term {
      properties {
        name
      }
    }
  }
}

fragment OwnershipInfo on Ownership {
  owners {
    owner {
      ... on CorpUser {
        properties {
          displayName
        }
      }
      ... on CorpGroup {
        properties {
          displayName
        }
      }
    }
  }
}

fragment DomainInfo on DomainAssociation {
  domain {
    properties {
      name
    }
  }
}

fragment EntitySocialDetails on Entity {
  urn
  type
  ... on Dataset {
    properties {
      name
      description
    }
    editableProperties {
      description
    }
    platform {
      ...PlatformInfo
    }
    subTypes {
      typeNames
    }
    glossaryTerms {
      ...TermsInfo
    }
    ownership {
      ...OwnershipInfo
    }
    domain {
      ...DomainInfo
    }
  }
  ... on Chart {
    properties {
      name
      description
    }
    editableProperties {
      description
    }
    platform {
      ...PlatformInfo
    }
    glossaryTerms {
      ...TermsInfo
    }
    ownership {
      ...OwnershipInfo
    }
    domain {
      ...DomainInfo
    }
  }
  ... on Dashboard {
    properties {
      name
      description
    }
    editableProperties {
      description
    }
    platform {
      ...PlatformInfo
    }
    glossaryTerms {
      ...TermsInfo
    }
    ownership {
      ...OwnershipInfo
    }
    domain {
      ...DomainInfo
    }
  }
  ... on Tag {
    properties {
      name
      description
    }
    ownership {
      ...OwnershipInfo
    }
  }
}
"""


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
