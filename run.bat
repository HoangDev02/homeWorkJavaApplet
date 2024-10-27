@echo off
if not defined JAVA_HOME (
    @echo environment [JAVA_HOME] is not set. Please set it to point to JDK 17
    exit /b 1
)
if not defined JC_HOME_SIMULATOR (
    @echo environment [JC_HOME_SIMULATOR] is not set. Please set it to point to latest Java Card SDK
    exit /b 1
)
set EXT_MODULEPATH="%JC_HOME_SIMULATOR%/client/AMService;%JC_HOME_SIMULATOR%/client/COMService/"
setlocal
for %%I in (.) do set SAMPLE_NAME=%%~nxI
for %%I in (.) do set SAMPLES_DIR=%%~dpI
@echo Running Java client for example [%SAMPLE_NAME%] ...
pushd homeWorkClient\bin
"%JAVA_HOME%/bin/java" -p "%JC_HOME_SIMULATOR%/client/AMService/amservice.jar;%JC_HOME_SIMULATOR%/client/COMService/socketprovider.jar;%JC_HOME_SIMULATOR%/client/COMService/extension.jar" --module-path %EXT_MODULEPATH% --add-modules ALL-MODULE-PATH com.oracle.javacard.sample.AMSMyApplet -cap=%SAMPLES_DIR%/%SAMPLE_NAME%/homeWordApplet/deliverables/homeWordApplet/com/example/myapplet/javacard/myapplet.cap -props="D:\testApplet\client.config.properties"
popd
endlocal