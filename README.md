# JaBUTi 1.1

## Requirements
* Java 11 or greater
* Java 1.5 (yes, you need both!)
* Graphviz (https://www.graphviz.org/)


## Building

The easiest way to do it is using Maven, a platform-independent Java-based build tool, by Apache Foundation. You can get it from http://maven.apache.org.

As JaBUTi should be compiled as Java 5 application, you must install the Java 1.5 SDK and configure the path for it in the file toolchains.org.

To build Jabuti, at the prompt type:
```
mvn --global-toolchains toolchains.xml install
```

This will build the software within the directory `target`. You will find JaBUTi ready to run in `target/JaBUTi`.


## Running

* `cd target/JaBUTi/`
* `java -jar JaBUTi.jar`


## How to use JaBUTi

* [Quick tutorial (in Portuguese)](https://docs.google.com/presentation/d/1sWLRY1w_FrFj3232OqYo6gJeGYynbIRvD_0C06t49Xc/edit?usp=sharing)
* [Longer tutorial, with videos demonstrating usage](doc/Tutorial-Slides.pdf)
* [User's guide](doc/UserGuide.pdf)
