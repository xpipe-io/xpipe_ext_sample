# X-Pipe Sample Extension

This repository contains the code of a sample extension for X-Pipe,
which you can use as a basis to create your own extensions.
This repository covers everything extension related and tries to explain everything as clearly
as possible with a lot of comments, and should be a good basis for your own extension.


### Requirements

You need to following in order to utilize this repository:
- An X-Pipe installation
- A JDK installation (At least Java 17)
- A basic knowledge of how to run gradle tasks using the gradle wrapper
- A java ide that fully supports all modularity features

### Modularity

The X-Pipe project heavily utilizes java modules.
As a result, all is extensions must also be valid modules.
To properly unit test a module, the tests must also be modularized.
This structure, which is a perfectly valid java project, is 
too much to handle for eclipse, which have dropped the ball in adding modularity support.
Sadly, you will not be able to run any unit tests on eclipse
based platforms, which also includes visual studio code, when using the java extension.
It is strongly recommended to work on this project in IntelliJ, which fully supports all modularity features.

### Configuration Options

In order to configure the build and execution,
you can set several properties, which you can find in the file `dev.properties`.
The `targetXPipeVersion` property sets the X-Pipe API version which should be used to build against.
It is important that this version matches with the actual installation version to prevent any sort of issues.
Furthermore, there are two properties relating to the debugging of the X-Pipe daemon.
When the `attachDebugger` option is enabled, the launched daemon will wait until it can attach to a debugger.
Note that if it can't find a debugger, it will wait forever
The `debugMode` option will turn on the debug output for both the daemon and client, allowing you to diagnose errors more easily.

### Unit Tests

This repository also contains various unit tests for the extension.
Note that in order to accurately test an extension, it has to be run by the X-Pipe daemon.
Therefore, the unit test use the X-Pipe API to launch the daemon with the extension enabled,
it can be tested under actual conditions.
To not conflict with your local X-Pipe installation, the daemon that is launched by the unit 
test uses a different data directory and a different port.

### Production Tests

To test other features that are hard to test with unit tests,
there also exists an option to test your extension in production environment.
The gradle project is configured to be executable and launches the daemon
with your expansion enabled while also adhering to your debug configuration settings.
Just execute the gradle `run` task.
