#!/bin/bash

PROD_COMPOSE_FILE="./docker/prod.docker-compose.yml"
DEV_COMPOSE_FILE="./docker/dev.docker-compose.yml"

case "$1" in
  prod)
    COMPOSE_FILE=$PROD_COMPOSE_FILE
    ;;
  dev)
    COMPOSE_FILE=$DEV_COMPOSE_FILE
    ;;
  clean)
    echo "Cleaning Gradle build files..."
    ./gradlew clean
    ./gradlew --stop
    exit 0
    ;;
  *)
    echo "Usage: $0 {prod|dev} {start|stop} OR $0 clean"
    exit 1
    ;;
esac

case "$2" in
  start)
    echo "Building and copying server.jar..."
    ./gradlew dockerMount
    echo "Starting Docker Compose services..."
    docker compose -f "$COMPOSE_FILE" up -d
    if [ $? -ne 0 ]; then
        sleep 1s
        $0 $1 stop
    fi
    ;;
  stop)
    echo "Stopping Docker Compose services..."
    docker compose -f "$COMPOSE_FILE" down
    docker system prune -f
    ;;
  *)
    echo "Usage: $0 {prod|dev} {start|stop} OR $0 clean"
    exit 1
    ;;
esac
