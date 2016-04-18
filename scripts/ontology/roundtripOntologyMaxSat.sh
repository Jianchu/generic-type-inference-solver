#!/bin/bash

export MYDIR=`dirname $0`/..
. $MYDIR/setup.sh
export ROOT=$MYDIR/../..
CORPUS_DIR=$ROOT/../corpus
distDir=$CHINF"/dist"
java -classpath "$distDir"/checker.jar:"$distDir"/plume.jar:"$distDir"/checker-framework-inference.jar:$ROOT/generic-type-inference-solver/bin checkers.inference.InferenceLauncher --solverArgs="backEndType=maxsatbackend.MaxSat" --checker ontology.OntologyChecker --solver constraintsolver.ConstraintSolver --mode ROUNDTRIP -afud $CORPUS_DIR/sorting/00_sort/annotated $*