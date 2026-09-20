#!/bin/bash

set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd -- "$SCRIPT_DIR/.." && pwd)"
cd "$PROJECT_ROOT"

SERVICE=$1
DEBUG="false"

for arg in "${@:2}"; do
  [[ "$arg" == DEBUG=* ]] && DEBUG="${arg#DEBUG=}"
done

if [ -z "$SERVICE" ]; then
    echo "Usage: $0 <service>"
    exit 1
fi

COMPOSE_ARGS="-f docker-compose.yml"

if [ "$DEBUG" = "true" ]; then
  echo "Running in DEBUG mode. Merging docker-compose.debug.yml..."
  COMPOSE_ARGS="$COMPOSE_ARGS -f docker-compose.debug.yml"
  SCALE_ARGS="--scale user-service-sync=1"
else
  echo "Running in STANDARD mode."
  SCALE_ARGS=""
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
docker compose $COMPOSE_ARGS -p friends up $SCALE_ARGS --build --no-deps -d "$SERVICE"
