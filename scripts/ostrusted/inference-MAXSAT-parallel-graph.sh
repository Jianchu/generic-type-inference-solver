#!/bin/bash

export MYDIR=`dirname $0`/..
. $MYDIR/setup.sh
export ROOT=$MYDIR/../..
distDir=$CHINF"/dist"

export CLASSPATH=$ROOT/generic-type-inference-solver/bin

$ROOT/checker-framework-inference/scripts/inference-dev checkers.inference.InferenceLauncher --solverArgs="backEndType=maxsatbackend.MaxSat,collectStatistic=true" --checker ostrusted.OsTrustedChecker --solver constraintsolver.ConstraintSolver --mode INFER --hacks=true $*
