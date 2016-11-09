#!/bin/bash

export MYDIR=`dirname $0`/..
. $MYDIR/setup.sh
export ROOT=$MYDIR/../..
distDir=$CHINF"/dist"

export CLASSPATH=$ROOT/generic-type-inference-solver/bin:.

$ROOT/checker-framework-inference/scripts/inference-dev checkers.inference.InferenceLauncher --checker dataflow.DataflowChecker --mode TYPECHECK --hacks=true $*