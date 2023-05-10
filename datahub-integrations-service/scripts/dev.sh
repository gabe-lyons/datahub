#!/bin/bash

source .env

poetry run uvicorn datahub_integrations.server:app --reload --port 9003
