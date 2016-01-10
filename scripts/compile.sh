#!/bin/bash

export MYDIR=`dirname $0`
. $MYDIR/setup.sh
mkdir $MYDIR/../bin/
rm -rf $MYDIR/../bin/*
$JAVAC -source 8 -target 8 -d $MYDIR/../bin/ `find $MYDIR/../src -name "*.java"`

