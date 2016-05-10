#!/bin/bash
ROOT=$TRAVIS_BUILD_DIR/..

# Fail the whole script if any command fails
set -e

export SHELLOPTS

export JAVA_HOME=${JAVA_HOME:-$(dirname $(dirname $(dirname $(readlink -f $(/usr/bin/which java)))))}

export JSR308=$ROOT
export AFU=$ROOT/annotation-tools/annotation-file-utilities
export CHECKERFRAMEWORK=$ROOT/checker-framework

export PATH=$AFU/scripts:$JAVA_HOME/bin:$PATH

## Build Checker Framework
if [ -d $ROOT/checker-framework-inference ] ; then
    # Older versions of git don't support the -C command-line option
    (cd $ROOT/checker-framework-inference && git pull)
else
    (cd $ROOT && git clone https://github.com/pascaliUWat/checker-framework-inference.git)
fi

# This also builds annotation-tools and jsr308-langtools
(cd $ROOT/checker-framework-inference/ && ./.travis-build-without-test.sh)


cd $ROOT
bash generic-type-inference-solver/integrationSetup.sh
