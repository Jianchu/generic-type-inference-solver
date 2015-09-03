export ROOT=$MYDIR/../..

export JAVAC=$ROOT/jsr308-langtools/dist/bin/javac
export CHECKERFRAMEWORK=$ROOT/checker-framework

# ensures that the JSR308 javac uses the JDK7 java
export PATH=$JAVA_HOME/bin:$PATH
export JAVA=$JAVA_HOME/bin/java
export CHINF=$ROOT/checker-framework-inference

# export SCALA_HOME=/home/software/scala/scala/current/
# export SCALA=$SCALA_HOME/bin/scala
# export SCALAC=$SCALA_HOME/bin/fsc
export SCALAC=fsc
export JAVACMD=$JAVA

export CFCLASSPATH=$ROOT/checker-framework-inference/bin:$ROOT/universe/bin:$ROOT/LogiqlSolver/bin:$ROOT/jsr308-langtools/build/classes:$CHECKERFRAMEWORK/javacutil/build/:$CHECKERFRAMEWORK/dataflow/build/:$CHECKERFRAMEWORK/framework/build/:$CHECKERFRAMEWORK/stubparser/build/:$CHECKERFRAMEWORK/checker/build/:$CHECKERFRAMEWORK/framework/tests/junit.jar:$CHECKERFRAMEWORK/checker/dist/checker.jar:$JAVA_HOME/lib/tools.jar:$JAVA_HOME/jre/lib/rt.jar:$SCALA_HOME/lib/scala-library.jar:$SCALA_HOME/lib/scala-compiler.jar:$ROOT/plume-lib/java/plume.jar:$ROOT/annotation-tools/annotation-file-utilities/bin:$ROOT/annotation-tools/annotation-file-utilities/annotation-file-utilities.jar:$ROOT/checker-framework-inference/tests:.

export CLASSPATH=$CFCLASSPATH

export JAVA_OPTS="-ea -server -Xmx1024m -Xms512m -Xss1m -Xbootclasspath/p:$CLASSPATH"



