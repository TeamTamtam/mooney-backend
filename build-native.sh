#!/usr/bin/env bash
set -e

# 1) 스크립트 기준 최상위 프로젝트 루트
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# 2) 모듈 디렉터리로 이동
cd "$ROOT_DIR/mooney"

JAR="build/libs/mooney-0.0.1-SNAPSHOT.jar"
NAME="Mooney"
VERSION="1.0.0"
BASE_OPTS=(
  --input build/libs
  --main-jar "$(basename "$JAR")"
  --name "$NAME"
  --app-version "$VERSION"
  --java-options "-Xmx512m"
)

# 3) OS 감지
OS="$(uname -s)"
case "$OS" in
  Darwin|Linux) TYPE="app-image" ;;
  MINGW*|MSYS*|CYGWIN*) TYPE="exe" ;;
  *) echo "Unsupported OS: $OS" >&2; exit 1 ;;
esac

echo "Building $NAME for $OS..."

jpackage \
  "${BASE_OPTS[@]}" \
  --type "$TYPE" \
  --dest "$ROOT_DIR"    # 생성 결과물을 스크립트가 있는 루트에 배치

echo "$NAME package created at $ROOT_DIR"
