#!/bin/bash

export MYDIR=`dirname $0`
. $MYDIR/setup.sh
mkdir $MYDIR/../bin/
$JAVAC -source 7 -target 7 -d $MYDIR/../bin/ `find $MYDIR/../src/ -name "*.java"`

