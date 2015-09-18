@echo off
set SCRIPT_DIR=%~dp0
set CONNEXO_DIR=%SCRIPT_DIR%..

"%CONNEXO_DIR%\bin\ConnexoService" /uninstall
"%CONNEXO_DIR%\partners\tomcat\bin\service" remove ConnexoTomcat
call "%CONNEXO_DIR%\partners\yajsw-stable-11.11\bat\uninstallService.bat"

if exist "%CONNEXO_DIR%\partners\tomcat" rmdir /s /q "%CONNEXO_DIR%\partners\tomcat"
if exist "%CONNEXO_DIR%\partners\wso2is-4.5.0" rmdir /s /q "%CONNEXO_DIR%\partners\wso2is-4.5.0"
if exist "%CONNEXO_DIR%\partners\yajsw-stable-11.11" rmdir /s /q "%CONNEXO_DIR%\partners\yajsw-stable-11.11"

cd /D "%CONNEXO_DIR%"