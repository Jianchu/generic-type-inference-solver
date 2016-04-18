#!/bin/bash

export MYDIR=`dirname $0`/..
. $MYDIR/setup.sh
export ROOT=$MYDIR/../..
distDir=$CHINF"/dist"
java -classpath "$distDir"/checker.jar:"$distDir"/plume.jar:"$distDir"/checker-framework-inference.jar:$ROOT/universe/bin checkers.inference.InferenceLauncher --solverArgs="backEndType=maxsatbackend.MaxSat" --checker ontology.OntologyChecker --solver constraintsolver.ConstraintSolver --mode ROUNDTRIP -afud $ROOT/mini_corpus/sorting/00_sort/annotated $*