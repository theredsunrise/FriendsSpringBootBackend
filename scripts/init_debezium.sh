#!/bin/sh

set -eu

DEBEZIUM_URL="http://debezium:8083"

printf "Registering/updating Debezium connectors...\n\n"

POSTGRES_PASSWORD_REF='${fileprovider:/run/secrets/DEBEZIUM_CREDENTIALS_PROPERTIES:POSTGRES_PASSWORD}'
KEYSTORE_PASSWORD_REF='${fileprovider:/run/secrets/DEBEZIUM_CREDENTIALS_PROPERTIES:KEYSTORE_PASSWORD}'

register_or_update_connector() {
  CONNECTOR_NAME="$1"
  CONFIG="$2"

  printf "Processing connector: %s\n" "$CONNECTOR_NAME"

  if curl -fsS "${DEBEZIUM_URL}/connectors/${CONNECTOR_NAME}" >/dev/null 2>&1; then
    printf "Connector exists -> updating configuration...\n"

    curl -fsS -X PUT \
      -H "Accept:application/json" \
      -H "Content-Type:application/json" \
      "${DEBEZIUM_URL}/connectors/${CONNECTOR_NAME}/config" \
      -d "$CONFIG"

    printf "\nConnector updated: %s\n\n" "$CONNECTOR_NAME"
  else
    printf "Connector does not exist -> creating...\n"

    curl -fsS -X POST \
      -H "Accept:application/json" \
      -H "Content-Type:application/json" \
      "${DEBEZIUM_URL}/connectors" \
      -d "{
        \"name\": \"${CONNECTOR_NAME}\",
        \"config\": ${CONFIG}
      }"

    printf "\nConnector created: %s\n\n" "$CONNECTOR_NAME"
  fi
}


USER_CONFIG='{
  "connector.class": "io.debezium.connector.postgresql.PostgresConnector",
  "tasks.max": "1",
  "plugin.name": "pgoutput",

  "database.hostname": "postgres-db",
  "database.port": "5432",
  "database.user": "'"$POSTGRES_USER"'",
  "database.password": "'"$POSTGRES_PASSWORD_REF"'",
  "database.dbname": "'"$POSTGRES_DB"'",
  "database.sslmode": "verify-full",
  "database.sslcert": "/etc/ssl/debezium/debezium.crt",
  "database.sslkey": "/etc/ssl/debezium/debezium.pkcs8.key",
  "database.sslrootcert": "/etc/ssl/ca/ca.crt",
  "database.sslpassword": "'"$KEYSTORE_PASSWORD_REF"'",

  "skipped.operations": "u",
  "topic.prefix": "cdc-users",
  "slot.name": "users_outbox_slot",
  "publication.name": "users_outbox_publication",
  "table.include.list": "public.users_outbox",

  "publication.autocreate.mode": "disabled",
  "heartbeat.interval.ms": "5000",
  "heartbeat.action.query": "INSERT INTO public.users_outbox_heartbeat (id, ts) VALUES(1, NOW()) ON CONFLICT (id) DO UPDATE SET ts = EXCLUDED.ts;",

  "poll.interval.ms": "5",
  "max.batch.size": "8192",
  "max.queue.size": "65536",

  "producer.batch.size": "65536",
  "producer.linger.ms": "0",
  "producer.buffer.memory": "268435456",
  "producer.max.in.flight.requests.per.connection": "5",

  "producer.acks": "all",
  "producer.enable.idempotence": "true",
  "producer.compression.type": "lz4",

  "transforms": "outbox",
  "transforms.outbox.type": "io.debezium.transforms.outbox.EventRouter",
  "transforms.outbox.route.by.field": "event_topic",
  "transforms.outbox.table.field.event.key": "event_id",
  "transforms.outbox.table.field.event.payload": "event_payload",
  "transforms.outbox.route.topic.replacement": "'"$USER_EVENTS_TOPIC"'",
  "transforms.outbox.table.field.event.id": "id",
  "transforms.outbox.table.fields.additional.placement": "event_type:header:eventType,tracingspancontext:header:traceparent",

  "tracing.span.context.field": "tracingspancontext",
  "tracing.operation.name": "debezium-read",
  "tracing.with.context.field.only": "true"
}'


FRIENDSHIP_CONFIG='{
  "connector.class": "io.debezium.connector.postgresql.PostgresConnector",
  "tasks.max": "1",
  "plugin.name": "pgoutput",

  "database.hostname": "postgres-db",
  "database.port": "5432",
  "database.user": "'"$POSTGRES_USER"'",
  "database.password": "'"$POSTGRES_PASSWORD_REF"'",
  "database.dbname": "'"$POSTGRES_DB"'",
  "database.sslmode": "verify-full",
  "database.sslcert": "/etc/ssl/debezium/debezium.crt",
  "database.sslkey": "/etc/ssl/debezium/debezium.pkcs8.key",
  "database.sslrootcert": "/etc/ssl/ca/ca.crt",
  "database.sslpassword": "'"$KEYSTORE_PASSWORD_REF"'",

  "skipped.operations": "u",
  "topic.prefix": "cdc-friendships",
  "slot.name": "friendships_outbox_slot",
  "publication.name": "friendships_outbox_publication",
  "table.include.list": "public.friendships_outbox",

  "publication.autocreate.mode": "disabled",
  "heartbeat.interval.ms": "5000",
  "heartbeat.action.query": "INSERT INTO public.friendships_outbox_heartbeat (id, ts) VALUES(1, NOW()) ON CONFLICT (id) DO UPDATE SET ts = EXCLUDED.ts;",

  "poll.interval.ms": "5",
  "max.batch.size": "8192",
  "max.queue.size": "65536",

  "producer.batch.size": "65536",
  "producer.linger.ms": "0",
  "producer.buffer.memory": "268435456",
  "producer.max.in.flight.requests.per.connection": "5",

  "producer.acks": "all",
  "producer.enable.idempotence": "true",
  "producer.compression.type": "lz4",

  "transforms": "outbox",
  "transforms.outbox.type": "io.debezium.transforms.outbox.EventRouter",
  "transforms.outbox.route.by.field": "event_topic",
  "transforms.outbox.table.field.event.key": "event_id",
  "transforms.outbox.table.field.event.payload": "event_payload",
  "transforms.outbox.route.topic.replacement": "'"$FRIENDSHIP_EVENTS_TOPIC"'",
  "transforms.outbox.table.field.event.id": "id",
  "transforms.outbox.table.fields.additional.placement": "event_type:header:eventType,tracingspancontext:header:traceparent",

  "tracing.span.context.field": "tracingspancontext",
  "tracing.operation.name": "debezium-read",
  "tracing.with.context.field.only": "true"
}'


register_or_update_connector \
  "postgres-user-outbox-connector" \
  "$USER_CONFIG"

register_or_update_connector \
  "postgres-friendship-outbox-connector" \
  "$FRIENDSHIP_CONFIG"

printf "All Debezium connectors processed successfully.\n"
