#!/bin/bash

COMPOSE_FILE="./docker/docker-compose.yml"

case "$1" in
  start)
    echo "Building and copying server.jar..."
    ./gradlew dockerMount
    echo "Starting Docker Compose services..."
    docker compose -f "$COMPOSE_FILE" up -d
    if [ $? -ne 0 ]; then
        sleep 1s
        ./smq-server.sh stop
    fi
    ;;
  stop)
    echo "Stopping Docker Compose services..."
    docker compose -f "$COMPOSE_FILE" down
    docker system prune -f
    ;;
  clean)
    echo "Cleaning Gradle build files..."
    ./gradlew clean
    ./gradlew --stop
    ;;
  *)
    echo "Usage: $0 {start|stop|clean}"
    exit 1
    ;;
esac
