#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"

# Use Homebrew Java 21 if system java is missing
if ! java -version &>/dev/null 2>&1; then
  if [[ -x /opt/homebrew/opt/openjdk@21/bin/java ]]; then
    export JAVA_HOME=/opt/homebrew/opt/openjdk@21
    export PATH="$JAVA_HOME/bin:$PATH"
  else
    echo "Error: Java not found. Install JDK 21." >&2
    exit 1
  fi
fi

# gradlew eval-expands empty JAVA_OPTS/GRADLE_OPTS into bare empty-string args
# that Java misreads as the main class — prevent by exporting them as truly empty.
export JAVA_OPTS="${JAVA_OPTS-}"
export GRADLE_OPTS="${GRADLE_OPTS-}"
GRADLE="./gradlew"

echo "=== [1/5] Benchmark local (CPU-only) ==="
if ! command -v kotlin &>/dev/null; then
  echo "Warning: 'kotlin' not found — skipping benchmark. Install Kotlin SDK to enable."
else
  mkdir -p results
  kotlin benchmark/allowlist-benchmark.main.kts
fi

echo ""
echo "=== [2/5] Gerando dados de teste (20 × 500k PVs) ==="
python3 scripts/generate_test_data.py

echo ""
echo "=== [3/5] Build do JAR ==="
$GRADLE bootJar --no-daemon -q

echo ""
echo "=== [4/5] Docker Compose — build + k6 load test ==="
mkdir -p results
docker compose up --build --abort-on-container-exit --exit-code-from k6
docker compose down --remove-orphans

echo ""
echo "=== [5/5] Atualizando apresentação ==="
python3 scripts/update_presentation.py

echo ""
echo "Concluído! Abra presentation/index.html no navegador."
