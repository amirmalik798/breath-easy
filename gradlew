#!/usr/bin/env sh
# Lightweight Gradle bootstrapper for environments where the wrapper JAR is not present.
# It downloads the pinned Gradle distribution once and then delegates all arguments to it.
set -eu

GRADLE_VERSION="9.6.0"
GRADLE_HOME_DIR="${GRADLE_USER_HOME:-${HOME}/.gradle}/breathe-easy/gradle-${GRADLE_VERSION}"
GRADLE_BIN="${GRADLE_HOME_DIR}/bin/gradle"

if [ ! -x "${GRADLE_BIN}" ]; then
    mkdir -p "$(dirname "${GRADLE_HOME_DIR}")"
    TMP_DIR="$(mktemp -d "${TMPDIR:-/tmp}/breathe-easy-gradle.XXXXXX")"
    cleanup() { rm -rf "${TMP_DIR}"; }
    trap cleanup EXIT HUP INT TERM
    ARCHIVE="${TMP_DIR}/gradle-${GRADLE_VERSION}-bin.zip"
    URL="https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip"

    if command -v curl >/dev/null 2>&1; then
        curl --fail --location --retry 3 --connect-timeout 20 "${URL}" -o "${ARCHIVE}"
    elif command -v wget >/dev/null 2>&1; then
        wget --tries=3 --timeout=20 "${URL}" -O "${ARCHIVE}"
    else
        echo "BreatheEasy needs curl or wget to download Gradle ${GRADLE_VERSION}." >&2
        exit 1
    fi

    if command -v unzip >/dev/null 2>&1; then
        unzip -q "${ARCHIVE}" -d "${TMP_DIR}"
    else
        echo "BreatheEasy needs unzip to unpack Gradle ${GRADLE_VERSION}." >&2
        exit 1
    fi

    mkdir -p "$(dirname "${GRADLE_HOME_DIR}")"
    if [ ! -x "${GRADLE_BIN}" ]; then
        rm -rf "${GRADLE_HOME_DIR}"
        mv "${TMP_DIR}/gradle-${GRADLE_VERSION}" "${GRADLE_HOME_DIR}"
    fi
fi

exec "${GRADLE_BIN}" "$@"
