#!/bin/zsh
set -e

# set version here
VERSION="26.2"

# mc decompiled sources are added as a submodule using command
# git submodule add --depth 1 -b "$VERSION" git@github-patch:patchnote-mc/mc_decompiled.git mc_decompiled/sources/$VERSION

URL="git@github-patch:patchnote-mc/mc_decompiled.git"
SUB="mc_decompiled/sources/$VERSION"

# operate from the repo root
cd "${0:A:h}/.."

if git submodule status "$SUB" >/dev/null 2>&1; then
  git submodule update --init --recursive --depth 1 "$SUB"
else
  rm -rf "$SUB"
  git submodule add --force --depth 1 -b "$VERSION" "$URL" "$SUB"
fi

# Check out the matching version branch.
cd "$SUB"
git fetch --depth 1 origin "$VERSION"
git -c advice.detachedHead=false checkout "$VERSION"
