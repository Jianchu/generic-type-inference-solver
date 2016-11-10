#!/bin/bash

export MYDIR=`dirname $0`/..
. $MYDIR/setup.sh
export ROOT=$MYDIR/../..
export LOGICDATA=./logiqldata
distDir=$CHINF"/dist"

export CLASSPATH=$ROOT/generic-type-inference-solver/bin

$ROOT/checker-framework-inference/scripts/inference-dev checkers.inference.InferenceLauncher --solverArgs="backEndType=logiqlbackend.LogiQL,useGraph=false" --checker ostrusted.OsTrustedChecker --solver constraintsolver.ConstraintSolver --mode INFER --hacks=true $*
rm -r $LOGICDATA