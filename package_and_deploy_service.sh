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

SERVICE=$1
DEBUG="${2:-}"
DEBUG="${2#DEBUG=}"

if [ -z "$SERVICE" ]; then
    echo "Usage: $0 <service>"
    exit 1
fi

COMPOSE_ARGS="-f docker-compose.yml"

if [ "$DEBUG" = "true" ]; then
  echo "Running in DEBUG mode. Merging docker-compose.debug.yml..."
  COMPOSE_ARGS="$COMPOSE_ARGS -f docker-compose.debug.yml"
else
  echo "Running in STANDARD mode."
fi

if ! SERVICES=$(docker compose ${COMPOSE_ARGS} config --services); then
    echo "ERROR: docker compose config failed"
    exit 1
fi

if ! printf '%s\n' "$SERVICES" | grep -Fqx -- "$SERVICE"; then
    echo "ERROR: Service '$SERVICE' not found"
    echo "Available services:"
    printf '%s\n' "$SERVICES"
    exit 1
fi

./mvnw clean package -DskipTests
docker compose $COMPOSE_ARGS up --build --no-deps -d "$SERVICE"