::
:: Copyright (c) 1998, 2024, Oracle and/or its affiliates. All rights reserved.
::
@echo off

if not defined JAVA_HOME (
    @echo environment [JAVA_HOME] is not set. Please set it to point to JDK 17
    exit /b 1
)

if not defined JC_HOME_TOOLS (
    @echo environment [JC_HOME_TOOLS] is not set. Please set it to point to latest Java Card Tools
    exit /b 1
)

if not defined JC_HOME_SIMULATOR (
    @echo environment [JC_HOME_SIMULATOR] is not set. Please set it to point to latest Java Card SDK
    exit /b 1
)

setlocal
for %%I in (.) do set SAMPLE_NAME=%%~nxI
@echo "Building Java Card applet(s) for example [%SAMPLE_NAME%] ..."
pushd .\homeWordApplet\src
set JAVA_FILES=files.lst
dir /s /b *.java > %JAVA_FILES%
@echo Java file(s) to be compiled:
type %JAVA_FILES%
"%JAVA_HOME%/bin/javac" -g -d ..\bin -cp "%JC_HOME_TOOLS%/lib/api_classic-3.2.0.jar;%JC_HOME_TOOLS%\lib\api_classic_annotations-3.2.0.jar" -source 7 -target 7 -Xlint:-options @%JAVA_FILES%
del %JAVA_FILES%
call "%JC_HOME_TOOLS%/bin/converter.bat" -config ../configurations/homeWordApplet.conf
call "%JC_HOME_TOOLS%/bin/verifyexp.bat" ../deliverables/homeWordApplet/com/example/myapplet/javacard/myapplet.exp
popd
@echo.
@echo Building Java client for example [%SAMPLE_NAME%] ...
pushd homeWorkClient\src
dir /s /b *.java > %JAVA_FILES%
@echo Java file(s) to be compiled:
type %JAVA_FILES%
"%JAVA_HOME%/bin/javac" -g -d ..\bin -cp "%JC_HOME_SIMULATOR%/client/AMService/amservice.jar;%JC_HOME_SIMULATOR%/client/COMService/socketprovider.jar" @%JAVA_FILES%
del %JAVA_FILES%
popd
endlocal