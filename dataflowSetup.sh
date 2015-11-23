#!/bin/bash

export MYDIR=`dirname $0`
cp -r $MYDIR/src/dataflow $MYDIR/../checker-framework-inference/src
cp -r $MYDIR/src/dataflow/DataflowExample $MYDIR/..
gradle -b ../checker-framework-inference/build.gradle dist