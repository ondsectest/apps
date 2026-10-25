#!/usr/bin/env bash
# Zip both extensions into dist/ for a GitHub release (or a Chrome Web Store upload).
# The theme lives at the repo root (manifest.json + icons/); the New Tab page lives in new-tab/.
# Usage: ./scripts/package.sh
# Built by Santhosh Tuppad.
set -euo pipefail

cd "$(dirname "$0")/.."
version=$(python3 -c 'import json; print(json.load(open("manifest.json"))["version"])')

rm -rf dist && mkdir dist
zip -qr -X "dist/testers-corner-theme-v${version}.zip" manifest.json icons -x '*/.*'
(cd new-tab && zip -qr -X "../dist/testers-corner-new-tab-v${version}.zip" . -x '.*')

ls -1 dist
