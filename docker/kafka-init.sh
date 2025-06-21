# This script is meant to be executed in container; NOT a host!

KAFKA_TOPIC_CLI=./opt/kafka/bin/kafka-topics.sh

max_attempts=5
attempt=1

until $KAFKA_TOPIC_CLI --bootstrap-server smq-broker:9092 --list; do
  if [ $attempt -ge $max_attempts ]; then
    echo "Kafka is not ready after $max_attempts attempts. Exiting."
    exit 1
  fi

  echo "Waiting for Kafka to be ready... (attempt $attempt/$max_attempts)"
  attempt=$((attempt+1))
  sleep 2
done

$KAFKA_TOPIC_CLI --create --if-not-exists --bootstrap-server smq-broker:9092 --replication-factor 1 --partitions 1 --topic $KAFKA_TOPIC_NAME