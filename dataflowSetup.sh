#!/bin/bash
export MYDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

cp -r $MYDIR/src/dataflow $MYDIR/../checker-framework-inference/src
mv $MYDIR/../checker-framework-inference/src/dataflow/tests/DataflowTest.java $MYDIR/../checker-framework-inference/tests/checkers/inference
cp -r $MYDIR/../checker-framework-inference/src/dataflow/tests/testdata/dataflow $MYDIR/../checker-framework-inference/testdata
cp -r $MYDIR/src/dataflow/DataflowExample $MYDIR/..
gradle -b $MYDIR/../checker-framework-inference/build.gradle dist