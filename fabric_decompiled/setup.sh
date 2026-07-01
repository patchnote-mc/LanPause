#!/bin/zsh
set -e

# fabric source is added as a submodule using command
# git submodule add --depth 1 https://github.com/FabricMC/fabric-api.git fabric_decompiled/src

URL="https://github.com/FabricMC/fabric-api.git"
TAG="0.153.0+26.2"

# operate from the repo root
cd "${0:A:h}/.."

if git submodule status fabric_decompiled/src >/dev/null 2>&1; then
  git submodule update --init --recursive --depth 1 fabric_decompiled/src
else
  rm -rf fabric_decompiled/src
  git submodule add --force --depth 1 "$URL" fabric_decompiled/src
fi

# Check out the matching fabric-api tag.
cd fabric_decompiled/src
git fetch --depth 1 origin tag "$TAG" --no-tags
git -c advice.detachedHead=false checkout "$TAG"
