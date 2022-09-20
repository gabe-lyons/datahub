#!/bin/bash

set -e

: ${DATAHUB_ANALYTICS_ENABLED:=true}
: ${USE_AWS_ELASTICSEARCH:=false}

if [[ $ELASTICSEARCH_USE_SSL == true ]]; then
  ELASTICSEARCH_PROTOCOL=https
else
  ELASTICSEARCH_PROTOCOL=http
fi
echo -e "Going to use $ELASTICSEARCH_PROTOCOL"


if [[ -z $ELASTICSEARCH_MASTER_USERNAME ]]; then
  echo -e "Variable ELASTICSEARCH_MASTER_USERNAME is not set. Going to use value of ELASTICSEARCH_USERNAME"
  ELASTICSEARCH_MASTER_USERNAME=$ELASTICSEARCH_USERNAME
fi
if [[ -z $ELASTICSEARCH_MASTER_PASSWORD ]]; then
  echo -e "Variable ELASTICSEARCH_MASTER_PASSWORD is not set. Going to use value of ELASTICSEARCH_PASSWORD"
  ELASTICSEARCH_MASTER_PASSWORD=$ELASTICSEARCH_PASSWORD
fi

if [[ ! -z $ELASTICSEARCH_MASTER_USERNAME ]] && [[ -z $ELASTICSEARCH_AUTH_HEADER ]]; then
  AUTH_TOKEN=$(echo -ne "$ELASTICSEARCH_MASTER_USERNAME:$ELASTICSEARCH_MASTER_PASSWORD" | base64 --wrap 0)
  ELASTICSEARCH_AUTH_HEADER="Authorization:Basic $AUTH_TOKEN"
fi

# Add default header if needed
if [[ -z $ELASTICSEARCH_AUTH_HEADER ]]; then
  echo -e "Going to use default elastic headers"
  ELASTICSEARCH_AUTH_HEADER="Accept: */*"
fi

function create_datahub_usage_event_datastream() {
  echo -e "Going to use prefix $INDEX_PREFIX"
  if [[ -z "$INDEX_PREFIX" ]]; then
    PREFIX=''
  else
    PREFIX="${INDEX_PREFIX}_"
  fi

  POLICY_RESPONSE_CODE=$(curl -o /dev/null -s -w "%{http_code}" --header "$ELASTICSEARCH_AUTH_HEADER" "$ELASTICSEARCH_PROTOCOL://$ELASTICSEARCH_HOST:$ELASTICSEARCH_PORT/_ilm/policy/${PREFIX}datahub_usage_event_policy")
  echo -e "Policy GET response code is $POLICY_RESPONSE_CODE"
  if [ $POLICY_RESPONSE_CODE -eq 403 ]
  then
    echo -e "Forbidden so exiting"
    exit 1
  fi
  POLICY_NAME="${PREFIX}datahub_usage_event_policy"
  if [ $POLICY_RESPONSE_CODE -eq 404 ]
  then
    echo -e "\ncreating $POLICY_NAME"
    sed -e "s/PREFIX/${PREFIX}/g" /index/usage-event/policy.json | tee -a /tmp/policy.json
    curl -XPUT --header "$ELASTICSEARCH_AUTH_HEADER" "$ELASTICSEARCH_PROTOCOL://$ELASTICSEARCH_HOST:$ELASTICSEARCH_PORT/_ilm/policy/${POLICY_NAME}" -H 'Content-Type: application/json' --data @/tmp/policy.json
  else
    echo -e "\n${POLICY_NAME} exists"
  fi
  TEMPLATE_RESPONSE_CODE=$(curl -o /dev/null -s -w "%{http_code}" --header "$ELASTICSEARCH_AUTH_HEADER" "$ELASTICSEARCH_PROTOCOL://$ELASTICSEARCH_HOST:$ELASTICSEARCH_PORT/_index_template/${PREFIX}datahub_usage_event_index_template")
  echo -e "Template GET response code is $TEMPLATE_RESPONSE_CODE"
  if [ $TEMPLATE_RESPONSE_CODE -eq 403 ]
  then
    echo -e "Forbidden so exiting"
    exit 1
  fi
  TEMPLATE_NAME="${PREFIX}datahub_usage_event_index_template"
  if [ $TEMPLATE_RESPONSE_CODE -eq 404 ]
  then
    echo -e "\ncreating $TEMPLATE_NAME"
    sed -e "s/PREFIX/${PREFIX}/g" /index/usage-event/index_template.json | tee -a /tmp/index_template.json
    curl -XPUT --header "$ELASTICSEARCH_AUTH_HEADER" "$ELASTICSEARCH_PROTOCOL://$ELASTICSEARCH_HOST:$ELASTICSEARCH_PORT/_index_template/$TEMPLATE_NAME" -H 'Content-Type: application/json' --data @/tmp/index_template.json
  else
    echo -e "\n$TEMPLATE_NAME exists"
  fi
}

