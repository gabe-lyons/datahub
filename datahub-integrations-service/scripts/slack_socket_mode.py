from datahub_integrations.slack.config import slack_config
from datahub_integrations.slack.slack import get_slack_app

if __name__ == "__main__":
    # For development - using the slack websocket API.
    import os

    from slack_bolt.adapter.socket_mode import SocketModeHandler

    APP_LEVEL_TOKEN = os.environ.get("APP_LEVEL_TOKEN")
    config = slack_config.get_config()
    app = get_slack_app(config)
    SocketModeHandler(app, APP_LEVEL_TOKEN).start()
