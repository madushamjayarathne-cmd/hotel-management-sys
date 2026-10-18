#!/bin/sh
# Compiles the app and the framework-free tests, then runs all of them.
set -e
cd "$(dirname "$0")"
OUT=$(mktemp -d)
javac --release 21 -d "$OUT" $(find src test -name '*.java')
cp resources/* "$OUT"/
java -cp "$OUT" com.hotel.testing.AllTests
