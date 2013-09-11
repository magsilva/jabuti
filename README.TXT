JABUTI 1.0

Building
--------

The easiest way to do it is using Ant, the platform-independent Java-based build tool, by Apache Foundation. You can get it from http://ant.apache.org.

To build Jabuti, at the prompt type:

   ant jar

This will build the software within the directory build/jar.

The command 'ant clean' cleans up all the generated files.

Running
-------

You can run Jabuti by typing:

  ant run

Or, manually, withing build/jar you can run

java -cp Jabuti-bin.jar br.jabuti.gui.JabutiGUI

Other ways to run Jabuti
------------------------

To execute a test case

java -cp Jabuti-bin.jar br.jabuti.probe.ProberLoader -p <project name> <class to execute> [execution parameters]


To instrument and store it instrumented

java -cp Jabuti-bin.jar br.jabuti.probe.ProberInstrum -o <file.jar> -p <project name> <class to execute>

Once instrumented, to execute a test case

java -cp <file.jar> <class to execute> [execution parameters]