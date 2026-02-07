#!/usr/bin/env bash
set -euo pipefail

VERSION_INPUT=${1:-}

if [[ -z "$VERSION_INPUT" ]]; then
	echo "Usage: .github/release.sh <version>"
	echo "Example: .github/release.sh 0.2.0"
	echo "Example: .github/release.sh v0.2.0"
	exit 1
fi

if [[ "$VERSION_INPUT" == vv* ]]; then
	echo "Error: invalid version '$VERSION_INPUT'"
	exit 1
fi

VERSION="${VERSION_INPUT#v}"

SEMVER_RE='^(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)(-[0-9A-Za-z-]+(\.[0-9A-Za-z-]+)*)?(\+[0-9A-Za-z-]+(\.[0-9A-Za-z-]+)*)?$'
if [[ ! "$VERSION" =~ $SEMVER_RE ]]; then
	echo "Error: invalid semver '$VERSION_INPUT'"
	echo "Example: .github/release.sh 1.2.3"
	exit 1
fi

MAJOR="${BASH_REMATCH[1]}"
MINOR="${BASH_REMATCH[2]}"
PATCH="${BASH_REMATCH[3]}"
TAG="v$VERSION"

if [[ -n "$(git status --porcelain)" ]]; then
	echo "Error: working tree not clean"
	exit 1
fi

BRANCH=$(git rev-parse --abbrev-ref HEAD)
if [[ "$BRANCH" != "main" ]]; then
	echo "Error: not on main branch (currently on $BRANCH)"
	exit 1
fi

git pull --ff-only

if ! command -v git-cliff &>/dev/null; then
	echo "Error: git-cliff not installed"
	exit 1
fi

if [[ ! -f ".github/cliff.toml" ]]; then
	echo "Error: .github/cliff.toml not found"
	exit 1
fi

# MAJOR * 100000 + MINOR * 10000 + PATCH * 100
# Leaves 100 build slots per patch for hotfix rebuilds
VERSION_CODE=$(( MAJOR * 100000 + MINOR * 10000 + PATCH * 100 ))

PREV_TAG=$(git describe --tags --abbrev=0 2>/dev/null || echo "none")

echo "Releasing ${PREV_TAG} → ${TAG} (versionCode=$VERSION_CODE)"
echo ""
git cliff --config .github/cliff.toml --tag "$TAG" --unreleased
echo ""

read -rp "Commit, tag, and push? [y/N] " confirm
if [[ ! "$confirm" =~ ^[Yy]$ ]]; then
	echo "Aborted"
	exit 1
fi

sed -i "s/^version\.code=.*/version.code=${VERSION_CODE}/" gradle.properties
sed -i "s/^version\.name=.*/version.name=${VERSION}/" gradle.properties

git add gradle.properties
git commit -m "chore: release $TAG"

git tag -a "$TAG" -m "$TAG"
git push origin main --follow-tags

echo ""
echo "Released $TAG"
