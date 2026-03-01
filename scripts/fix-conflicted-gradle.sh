#!/usr/bin/env bash
set -euo pipefail

FILES=("build.gradle.kts" "androidApp/build.gradle.kts")

for f in "${FILES[@]}"; do
  if [[ ! -f "$f" ]]; then
    continue
  fi

  tmp="${f}.tmp.$$"
  awk '
    /^(<<<<<<<|=======|>>>>>>>)/ { next }
    /^codex\// { next }
    /^main$/ { next }
    { print }
  ' "$f" > "$tmp"

  mv "$tmp" "$f"
  echo "Sanitized $f"
done

echo "Done. Now run: ./gradlew --stop && ./gradlew help"