function create_datahub_usage_event_aws_elasticsearch() {
  if [[ -z "$INDEX_PREFIX" ]]; then
    PREFIX=''
  else
    PREFIX="${INDEX_PREFIX}_"
  fi


  if [ $(curl -o /dev/null -s -w "%{http_code}" --header "$ELASTICSEARCH_AUTH_HEADER" "$ELASTICSEARCH_PROTOCOL://$ELASTICSEARCH_HOST:$ELASTICSEARCH_PORT/_opendistro/_ism/policies/${PREFIX}datahub_usage_event_policy") -eq 404 ]
  then
    echo -e "\ncreating datahub_usage_event_policy"
    sed -e "s/PREFIX/${PREFIX}/g" /index/usage-event/aws_es_ism_policy.json | tee -a /tmp/aws_es_ism_policy.json
    curl -XPUT --header "$ELASTICSEARCH_AUTH_HEADER" "$ELASTICSEARCH_PROTOCOL://$ELASTICSEARCH_HOST:$ELASTICSEARCH_PORT/_opendistro/_ism/policies/${PREFIX}datahub_usage_event_policy" -H 'Content-Type: application/json' --data @/tmp/aws_es_ism_policy.json
  else
    echo -e "\ndatahub_usage_event_policy exists"
  fi
  if [ $(curl -o /dev/null -s -w "%{http_code}" --header "$ELASTICSEARCH_AUTH_HEADER" "$ELASTICSEARCH_PROTOCOL://$ELASTICSEARCH_HOST:$ELASTICSEARCH_PORT/_template/${PREFIX}datahub_usage_event_index_template") -eq 404 ]
  then
    echo -e "\ncreating datahub_usage_event_index_template"
    sed -e "s/PREFIX/${PREFIX}/g" /index/usage-event/aws_es_index_template.json | tee -a /tmp/aws_es_index_template.json
    curl -XPUT --header "$ELASTICSEARCH_AUTH_HEADER" "$ELASTICSEARCH_PROTOCOL://$ELASTICSEARCH_HOST:$ELASTICSEARCH_PORT/_template/${PREFIX}datahub_usage_event_index_template" -H 'Content-Type: application/json' --data @/tmp/aws_es_index_template.json
    curl -XPUT --header "$ELASTICSEARCH_AUTH_HEADER" "$ELASTICSEARCH_PROTOCOL://$ELASTICSEARCH_HOST:$ELASTICSEARCH_PORT/${PREFIX}datahub_usage_event-000001"  -H 'Content-Type: application/json' --data "{\"aliases\":{\"${PREFIX}datahub_usage_event\":{\"is_write_index\":true}}}"
  else
    echo -e "\index exists"
  fi
}

function create_access_policy_data_es_cloud {
  cat <<EOF
{
   "cluster":[ "monitor" ],
   "indices":[
      {
         "names":["${INDEX_PREFIX}_*"],
         "privileges":["all"]
      }
   ]
}
EOF
}

function create_user_data_es_cloud {
    cat <<EOF
{
   "password": "${ELASTICSEARCH_PASSWORD}",
	  "roles":["${INDEX_PREFIX}_access"]
}
EOF
}

function create_user_es_cloud {
  # Tested with Elastic 7.17
  ROLE="${INDEX_PREFIX}_access"
  echo -e '\ncreating role' "${ROLE}"
  curl -XPOST --header "$ELASTICSEARCH_AUTH_HEADER" "$ELASTICSEARCH_PROTOCOL://$ELASTICSEARCH_HOST:$ELASTICSEARCH_PORT/_security/role/${ROLE}" -H 'Content-Type: application/json' --data "$(create_access_policy_data_es_cloud)"
  echo -e '\ncreating user' "${ELASTICSEARCH_USERNAME}"
  curl -XPOST --header "$ELASTICSEARCH_AUTH_HEADER" "$ELASTICSEARCH_PROTOCOL://$ELASTICSEARCH_HOST:$ELASTICSEARCH_PORT/_security/user/${ELASTICSEARCH_USERNAME}" -H 'Content-Type: application/json' --data "$(create_user_data_es_cloud)"
}


function create_user() {
  ROLE="${INDEX_PREFIX}_access"
  echo -e '\ncreating role' "${ROLE}"
  curl -XPUT --header "$ELASTICSEARCH_AUTH_HEADER" "$ELASTICSEARCH_PROTOCOL://$ELASTICSEARCH_HOST:$ELASTICSEARCH_PORT/_opendistro/_security/api/roles/${ROLE}" -H 'Content-Type: application/json' \
    -d "{\"cluster_permissions\":[\"indices:*\", \"cluster:monitor/tasks/lists\"], \"index_permissions\":[{\"index_patterns\":[\"${INDEX_PREFIX}_*\"], \"allowed_actions\":[\"indices_all\"]}]}"
  echo -e '\ncreating user' "${ELASTICSEARCH_USERNAME}"
  curl -XPUT --header "$ELASTICSEARCH_AUTH_HEADER" "$ELASTICSEARCH_PROTOCOL://$ELASTICSEARCH_HOST:$ELASTICSEARCH_PORT/_opendistro/_security/api/internalusers/${ELASTICSEARCH_USERNAME}" -H 'Content-Type: application/json' \
    -d "{\"password\":\"${ELASTICSEARCH_PASSWORD}\", \"opendistro_security_roles\":[\"${ROLE}\"]}"
}

if [[ $CREATE_USER == true ]]; then
  if [[ $USE_AWS_ELASTICSEARCH == true ]]; then
    create_user || exit 1
  else
    create_user_es_cloud || exit 1
  fi
fi

if [[ $DATAHUB_ANALYTICS_ENABLED == true ]]; then
  if [[ $USE_AWS_ELASTICSEARCH == false ]]; then
    create_datahub_usage_event_datastream || exit 1
  else
    create_datahub_usage_event_aws_elasticsearch || exit 1
  fi
fi
