#!/bin/bash
export MYDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

ant -buildfile $MYDIR/src/dataflow/dataflowexample/build.xml compile-libs
ln -s $MYDIR/src/dataflow $MYDIR/../checker-framework-inference/src
mv $MYDIR/../checker-framework-inference/src/dataflow/tests/DataflowTest.java $MYDIR/../checker-framework-inference/tests/checkers/inference
ln -s $MYDIR/../checker-framework-inference/src/dataflow/tests/testdata/dataflow $MYDIR/../checker-framework-inference/testdata
ln -s $MYDIR/src/dataflow/dataflowexample $MYDIR/..
gradle -b $MYDIR/../checker-framework-inference/build.gradle dist
