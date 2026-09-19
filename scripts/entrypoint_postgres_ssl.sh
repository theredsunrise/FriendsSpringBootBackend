#!/bin/bash

set -euo pipefail

chown postgres:postgres /var/lib/postgresql/ca /var/lib/postgresql/certs

chmod 600 "/var/lib/postgresql/certs/postgres-db.key"
chmod 644 "/var/lib/postgresql/certs/postgres-db.crt" \
"/var/lib/postgresql/ca/ca.crt" \
"/var/lib/postgresql/certs/pg_hba.conf" \
"/var/lib/postgresql/certs/pg_ident.conf"

exec docker-entrypoint.sh postgres "$@" \
  -c ssl=on \
  -c ssl_ca_file="/var/lib/postgresql/ca/ca.crt" \
  -c ssl_cert_file="/var/lib/postgresql/certs/postgres-db.crt" \
  -c ssl_key_file="/var/lib/postgresql/certs/postgres-db.key" \
  -c hba_file="/var/lib/postgresql/certs/pg_hba.conf" \
  -c ident_file="/var/lib/postgresql/certs/pg_ident.conf" \
  -c logging_collector=off \
  -c wal_level=logical
