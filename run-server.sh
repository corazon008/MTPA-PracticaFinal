#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PORT="${1:-${SERVER_PORT:-5000}}"
DATA_DIR="${SERVER_DATA_DIR:-$ROOT/data}"
LOG_DIR="${SERVER_LOG_DIR:-$ROOT/target/logs}"
METRICS_FILE="${SERVER_METRICS_FILE:-$ROOT/target/metrics/server-metrics.txt}"
CONFIG_FILE="${SERVER_CONFIG:-$ROOT/server.properties}"

"$ROOT/init-server-data.sh"

MVN_ARGS=(
  -q
  -DskipTests
  "-Dexec.mainClass=org.example.server.ServerApplication"
  "-Dexec.args=$PORT"
  "-Dpersistence.dir=$DATA_DIR"
  "-Dserver.log.dir=$LOG_DIR"
  "-Dserver.metrics.file=$METRICS_FILE"
  "-Dserver.config=$CONFIG_FILE"
  exec:java
)

mvn "${MVN_ARGS[@]}"


