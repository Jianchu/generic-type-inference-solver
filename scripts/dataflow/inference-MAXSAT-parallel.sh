#!/bin/bash

export MYDIR=`dirname $0`/..
. $MYDIR/setup.sh
export ROOT=$MYDIR/../..
distDir=$CHINF"/dist"

export CLASSPATH=$ROOT/generic-type-inference-solver/bin:/home/jianchu/jsr308/checker-framework-inference/dist/plume.jar:.

$ROOT/checker-framework-inference/scripts/inference-dev checkers.inference.InferenceLauncher --solverArgs="backEndType=maxsatbackend.MaxSat" --checker dataflow.DataflowChecker --solver dataflow.solvers.backend.DataflowConstraintSolver --mode INFER --hacks=true $*