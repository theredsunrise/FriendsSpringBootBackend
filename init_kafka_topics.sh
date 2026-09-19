#!/bin/sh

set -eu

KAFKA_BOOTSTRAP_SERVERS="kafka:9092"
KAFKA_CONFIG="/etc/ssl/kafka-healthcheck/client.properties"

for topic in \
  "$USER_EVENTS_TOPIC" \
  "$FRIENDSHIP_EVENTS_TOPIC" \
  "$USER_EVENTS_RESPONSE_TOPIC" \
  "$FRIENDSHIP_EVENTS_RESPONSE_TOPIC"
do
  echo "Ensuring Kafka topic has ${KAFKA_LISTENER_CONCURRENCY} partitions: ${topic}"

  /opt/kafka/bin/kafka-topics.sh \
    --bootstrap-server "$KAFKA_BOOTSTRAP_SERVERS" \
    --command-config "$KAFKA_CONFIG" \
    --create \
    --if-not-exists \
    --topic "$topic" \
    --partitions ${KAFKA_LISTENER_CONCURRENCY} \
    --replication-factor 1

  PARTITIONS=$(
    /opt/kafka/bin/kafka-topics.sh \
      --bootstrap-server "$KAFKA_BOOTSTRAP_SERVERS" \
      --command-config "$KAFKA_CONFIG" \
      --describe \
      --topic "$topic" \
      | awk -F 'PartitionCount: ' '/Topic:/ { split($2, values, " "); print values[1]; exit }'
  )
  echo "Number of partitions detected: ${PARTITIONS}"

  if [ "$PARTITIONS" -lt ${KAFKA_LISTENER_CONCURRENCY} ]; then
    echo "Updating number of partitions to ${KAFKA_LISTENER_CONCURRENCY}"
    /opt/kafka/bin/kafka-topics.sh \
      --bootstrap-server "$KAFKA_BOOTSTRAP_SERVERS" \
      --command-config "$KAFKA_CONFIG" \
      --alter \
      --topic "$topic" \
      --partitions ${KAFKA_LISTENER_CONCURRENCY}
  fi
done

echo "Kafka application topics are ready."
