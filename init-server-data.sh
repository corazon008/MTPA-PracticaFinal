#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DATA_DIR="${SERVER_DATA_DIR:-$ROOT/data}"
LOG_DIR="${SERVER_LOG_DIR:-$ROOT/target/logs}"
METRICS_DIR="${SERVER_METRICS_DIR:-$ROOT/target/metrics}"

mkdir -p "$DATA_DIR/messages" "$LOG_DIR" "$METRICS_DIR"

echo "Initialized server directories:"
echo "  data:     $DATA_DIR"
echo "  logs:     $LOG_DIR"
echo "  metrics:  $METRICS_DIR"

