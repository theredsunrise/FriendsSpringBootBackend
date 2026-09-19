#!/bin/bash

set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd -- "$SCRIPT_DIR/.." && pwd)"
cd "$PROJECT_ROOT"

RENDER_KEYS="false"
DEBUG="false"
SECRETS="false"

for arg in "$@"; do
  [[ "$arg" == RENDER_KEYS=* ]] && RENDER_KEYS="${arg#RENDER_KEYS=}"
  [[ "$arg" == DEBUG=* ]] && DEBUG="${arg#DEBUG=}"
  [[ "$arg" == SECRETS=* ]] && SECRETS="${arg#SECRETS=}"
done

COMPOSE_ARGS="-f docker-compose.yml"

if [ "$DEBUG" = "true" ]; then
  echo "Running in DEBUG mode. Merging docker-compose.debug.yml..."
  COMPOSE_ARGS="$COMPOSE_ARGS -f docker-compose.debug.yml"
else
  echo "Running in STANDARD mode."
fi

./mvnw clean package -DskipTests

if [ "$SECRETS" = "true" ]; then
  "$SCRIPT_DIR/create_secrets.sh"
fi
if [ "$RENDER_KEYS" = "true" ]; then
  "$SCRIPT_DIR/create_credentials.sh" "$(<.secrets/KEYSTORE_PASSWORD)"
fi

docker compose $COMPOSE_ARGS -p friends up --build --remove-orphans -d
