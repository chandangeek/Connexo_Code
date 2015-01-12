@echo off
set SCRIPT_DIR=%~dp0
IF "%JAVA_HOME%"=="" (
   echo Please define your JAVA_HOME
   goto :END
)

set CUR_DIR=%CD%
set JUPITER_INSTALLATION_DIR=%SCRIPT_DIR%\..

set VM_OPTIONS=-Djava.util.logging.config.file=%JUPITER_INSTALLATION_DIR%\conf\logging.properties
REM create logs directory if not exists
mkdir %JUPITER_INSTALLATION_DIR%\logs 2>NUL

cd %JUPITER_INSTALLATION_DIR%
echo Starting Connexo with interactive shell....
%JAVA_HOME%\bin\java %VM_OPTIONS% -cp lib\org.apache.felix.main-4.4.1.jar;lib\javax.annotation-api-1.2.jar org.apache.felix.main.Main
cd %CUR_DIR%

:END