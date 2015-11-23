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

