# Compilers Project

Project developed in the compilers class. This programming project aims at learning the various aspects of programming language design and implementation by building a working compiler for a simple, but realistic high-level programming language. In this process, it was applied the knowledge acquired during the lectures and understanding of the various underlying algorithms and implementation trade-offs. The envisioned compiler will be able to handle a subset of the popular Java programming language and generate valid JVM (Java Virtual Machine) instructions in the jasmin format,which are then translated into Java bytecodes by the jasmin assembler. 

To run this project, you need to install [Java](https://jdk.java.net/), [Gradle](https://gradle.org/install/). Please check the [compatibility matrix](https://docs.gradle.org/current/userguide/compatibility.html) for Java and Gradle versions.


## Compile and Running

To compile and install the program, run ``gradle installDist``. This will compile your classes and create a launcher script in the folder ``./build/install/comp2022-00/bin``. For convenience, there are two script files, one for Windows (``comp2022-00.bat``) and another for Linux (``comp2022-00``), in the root folder, that call tihs launcher script.

After compilation, a series of tests will be automatically executed. The build will stop if any test fails. Whenever you want to ignore the tests and build the program anyway, you can call Gradle with the flag ``-x test``.


