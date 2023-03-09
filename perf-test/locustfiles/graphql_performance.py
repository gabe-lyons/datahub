from locust import HttpUser, constant, task
from threading import Lock, Thread
from random import randint

from test_utils.datahub_sessions import DataHubSessions
from test_utils.graphql_queries import GraphQLQueries

lock = Lock()


datahub_instances = DataHubSessions()
graphql_queries = GraphQLQueries()
test_inputs = [
    {
        "query_name": "searchAcrossEntities",
        "inputs": [
            # Disabled Cache
            {"test_name": "*", "query": "*"},
            {"test_name": "customer", "query": "customer"},
            {"test_name": "orders", "query": "orders"},
            {"test_name": "log events", "query": "log events"},
            {"test_name": "account history", "query": "account history"},

            {"test_name": "* (cntrl)", "query": "*"},
            {"test_name": "customer (cntrl)", "query": "customer"},
            {"test_name": "orders (cntrl)", "query": "orders"},
            {"test_name": "log events (cntrl)", "query": "log events"},
            {"test_name": "account history (cntrl)", "query": "account history"},
            # Disabled Cache Single Entity
            # {"test_name": "* DATASET", "query": "*", "types": ["DATASET"]},
            # {"test_name": "customer DATASET", "query": "customer", "types": ["DATASET"]},
            # {"test_name": "orders DATASET", "query": "orders", "types": ["DATASET"]},
            # {"test_name": "log events DATASET", "query": "log events", "types": ["DATASET"]},
            # {"test_name": "account history DATASET", "query": "account history", "types": ["DATASET"]},

            # Enabled Cache
            {"test_name": "* (cache)", "query": "*", "searchFlags": {"skipCache": False, "fulltext": True}},
            {"test_name": "customer (cache)", "query": "customer", "searchFlags": {"skipCache": False, "fulltext": True}},
            {"test_name": "orders (cache)", "query": "orders", "searchFlags": {"skipCache": False, "fulltext": True}},
            {"test_name": "log events (cache)", "query": "log events", "searchFlags": {"skipCache": False, "fulltext": True}},
            {"test_name": "account history (cache)", "query": "account history", "searchFlags": {"skipCache": False, "fulltext": True}},
            # Enabled Cache Single Entity
            # {"test_name": "* DATASET (cache)", "query": "*", "types": ["DATASET"], "searchFlags": {"skipCache": False, "fulltext": True}},
            # {"test_name": "customer DATASET (cache)", "query": "customer", "types": ["DATASET"], "searchFlags": {"skipCache": False, "fulltext": True}},
            # {"test_name": "orders DATASET (cache)", "query": "orders", "types": ["DATASET"], "searchFlags": {"skipCache": False, "fulltext": True}},
            # {"test_name": "log events DATASET (cache)", "query": "log events", "types": ["DATASET"], "searchFlags": {"skipCache": False, "fulltext": True}},
            # {"test_name": "account history DATASET (cache)", "query": "account history", "types": ["DATASET"], "searchFlags": {"skipCache": False, "fulltext": True}},
        ]
    }
]


class SearchUser(HttpUser):
    wait_time = constant(1)

    @task
    def search(self):
        test_run = test_inputs[0]
        session = datahub_instances.get_session(self.host)
        gql_query = graphql_queries.get_query(test_run["query_name"])

        inputs = test_run["inputs"]
        rand_input = inputs[randint(0, len(inputs)-1)]
        test_name = rand_input["test_name"]
        post_json = gql_query.get_query_with_inputs(rand_input)

        response = self.client.request(
            method="POST",
            name=f"{session.get_short_host()}[{test_name}]",
            url=f"{self.host}/api/graphql",
            cookies=session.get_cookies(),
            json=post_json
        )
        response.raise_for_status()
