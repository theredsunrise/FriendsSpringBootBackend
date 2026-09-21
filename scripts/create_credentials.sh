#!/bin/bash

set -euo pipefail

PASSWORD=${1:-}
POSTGRES_USER=${2:-}

if [ -z "$PASSWORD" ] || [ -z "$POSTGRES_USER" ]; then
    echo "Usage: $0 <password> <POSTGRES_USER>"
    exit 1
fi

VALIDITY_DAYS=3650
SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd -- "$SCRIPT_DIR/.." && pwd)"
BASE_DIR="$PROJECT_ROOT/.certs"

generate_cert() {
    local NAME="$1"
    local CN="$2"
    local EKU="$3"
    local SAN="$4"
    local DIR="$NAME"

    echo
    echo "Generating certificate: $NAME"

    openssl genrsa \
      -out "$DIR/$NAME.key" \
      3072

    cat > "$DIR/$NAME.cnf" <<EOF
subjectAltName=$SAN
extendedKeyUsage=$EKU
keyUsage=digitalSignature,keyEncipherment
subjectKeyIdentifier=hash
authorityKeyIdentifier=keyid,issuer
EOF

    openssl req \
      -new \
      -sha256 \
      -key "$DIR/$NAME.key" \
      -out "$DIR/$NAME.csr" \
      -subj "/CN=$CN/O=Friends"

    openssl x509 \
      -req \
      -sha256 \
      -in "$DIR/$NAME.csr" \
      -CA "ca/ca.crt" \
      -CAkey "ca/ca.key" \
      -CAcreateserial \
      -out "$DIR/$NAME.crt" \
      -days "$VALIDITY_DAYS" \
      -extfile "$DIR/$NAME.cnf"

    openssl pkcs12 -export \
      -out "$DIR/$NAME.p12" \
      -inkey "$DIR/$NAME.key" \
      -in "$DIR/$NAME.crt" \
      -certfile "ca/ca.crt" \
      -name "$NAME" \
      -passout "pass:$PASSWORD"

    keytool -importcert \
      -noprompt \
      -alias friends-ca \
      -file "ca/ca.crt" \
      -storetype PKCS12 \
      -keystore "$DIR/$NAME-truststore.p12" \
      -storepass "$PASSWORD"

    openssl pkcs8 -topk8 \
    -inform PEM \
    -outform DER \
    -in "$DIR/$NAME.key" \
    -out "$DIR/$NAME.pkcs8.key" \
    -nocrypt

    echo "  ✓ $DIR/$NAME.key"
    echo "  ✓ $DIR/$NAME.crt"
    echo "  ✓ $DIR/$NAME.p12"
    echo "  ✓ $DIR/$NAME-truststore.p12"
}

command -v openssl >/dev/null 2>&1 || {
    echo "ERROR: openssl is not installed"
    exit 1
}

command -v keytool >/dev/null 2>&1 || {
    echo "ERROR: keytool is not installed"
    exit 1
}

