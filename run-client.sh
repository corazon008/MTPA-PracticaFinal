#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SERVER_HOST="${SERVER_HOST:-127.0.0.1}"
PORT="${1:-${SERVER_PORT:-5000}}"

MVN_ARGS=(
  -q
  -DskipTests
  "-Dexec.mainClass=org.example.client.ClientApplication"
  "-Dexec.args=$PORT"
  exec:java
)

mvn "${MVN_ARGS[@]}"

