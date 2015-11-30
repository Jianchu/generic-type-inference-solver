#!/bin/bash
export MYDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

ln -s $MYDIR/src/dataflow $MYDIR/../checker-framework-inference/src
mv $MYDIR/../checker-framework-inference/src/dataflow/tests/DataflowTest.java $MYDIR/../checker-framework-inference/tests/checkers/inference
ln -s $MYDIR/../checker-framework-inference/src/dataflow/tests/testdata/dataflow $MYDIR/../checker-framework-inference/testdata
ln -s $MYDIR/src/dataflow/DataflowExample $MYDIR/..
gradle -b $MYDIR/../checker-framework-inference/build.gradle dist
