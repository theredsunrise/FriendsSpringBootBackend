#!/bin/bash

set -euo pipefail

export MONGO_DB_ROOT_USERNAME="db_user"
export MONGO_DB_ROOT_PASSWORD="db_secure_password"
export POSTGRES_USER="db_user"
export POSTGRES_PASSWORD="db_secure_password"
export KEY_PASSWORD="changeit"
export KEYSTORE_PASSWORD="changeit"

cleanup() {
    unset MONGO_DB_ROOT_USERNAME
    unset MONGO_DB_ROOT_PASSWORD
    unset POSTGRES_USER
    unset POSTGRES_PASSWORD
    unset KEY_PASSWORD
    unset KEYSTORE_PASSWORD
}

trap cleanup EXIT

RENDER_KEYS="false"
DEBUG="false"

for arg in "$@"; do
  [[ "$arg" == RENDER_KEYS=* ]] && RENDER_KEYS="${arg#RENDER_KEYS=}"
  [[ "$arg" == DEBUG=* ]] && DEBUG="${arg#DEBUG=}"
done

COMPOSE_ARGS="-f docker-compose.yml"

if [ "$DEBUG" = "true" ]; then
  echo "Running in DEBUG mode. Merging docker-compose.debug.yml..."
  COMPOSE_ARGS="$COMPOSE_ARGS -f docker-compose.debug.yml"
else
  echo "Running in STANDARD mode."
fi

./mvnw clean package -DskipTests
if [ "$RENDER_KEYS" = "true" ]; then
  ./create_credentials.sh "$KEYSTORE_PASSWORD"
fi

docker compose $COMPOSE_ARGS up --build --remove-orphans -d