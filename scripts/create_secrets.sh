#!/bin/bash

set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd -- "$SCRIPT_DIR/.." && pwd)"
SECRETS_DIR="${SECRETS_DIR:-$PROJECT_ROOT/.secrets}"
mkdir -p "$SECRETS_DIR"
chmod 700 "$SECRETS_DIR"

write_secret() {
  local name="$1"
  local value="$2"

  printf '%s' "$value" > "$SECRETS_DIR/$name"
}

#*********** CHANGE ************
write_secret POSTGRES_PASSWORD 'db_secure_password'
write_secret MONGO_DB_ROOT_PASSWORD 'db_secure_password'
write_secret KEY_PASSWORD 'secure_password'
write_secret KEYSTORE_PASSWORD 'secure_password'
#*******************************

KAFKA_CREDENTIALS="$SECRETS_DIR/kafka-credentials.properties"
{
  printf 'KEY_PASSWORD=%s\n' "$(<"$SECRETS_DIR/KEY_PASSWORD")"
  printf 'KEYSTORE_PASSWORD=%s\n' "$(<"$SECRETS_DIR/KEYSTORE_PASSWORD")"
} > "$KAFKA_CREDENTIALS"

DEBEZIUM_CREDENTIALS="$SECRETS_DIR/debezium-credentials.properties"
{
  printf 'POSTGRES_PASSWORD=%s\n' "$(<"$SECRETS_DIR/POSTGRES_PASSWORD")"
  printf 'KEYSTORE_PASSWORD=%s\n' "$(<"$SECRETS_DIR/KEYSTORE_PASSWORD")"
} > "$DEBEZIUM_CREDENTIALS"

printf 'Secrets created in %s\n' "$SECRETS_DIR"
