#!/bin/sh

set -eu

chmod 600 /etc/ssl/redis/redis.key
chmod 644 /etc/ssl/redis/redis.crt /etc/ssl/ca/ca.crt
chmod 600 /etc/ssl/redis-healthcheck/redis-healthcheck.key
chmod 644 /etc/ssl/redis-healthcheck/redis-healthcheck.crt

chown redis:redis /etc/ssl/redis/redis.key
chown redis:redis /etc/ssl/redis/redis.crt
chown redis:redis /etc/ssl/ca/ca.crt
chown redis:redis /etc/ssl/redis-healthcheck/redis-healthcheck.key
chown redis:redis /etc/ssl/redis-healthcheck/redis-healthcheck.crt

cat > /usr/local/bin/redis-cli-tls <<'EOF'
#!/bin/sh
exec /usr/local/bin/redis-cli \
  --tls \
  --cacert /etc/ssl/ca/ca.crt \
  --cert /etc/ssl/redis-healthcheck/redis-healthcheck.crt \
  --key /etc/ssl/redis-healthcheck/redis-healthcheck.key \
  "$@"
EOF
chmod +x /usr/local/bin/redis-cli-tls

chmod +x /usr/local/bin/redis-cli
exec docker-entrypoint.sh redis-server "$@" \
  --save "" \
  --appendonly no \
  --port 0 \
  --tls-port 6379 \
  --tls-cert-file /etc/ssl/redis/redis.crt \
  --tls-key-file /etc/ssl/redis/redis.key \
  --tls-ca-cert-file /etc/ssl/ca/ca.crt \
  --tls-auth-clients yes \
  --tls-protocols TLSv1.3
