#!/usr/bin/env bash
# One-time: generate gradle/wrapper/gradle-wrapper.jar.
# The jar is a ~60 KB binary that could not be committed from the authoring
# environment (no network). Run this once on a machine with Gradle installed.
set -euo pipefail
if [ -f gradle/wrapper/gradle-wrapper.jar ]; then
  echo "gradle-wrapper.jar already present — nothing to do."
  exit 0
fi
if ! command -v gradle >/dev/null 2>&1; then
  echo "System 'gradle' not found. Install Gradle (brew install gradle) or open"
  echo "the project in Android Studio, which regenerates the wrapper automatically."
  exit 1
fi
gradle wrapper --gradle-version 8.9 --distribution-type bin
echo "Wrapper generated. You can now run ./gradlew assembleDebug"
