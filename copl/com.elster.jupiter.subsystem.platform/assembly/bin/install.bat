@echo off
set SCRIPT_DIR=%~dp0

set /p jdbcUrl= Please enter the database url (format: jdbc:oracle:thin:@yourHost:yourPort:yourSID):
set /p dbUserName= Please enter the database user:
set /p dbPassword= Please enter the database password:

for /f "tokens=* delims= " %%a in ("%jdbcUrl%") do set jdbcUrl=%%a
for /f "tokens=* delims= " %%a in ("%dbUserName%") do set dbUserName=%%a
for /f "tokens=* delims= " %%a in ("%dbPassword%") do set dbPassword=%%a

set config_file=%SCRIPT_DIR%/../conf/config.properties

echo com.elster.jupiter.datasource.jdbcurl=%jdbcUrl% >> %config_file%
echo com.elster.jupiter.datasource.jdbcuser=%dbUsername% >> %config_file%
echo com.elster.jupiter.datasource.jdbcpassword=%dbPassword% >> %config_file%

echo Installing Connexo database schema
%SCRIPT_DIR%/Connexo.exe --install


SET /P ANSWER=Do you want to install Connexo as a windows service (Y/N)?
if /i {%ANSWER%}=={y} (goto :yes)
if /i {%ANSWER%}=={yes} (goto :yes)
goto :no
:yes
%SCRIPT_DIR%/ConnexoService.exe /install

:no