rm -rf "${BASE_DIR:?BASE_DIR is empty or unset}"/*
mkdir -p "$BASE_DIR"/{ca,\
kafka,\
kafka-healthcheck,\
redis,\
redis-healthcheck,\
user-service,\
user-service-sync,\
debezium,\
postgres-db,\
mongo-db,\
mongo-healthcheck,\
gateway}

pushd "$BASE_DIR" > /dev/null

echo
echo "Generating Root CA..."

openssl genrsa \
  -out ca/ca.key \
  4096

cat > ca/ca.cnf <<EOF
[v3_ca]
basicConstraints=critical,CA:TRUE
keyUsage=critical,keyCertSign,cRLSign
subjectKeyIdentifier=hash
authorityKeyIdentifier=keyid:always,issuer
EOF

openssl req \
  -x509 \
  -new \
  -sha256 \
  -key ca/ca.key \
  -out ca/ca.crt \
  -days "$VALIDITY_DAYS" \
  -subj "/CN=Friends Dev CA/O=Friends" \
  -extensions v3_ca \
  -config ca/ca.cnf

openssl pkcs12 -export \
  -out ca/ca.p12 \
  -inkey ca/ca.key \
  -in ca/ca.crt \
  -name friends-ca \
  -passout "pass:$PASSWORD"

keytool -importcert \
  -noprompt \
  -alias friends-ca \
  -file ca/ca.crt \
  -storetype PKCS12 \
  -keystore ca/ca-truststore.p12 \
  -storepass "$PASSWORD"

echo
echo "Generating Redis certificate..."

generate_cert \
  "redis" \
  "redis" \
  "serverAuth" \
  "DNS:redis,DNS:localhost,IP:127.0.0.1"

echo
echo "Generating Redis healthcheck certificate..."

generate_cert \
  "redis-healthcheck" \
  "redis-healthcheck" \
  "clientAuth" \
  "DNS:redis-healthcheck"

echo
echo "Generating Gateway certificate..."
generate_cert \
  "gateway" \
  "gateway" \
  "clientAuth,serverAuth" \
  "DNS:friends,DNS:localhost,IP:127.0.0.1"

echo
echo "Generating Kafka certificate..."

generate_cert \
  "kafka" \
  "kafka" \
  "clientAuth,serverAuth" \
  "DNS:kafka,DNS:localhost,IP:127.0.0.1"

echo
echo "Generating Kafka healthcheck certificate..."

generate_cert \
  "kafka-healthcheck" \
  "kafka-healthcheck" \
  "clientAuth" \
  "DNS:kafka-healthcheck"

cat > "kafka-healthcheck/client.properties" <<EOF
security.protocol=SSL

ssl.keystore.type=PKCS12
ssl.keystore.location=/etc/ssl/kafka-healthcheck/kafka-healthcheck.p12
ssl.keystore.password=$PASSWORD
ssl.key.password=$PASSWORD

ssl.truststore.type=PKCS12
ssl.truststore.location=/etc/ssl/kafka-healthcheck/kafka-healthcheck-truststore.p12
ssl.truststore.password=$PASSWORD

ssl.protocol=TLSv1.3
ssl.enabled.protocols=TLSv1.3

ssl.endpoint.identification.algorithm=HTTPS
EOF

echo
echo "Generating user-service certificate..."

generate_cert \
  "user-service" \
  "user-service" \
  "clientAuth" \
  "DNS:user-service,DNS:localhost,IP:127.0.0.1"

echo
echo "Generating user-service-sync certificate..."

generate_cert \
  "user-service-sync" \
  "user-service-sync" \
  "clientAuth" \
  "DNS:user-service-sync,DNS:localhost,IP:127.0.0.1"

echo
echo "Generating Debezium certificate..."

generate_cert \
  "debezium" \
  "debezium" \
  "clientAuth" \
  "DNS:debezium,DNS:localhost,IP:127.0.0.1"

echo
echo "Generating Postgresql certificate... "

generate_cert \
  "postgres-db" \
  "postgres-db" \
  "serverAuth" \
  "DNS:postgres-db,DNS:localhost,IP:127.0.0.1"

cat > "postgres-db/pg_hba.conf" <<EOF
local   all             all                             trust
host    all             all             127.0.0.1/32    trust
hostssl all             all             0.0.0.0/0       cert clientcert=verify-full map=friends_ssl_map
hostssl all             all             ::/0            cert clientcert=verify-full map=friends_ssl_map
EOF

cat > "postgres-db/pg_ident.conf" <<EOF
friends_ssl_map    debezium                            $POSTGRES_USER
friends_ssl_map    user-service                        $POSTGRES_USER
friends_ssl_map    kafka                               $POSTGRES_USER
friends_ssl_map    kafka-healthcheck                   $POSTGRES_USER
EOF

echo
echo "Generating MongoDB certificate..."

generate_cert \
  "mongo-db" \
  "mongo-db" \
  "serverAuth" \
  "DNS:mongo-db,DNS:localhost,IP:127.0.0.1"

cat "mongo-db/mongo-db.crt" "mongo-db/mongo-db.key" > "mongo-db/mongo-db.pem"

echo "Generating MongoDB replica-set keyfile..."

openssl rand -base64 512 > "mongo-db/security.keyfile"

echo
echo "Generating MongoDB healthcheck certificate..."

generate_cert \
  "mongo-healthcheck" \
  "mongo-healthcheck" \
  "clientAuth" \
  "DNS:mongo-healthcheck,DNS:localhost,IP:127.0.0.1"

cat "mongo-healthcheck/mongo-healthcheck.crt" "mongo-healthcheck/mongo-healthcheck.key" > \
"mongo-healthcheck/mongo-healthcheck.pem"

popd > /dev/null

echo
echo "Setting directory permissions..."

find "$BASE_DIR" -type d -exec chmod 700 {} \;

echo "Cleaning up temporary files..."

find "$BASE_DIR" -type f \( \
    -name "*.cnf" \
    -o -name "*.csr" \
    -o -name "*.srl" \
\) -delete

echo
echo "Certificate generation completed successfully."
echo
echo "Generated files:"
find "$BASE_DIR" -type f | sort
