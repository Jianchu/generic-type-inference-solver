#!/bin/bash

# Links will already exist if run before.
# Therefore, don't fail if any of teh commands fails.
# TODO: what is cleaner way to do this?
set +e

export MYDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

ant -buildfile $MYDIR/testing/dataflowexample/build.xml compile-libs

ln -s $MYDIR/src/dataflow $MYDIR/../checker-framework-inference/src
ln -s $MYDIR/src/ontology $MYDIR/../checker-framework-inference/src
ln -s $MYDIR/src/constraintsolver $MYDIR/../checker-framework-inference/src
ln -s $MYDIR/src/generalconstraintsolver $MYDIR/../checker-framework-inference/src
ln -s $MYDIR/src/maxsatbackend $MYDIR/../checker-framework-inference/src
ln -s $MYDIR/src/util $MYDIR/../checker-framework-inference/src
ln -s $MYDIR/src/constraintsolver $MYDIR/../checker-framework-inference/src
ln -s $MYDIR/src/logiqlbackend $MYDIR/../checker-framework-inference/src
ln -s $MYDIR/src/constraintgraph $MYDIR/../checker-framework-inference/src

ln -s $MYDIR/testing/tests/DataflowTest.java $MYDIR/../checker-framework-inference/tests/checkers/inference
ln -s $MYDIR/testing/tests/OntologyTest.java $MYDIR/../checker-framework-inference/tests/checkers/inference

ln -s $MYDIR/testing/dataflow $MYDIR/../checker-framework-inference/testdata
ln -s $MYDIR/testing/ontology $MYDIR/../checker-framework-inference/testdata

ln -s $MYDIR/testing/dataflowexample $MYDIR/..

bash $MYDIR/scripts/compile.sh
gradle -b $MYDIR/../checker-framework-inference/build.gradle dist
