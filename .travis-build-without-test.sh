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

#TODO: this split logic is copy from checker-framework-inference's travis script
#TODO: should refactor later on
# Split $TRAVIS_REPO_SLUG into the owner and repository parts
OIFS=$IFS
IFS='/'
read -r -a slugarray <<< "$TRAVIS_REPO_SLUG"
SLUGOWNER=${slugarray[0]}
SLUGREPO=${slugarray[1]}
IFS=$OIFS

## Build Checker Framework
if [ -d $ROOT/checker-framework-inference ] ; then
    # Older versions of git don't support the -C command-line option
    (cd $ROOT/checker-framework-inference && git pull)
else
    (cd $ROOT && git clone https://github.com/"$SLUGOWNER"/checker-framework-inference.git)
fi

# This also builds annotation-tools and jsr308-langtools
(cd $ROOT/checker-framework-inference/ && ./.travis-build-without-test.sh)


cd $ROOT
bash generic-type-inference-solver/integrationSetup.sh
