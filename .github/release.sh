#!/usr/bin/env bash
set -euo pipefail

compute_version_code() {
	local version="$1"
	local major="${version%%.*}"
	local rest="${version#*.}"
	local minor="${rest%%.*}"
	local commits=$(( $(git rev-list --count HEAD) + 1 ))
	echo $(( major * 100000 + minor * 10000 + commits ))
}

# Allow sourcing without running the release flow
[[ "${1:-}" == "--source-only" ]] && return 0 2>/dev/null || true
if [[ "${1:-}" == "--source-only" ]]; then exit 0; fi

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

PREV_TAG=$(git describe --tags --abbrev=0 2>/dev/null || echo "none")
VERSION_CODE=$(compute_version_code "$VERSION")

PRE_SUFFIX="${BASH_REMATCH[4]}"
PRE_RELEASE_TYPE=""
if [[ -n "$PRE_SUFFIX" ]] && [[ "$PRE_SUFFIX" =~ ^-?(alpha|beta|rc|dev) ]]; then
	PRE_RELEASE_TYPE="${BASH_REMATCH[1]}"
fi

if [[ -n "$PRE_RELEASE_TYPE" ]]; then
	echo "Releasing ${PREV_TAG} → ${TAG} (versionCode=$VERSION_CODE, pre-release=$PRE_RELEASE_TYPE)"
else
	echo "Releasing ${PREV_TAG} → ${TAG} (versionCode=$VERSION_CODE)"
fi
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
