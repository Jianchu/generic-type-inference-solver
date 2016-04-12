#!/bin/bash
ROOT=$TRAVIS_BUILD_DIR/..

# Fail the whole script if any command fails
set -e

export SHELLOPTS

./.travis-build-without-test.sh

cd $ROOT/checker-framework-inference

gradle copytest
ant -f tests.xml
