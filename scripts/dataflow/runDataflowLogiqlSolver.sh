#!/bin/bash

export MYDIR=`dirname $0`/..
. $MYDIR/setup.sh
export ROOT=$MYDIR/../..
export LOGICDATA=$MYDIR/../src/generalconstraintsolver/dataflowsolver/logicdata
rm -r $LOGICDATA
distDir=$CHINF"/dist"
java -classpath "$distDir"/checker.jar:"$distDir"/plume.jar:"$distDir"/checker-framework-inference.jar:$ROOT/universe/bin  checkers.inference.InferenceLauncher --checker dataflow.DataflowChecker --solver generalconstraintsolver.dataflowsolver.dataflowlogiqlsolver.DataflowLogiqlSolver --mode INFER $*