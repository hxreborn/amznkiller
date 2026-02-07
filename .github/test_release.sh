#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "$SCRIPT_DIR/release.sh" --source-only

PASS=0
FAIL=0

assert_eq() {
	local label="$1" expected="$2" actual="$3"
	if [[ "$expected" == "$actual" ]]; then
		echo "  PASS: $label"
		PASS=$(( PASS + 1 ))
	else
		echo "  FAIL: $label (expected=$expected actual=$actual)"
		FAIL=$(( FAIL + 1 ))
	fi
}

COMMITS=$(( $(git rev-list --count HEAD) + 1 ))

echo "=== compute_version_code ==="

# major.minor combinations
code=$(compute_version_code "1.0.0")
assert_eq "v1.0.0" "$(( 1 * 100000 + 0 * 10000 + COMMITS ))" "$code"

code=$(compute_version_code "1.1.0")
assert_eq "v1.1.0" "$(( 1 * 100000 + 1 * 10000 + COMMITS ))" "$code"

code=$(compute_version_code "2.0.0")
assert_eq "v2.0.0" "$(( 2 * 100000 + 0 * 10000 + COMMITS ))" "$code"

code=$(compute_version_code "2.3.0")
assert_eq "v2.3.0" "$(( 2 * 100000 + 3 * 10000 + COMMITS ))" "$code"

# pre-release same as stable at same commit
code_beta=$(compute_version_code "1.0.0-beta1")
code_stable=$(compute_version_code "1.0.0")
assert_eq "pre-release same as stable at same commit" "$code_beta" "$code_stable"

# is positive integer
assert_eq "is positive integer" "1" "$([[ "$code" =~ ^[1-9][0-9]*$ ]] && echo 1 || echo 0)"

# major bump > minor bump
code_v1_1=$(compute_version_code "1.1.0")
code_v2_0=$(compute_version_code "2.0.0")
assert_eq "major 2.0 > minor 1.1" "1" "$(( code_v2_0 > code_v1_1 ? 1 : 0 ))"

# minor bump always higher
code_v1_0=$(compute_version_code "1.0.0")
code_v1_1=$(compute_version_code "1.1.0")
assert_eq "minor 1.1 > minor 1.0" "1" "$(( code_v1_1 > code_v1_0 ? 1 : 0 ))"

echo ""
if (( FAIL > 0 )); then
	echo "FAILED: $FAIL tests failed, $PASS passed"
	exit 1
else
	echo "OK: $PASS tests passed"
fi
