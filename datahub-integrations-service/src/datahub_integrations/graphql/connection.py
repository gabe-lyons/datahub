import json
from typing import Optional

from datahub.ingestion.graph.client import DataHubGraph
from loguru import logger


def get_connection(graph: DataHubGraph, urn: str) -> Optional[dict]:
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
            "urn": urn,
        },
    )

    if not res["connection"]:
        return None

    connection_type = res["connection"]["details"]["type"]
    if connection_type != "json":
        logger.error(
            f"Expected connection details type to be 'json', but got {connection_type}"
        )
        return None

    blob = res["connection"]["details"]["json"]["blob"]
    obj = json.loads(blob)

    return obj


def save_connection(graph: DataHubGraph, urn: str, blob: str) -> None:
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
            "id": urn,
            "blob": blob,
        },
    )

    assert res["upsertConnection"]["urn"] == urn
