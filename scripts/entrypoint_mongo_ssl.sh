#!/bin/bash

set -euo pipefail

printf "Initializing MongoDB SSL configuration...\n"

chmod 600 /etc/ssl/mongo-db/security.keyfile
chmod 600 /etc/ssl/mongo-db/mongo-db.pem
chmod 600 /etc/ssl/mongo-healthcheck/mongo-healthcheck.pem
chmod 644 /etc/ssl/ca/ca.crt

chown 999:999 /etc/ssl/mongo-db/security.keyfile
chown 999:999 /etc/ssl/mongo-db/mongo-db.pem
chown 999:999 /etc/ssl/mongo-healthcheck/mongo-healthcheck.pem
chown 999:999 /etc/ssl/ca/ca.crt

exec docker-entrypoint.sh mongod "$@" \
  --bind_ip_all \
  --replSet rs0 \
  --keyFile /etc/ssl/mongo-db/security.keyfile \
  --tlsMode requireTLS \
  --tlsCertificateKeyFile /etc/ssl/mongo-db/mongo-db.pem \
  --tlsCAFile /etc/ssl/ca/ca.crt
