#!/bin/bash
export MYDIR=`dirname $0`
python /home/demo/feb2016/types/pascali-public/do-like-javac/bin/do-like-javac.py -t inference --checker dataflow.DataflowChecker --solver dataflow.solver.DataflowSolver -o logs -- ant compile
cp -r $MYDIR/build/DataflowExample/ $MYDIR
bash /home/demo/feb2016/types/annotation-tools/annotation-file-utilities/scripts/insert-annotations-to-source default.jaif src/DataflowExample.java
python /home/demo/feb2016/types/pascali-public/do-like-javac/bin/do-like-javac.py -t checker --checker "dataflow.DataflowChecker -Aflowdotdir=./dotfiles" -o logs -- ant check-annotated-src
dot -Tpdf dotfiles/_init_Dataflow.dot -o CFG.pdf
