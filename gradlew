#!/bin/sh
set -eu

APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
GRADLE_VERSION=8.10.2
GRADLE_HOME="${HOME}/.gradle/wrapper/dists/bfb-bootstrap-${GRADLE_VERSION}"
GRADLE_DIR="${GRADLE_HOME}/gradle-${GRADLE_VERSION}"
GRADLE_ZIP="${TMPDIR:-/tmp}/gradle-${GRADLE_VERSION}-bin.zip"

if [ ! -x "${GRADLE_DIR}/bin/gradle" ]; then
    mkdir -p "${GRADLE_HOME}"
    if command -v curl >/dev/null 2>&1; then
        curl -fL "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" -o "${GRADLE_ZIP}"
    elif command -v wget >/dev/null 2>&1; then
        wget -O "${GRADLE_ZIP}" "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip"
    else
        echo "ERROR: curl or wget is required to bootstrap Gradle ${GRADLE_VERSION}." >&2
        exit 1
    fi

    rm -rf "${GRADLE_DIR}"
    unzip -q "${GRADLE_ZIP}" -d "${GRADLE_HOME}"
    rm -f "${GRADLE_ZIP}"
fi

exec "${GRADLE_DIR}/bin/gradle" "$@"
