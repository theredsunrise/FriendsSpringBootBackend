#!/bin/bash

set -euo pipefail

printf "Checking and initializing MongoDB replica set..."

timeout 5 </dev/tcp/mongo-db/27017 2>/dev/null || true

ARG="mongodb://mongo-db:27017/?tls=true&\
tlsCAFile=/etc/ssl/ca/ca.crt&\
tlsCertificateKeyFile=/etc/ssl/mongo-healthcheck/mongo-healthcheck.pem&\
tlsAllowInvalidHostnames=false"

mongosh "$ARG" \
  --authenticationDatabase admin \
  -u "${MONGO_DB_ROOT_USERNAME}" \
  -p "${MONGO_DB_ROOT_PASSWORD}" \
  <<EOF
try {
  const status = rs.status();
  if (status.ok === 1) {
    print("Replica set already initialized.");
  }
} catch (e) {
  print("Replica set is not initialized. Initializing...");
  const result = rs.initiate({
    _id: "${MONGO_REPLICA_NAME}",
    members: [
      { _id: 0, host: "mongo-db:27017" }
    ]
  });
  printjson(result);
}
EOF

printf "MongoDB initialization check complete."
