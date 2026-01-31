#!/usr/bin/env sh

DIR="$(cd "$(dirname "$0")" && pwd)"
APP_HOME="$DIR"

GRADLE_WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

if [ ! -f "$GRADLE_WRAPPER_JAR" ]; then
  echo "Gradle wrapper jar not found!"
  exit 1
fi

JAVA_CMD="java"

exec "$JAVA_CMD" -classpath "$GRADLE_WRAPPER_JAR" org.gradle.wrapper.GradleWrapperMain "$@"
