JaBUTi 1.1
----------

Requirements
------------
* Java 1.6 or above to run


Requirements for system under testing (SUT)
-------------------------------------------
* Java 1.6 or _below_
  The library we use to manipulate Java bytecode is not completely compatible
  with Java 7.0 and above. We could still test when using the option "-XX:-SplitVerifier",
  but this option is ignored by recent Java 7 and 8 virtual machines. Thus, the only
  safe choice is to use an application compiled to Java 1.6 or below.


Building
--------

The easiest way to do it is using Maven, a platform-independent Java-based build tool, by Apache Foundation. You can get it from http://maven.apache.org.

To build Jabuti, at the prompt type:

   mvn install

This will build the software within the directory 'target/'

The command 'mvn clean' cleans up all the generated files.


Running
-------

java -jar jabuti.jar

