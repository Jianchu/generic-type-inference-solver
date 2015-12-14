This is a demo that shows if we run checker-framework-inference tool
on the projects for which we need to explicitly specify the classpath, 
the inference tool will fail.

Instructions
--------

1.Change into the demo directory:

```
cd /path/to/demo
```
  
2. Compile java files in libs directory:
```
ant compile-libs
```

3. Run inference tool either by do-like-javac:

```
do-like-javac -t inference --checker dataflow.DataflowChecker
  --solver dataflow.solver.DataflowSolver -o logs -- compile-project
```

or by $JSR308/checker-framework-inference/scripts/inference:

```
$JSR308/checker-framework-inference/scripts/inference
  --checker dataflow.DataflowChecker
  --solver dataflow.solver.DataflowSolver --mode INFER project/src/Bar.java
```

After this step, there will be an error shows that " package libs does not exist".