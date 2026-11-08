#!/bin/sh
set -eu
cd "$(dirname "$0")"
if [ -n "${JAVA_HOME:-}" ] && [ -x "$JAVA_HOME/bin/java" ]; then
  JAVA_BIN="$JAVA_HOME/bin/java"
elif [ -x /opt/homebrew/opt/openjdk/bin/java ]; then
  JAVA_BIN=/opt/homebrew/opt/openjdk/bin/java
else
  JAVA_BIN=java
fi
if [ ! -f target/nusa-campus.jar ]; then
  echo "Build terlebih dahulu: mvn clean package (JDK 11 atau lebih baru)."
  exit 1
fi
exec "$JAVA_BIN" -jar target/nusa-campus.jar "$@"
