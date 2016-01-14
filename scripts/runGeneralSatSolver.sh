#!/bin/bash

export MYDIR=`dirname $0`
. $MYDIR/setup.sh
export ROOT=$MYDIR/../..
distDir=$CHINF"/dist"
java -classpath /home/jianchu/jsr308/generic-type-inference-solver/bin:"$distDir"/checker.jar:"$distDir"/plume.jar:"$distDir"/checker-framework-inference.jar checkers.inference.InferenceLauncher --checker ostrusted.OsTrustedChecker --solver generalconstraintsolver.satsubsolver.GeneralSatSolver --mode INFER $*