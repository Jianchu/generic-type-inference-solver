#!/bin/bash
export MYDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

ant -buildfile $MYDIR/testing/dataflowexample/build.xml compile-libs
ln -s $MYDIR/src/dataflow $MYDIR/../checker-framework-inference/src
cp $MYDIR/testing/tests/DataflowTest.java $MYDIR/../checker-framework-inference/tests/checkers/inference
ln -s $MYDIR/../checker-framework-inference/src/dataflow/tests/testdata/dataflow $MYDIR/../checker-framework-inference/testdata
ln -s $MYDIR/testing/dataflowexample $MYDIR/..
gradle -b $MYDIR/../checker-framework-inference/build.gradle dist
