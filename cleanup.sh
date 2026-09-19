#!/bin/bash

set -e

export MONGO_DB_ROOT_USERNAME=""
export MONGO_DB_ROOT_PASSWORD=""
export POSTGRES_USER=""
export POSTGRES_PASSWORD=""
export KEY_PASSWORD=""
export KEYSTORE_PASSWORD=""

cleanup() {
    unset MONGO_DB_ROOT_USERNAME
    unset MONGO_DB_ROOT_PASSWORD
    unset POSTGRES_USER
    unset POSTGRES_PASSWORD
    unset KEY_PASSWORD
    unset KEYSTORE_PASSWORD
}

trap cleanup EXIT

docker compose down --remove-orphans -v