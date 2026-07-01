#!/bin/zsh

# set version here
VERSION="26.2"

# mc decompiled sources are added as a submodule using command
# git submodule add --depth 1 -b "$VERSION" git@github-patch:patchnote-mc/mc_decompiled.git mc_decompiled/sources/$VERSION

# reloading
git submodule update --init --recursive --depth 1

cd "sources/$VERSION"

# check out the matching version branch
git fetch origin "$VERSION" --depth 1
git checkout "$VERSION"

cd ../..
