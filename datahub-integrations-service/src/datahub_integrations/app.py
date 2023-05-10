import os

from datahub.ingestion.graph.client import DatahubClientConfig, DataHubGraph
from fastapi import FastAPI, Response
from fastapi.responses import RedirectResponse

app = FastAPI()


@app.get("/ping")
def ping() -> str:
    return "pong"


@app.get("/")
def redirect_to_docs() -> Response:
    return RedirectResponse(url="/docs")


# A global config and graph object that can be used by all routers.
DATAHUB_SERVER = f"{os.environ.get('DATAHUB_GMS_PROTOCOL', 'http')}://{os.environ['DATAHUB_GMS_HOST']}:{os.environ['DATAHUB_GMS_PORT']}"
graph = DataHubGraph(
    DatahubClientConfig(
        server=DATAHUB_SERVER,
        # When token is not set, the client will automatically try to use
        # DATAHUB_SYSTEM_CLIENT_ID and DATAHUB_SYSTEM_CLIENT_SECRET to authenticate.
        token=None,
    )
)
DATAHUB_FRONTEND_URL = graph.frontend_base_url
