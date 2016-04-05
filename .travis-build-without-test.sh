#!/bin/bash
ROOT=$TRAVIS_BUILD_DIR/..

# Fail the whole script if any command fails
set -e

export SHELLOPTS

## Build Checker Framework
if [ -d $ROOT/checker-framework-inference ] ; then
    # Older versions of git don't support the -C command-line option
    (cd $ROOT/checker-framework-inference && git pull)
else
    (cd $ROOT && git clone https://github.com/typetools/checker-framework-inference.git)
fi

# This also builds annotation-tools and jsr308-langtools
(cd $ROOT/checker-framework-inference/ && ./.travis-build-without-test.sh)

# TODO: how do we correctly build?
scripts/compile.sh
