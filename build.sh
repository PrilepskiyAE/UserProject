#!/bin/bash

set -Eeuo pipefail

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_DIR"
COMPOSE_FILE="docker-compose.yml"

echo "...........................Build..........................."
mvn clean package -DskipTests
docker compose -f "$COMPOSE_FILE" config >/dev/null
docker compose -f "$COMPOSE_FILE" build
docker compose -f "$COMPOSE_FILE" down --remove-orphans
docker compose -f "$COMPOSE_FILE" up -d

echo "...........................Start..........................."
sleep 10

docker compose -f "$COMPOSE_FILE" ps

echo "...........................Sycces..........................."
