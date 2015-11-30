Dataflow type system README
===========================

Requirements
------------

In order to use the dataflow type system, you first need to set up the
following four projects:

- https://bitbucket.org/typetools/jsr308-langtools
- https://github.com/typetools/annotation-tools
- https://github.com/typetools/checker-framework
- https://github.com/typetools/checker-framework-inference

You'll need environment variables `CHECKERFRAMEWORK`, `JSR308`,
`JAVA_HOME`, and `AFU` set up appropriately.
The `insert-annotations-to-source` script from AFU must be on your path.

The tool `do-like-javac` makes usage easier:

https://github.com/SRI-CSL/pascali-public.git                                        

I created a shortcut for this tool:

```
alias do-like-javac='python /the/path/to/do-like-javac/bin/do-like-javac.py'
```


Building
--------

1. Clone this repository into the `$JSR308` directory.

  ```
  cd $JSR308
  git clone https://github.com/Jianchu/LogiqlSolver.git
  ```

2. In the `$JSR308` directory, run
`LogiqlSolver/dataflowSetup.sh`. This bash script will set everything
up, recompile `checker-framework-inference`, and put the
`DataflowExample` directory into the `$JSR308` directory.

  ```
  bash LogiqlSolver/dataflowSetup.sh
  ```

3. Now you can use `checker-framework-inference` to invoke the
Dataflow Checker and Dataflow Solver like this:

  ```
  $JSR308/checker-framework-inference/scripts/inference.py
    --checker dataflow.DataflowChecker
    --solver dataflow.solver.DataflowSolver --mode INFER [List of files]
  ```


Running Example
---------------

After the second step of Building, a `DataflowExample` directory will be
placed under `$JSR308`.  This is a sample project that is annotated with
@Dataflow annotations, and you can play around with it: type check,
type infer, insert the inferred annotations to source code, generate
dot files, etc.

Here are some instructions that shows how to do these tasks with
`do-like-javac`:

1. Change into the DataflowExample directory:

  ```
  cd $JSR308/DataflowExample
  ```

2. Invoke the inference tool using `do-like-javac`.
If the inference is successful, a `default.jaif` file will be
generated in the current directory.

  ```
  do-like-javac -t inference --checker dataflow.DataflowChecker
    --solver dataflow.solver.DataflowSolver -o logs -- ant compile
  ```

3. Insert the infered annotations into the source code. A
directory called `annotated` will be created in the current directory. You can
find a source code with infered annotations in it.

  ```
  insert-annotations-to-source default.jaif src/DataflowExample.java
  ```

4. Invoke the checker tool with `do-like-javac`.
This step will type check the newly created source code, and generate
`.dot` files (in the `dotfiles` directory) that visualize the
control flow graph.

  ```
  do-like-javac -t checker --checker dataflow.DataflowChecker
    -Aflowdotdir=./dotfiles -o logs -- ant check-annotated-src
  ```

If you compare the original source code with the source code generated
by the third step, you can find the string field
`insertAnnotationToThis` is annotated with
`@DataFlow(typeNames={"java.lang.String"})` in the new source code.
