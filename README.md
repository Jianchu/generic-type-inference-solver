Dataflow type system README
==================================

Requirements
------------
In order to use dataflow type system, you will need the following four tools installed:

http://types.cs.washington.edu/jsr308/                                                              
http://types.cs.washington.edu/annotation-file-utilities/                                
http://types.cs.washington.edu/checker-framework/                                              
https://github.com/wmdietl/checker-framework-inference/                                                  

You'll need CHECKERFRAMEWORK, JSR308, JAVA_HOME, and AFU environment variables set up appropriately.
insert-annotations-to-source (from AFU) must be on your path.

The tool do-like-javac will make the usage easier.                                                  
https://github.com/SRI-CSL/pascali-public.git                                        

I created a shortcut for this tool:
alias do-like-javac='python /the/path/to/do-like-javac/bin/do-like-javac.py'

Building
------------
1. Clone this repository under $JSR308 directory.                                                 
   cd $JSR308                                                  
   git clone https://github.com/Jianchu/LogiqlSolver.git  
2. Run LogiqlSolver/dataflowSetup.sh under $JSR308 directory. This bash script will set everything, recompile checker-framework-inference, and put DataflowExample directory under current path ($JSR308).
   bash LogiqlSolver/dataflowSetup.sh
3. Now you can use checker-framework-inference to invoke dataflow checker and dataflow inference in normal way.  
    example:  
    ./scripts/inference.py --checker dataflow.DataflowChecker --solver dataflow.solver.DataflowSolver --mode INFER [List of files]  

Running Example
------------
After the second step of Building, a DataflowExample directory will be placed under $JSR308.  This is a sample project that shows how dataflow type system works. Here are some instructions that shows how to use do-like-javac 

1. cd into DataflowExample directory   
   cd DataflowExample  
2. invoke inference tool by do-like-javac. If the inference is successful, a jaif file will be generated under current path called default.jaif.    
   do-like-javac -t inference --checker dataflow.DataflowChecker --solver dataflow.solver.DataflowSolver -o logs -- ant compile     
3. Insert the infered annotations to source code. Then an annotated directory will be created under current path. You can find a source code with infered annotations in it.      
   insert-annotations-to-source default.jaif src/DataflowExample.java     
4. invoke checker tool by do-like-javac. This step will type check the new created source code, and generate .dot files that visualize the control flow graph, which be placed under dotfiles directory.  
   do-like-javac -t checker --checker dataflow.DataflowChecker\ -Aflowdotdir=./dotfiles -o logs -- ant check-annotated-src


 




