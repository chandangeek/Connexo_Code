@echo off
NET SESSION >nul 2>&1
IF %ERRORLEVEL% NEQ 0 (
	echo Please run this script as administrator
	goto :END
)

set SCRIPT_DIR=%~dp0
set CONNEXO_DIR=%SCRIPT_DIR%..

IF "%JAVA_HOME%"=="" (
   echo Please define your JAVA_HOME
   goto :END
) ELSE (
	for /f tokens^=2-3^ delims^=.^" %%j in ('"%JAVA_HOME%/bin/java" -fullversion 2^>^&1') do set "JAVA_VERSION=%%j%%k"
)

IF NOT "%JAVA_VERSION%"=="18" (
	echo Please install java 1.8 or change your JAVA_HOME variable to the correct installation
	goto :END
)

if [%1] == [config] goto :load-config
if [%1] == [tomcat] goto :install-tomcat
if [%1] == [wso2] goto :wso2-is
if [%1] == [facts] goto :facts
if [%1] == [flow] goto :flow

:load-config
call "%SCRIPT_DIR%/config.cmd"

echo Installing Connexo Container ...
echo ==========================================================================
if NOT [%1] == [config] (
	set /p jdbcUrl= "Please enter the database url (format: jdbc:oracle:thin:@dbHost:dbPort:dbSID): "
	set /p dbUserName= "Please enter the database user: "
	set /p dbPassword= "Please enter the database password: "
	set /p CONNEXO_HTTP_PORT= "Please enter the Connexo http port: "

	for /f "tokens=* delims= " %%a in ("%jdbcUrl%") do set jdbcUrl=%%a
	for /f "tokens=* delims= " %%a in ("%dbUserName%") do set dbUserName=%%a
	for /f "tokens=* delims= " %%a in ("%dbPassword%") do set dbPassword=%%a
	for /f "tokens=* delims= " %%a in ("%CONNEXO_HTTP_PORT%") do set CONNEXO_HTTP_PORT=%%a
)
set config_file="%SCRIPT_DIR%/../conf/config.properties"
set CONNEXO_URL=http://localhost:%CONNEXO_HTTP_PORT%

echo com.elster.jupiter.datasource.jdbcurl=%jdbcUrl%>> %config_file%
echo com.elster.jupiter.datasource.jdbcuser=%dbUsername%>> %config_file%
echo com.elster.jupiter.datasource.jdbcpassword=%dbPassword%>> %config_file%

echo Installing Connexo database schema
"%SCRIPT_DIR%/Connexo.exe" --install

if NOT [%1] == [config] (
	SET /P CONNEXO_SERVICE="Do you want to install Connexo as a windows service (Y/N)? "
)
if /i {%CONNEXO_SERVICE%}=={y} (goto :yes)
if /i {%CONNEXO_SERVICE%}=={yes} (goto :yes)
goto :no
:yes
"%SCRIPT_DIR%/ConnexoService.exe" /install

:no

:install-tomcat
set TOMCAT_DIR=tomcat
set TOMCAT_BASE=%CONNEXO_DIR%\partners
set TOMCAT_ZIP=tomcat-7.0.59
set CATALINA_BASE=%TOMCAT_BASE%\%TOMCAT_DIR%
set CATALINA_HOME=%CATALINA_BASE%
set TOMCAT_SHUTDOWN_PORT=8006

echo.
echo Extracting Apache Tomcat 7 ...
echo ==========================================================================

if NOT [%1] == [config] (
	set /p TOMCAT_HTTP_PORT= "Please enter the Tomcat http port: "
	for /f "tokens=* delims= " %%a in ("%TOMCAT_HTTP_PORT%") do set TOMCAT_HTTP_PORT=%%a
)

set JVM_OPTIONS=-Dport.shutdown=%TOMCAT_SHUTDOWN_PORT%;-Dport.http=%TOMCAT_HTTP_PORT%;-Dconnexo.url=%CONNEXO_URL%;-Dbtm.root=%CATALINA_HOME%;-Dbitronix.tm.configuration=%CATALINA_HOME%\conf\btm-config.properties;-Djbpm.tsr.jndi.lookup=java:comp/env/TransactionSynchronizationRegistry

cd /D "%TOMCAT_BASE%"
"%JAVA_HOME%/bin/jar" -xvf %TOMCAT_ZIP%.zip
if exist "%TOMCAT_DIR%" rmdir /s /q "%TOMCAT_DIR%"
rename apache-%TOMCAT_ZIP% "%TOMCAT_DIR%"
REM del %TOMCAT_ZIP%.zip

echo set CATALINA_OPTS=%CATALINA_OPTS% -Xmx512M ^
 -Dport.shutdown=%TOMCAT_SHUTDOWN_PORT% ^
 -Dport.http=%TOMCAT_HTTP_PORT% ^
 -Dconnexo.url=%CONNEXO_URL% ^
 -Dbtm.root=%%CATALINA_HOME%% ^
 -Dbitronix.tm.configuration=%%CATALINA_HOME%%\conf\btm-config.properties ^
 -Djbpm.tsr.jndi.lookup=java:comp/env/TransactionSynchronizationRegistry  > "%TOMCAT_DIR%\bin\setenv.bat"

cd /D "%TOMCAT_DIR%/bin"

echo Installing Apache Tomcat For Connexo as service ...
call service install ConnexoTomcat

cd /D "%CONNEXO_DIR%"

:wso2-is
set WSO2_DIR=%CONNEXO_DIR%\partners
echo.
echo Installing WSO2 Identity Server ...
echo ==========================================================================
cd "%WSO2_DIR%"
"%JAVA_HOME%/bin/jar" -xvf wso2is-4.5.0.zip
REM del wso2is-4.5.0.zip
"%JAVA_HOME%/bin/jar" -xvf yajsw-11.11.zip
REM del yajsw-stable-11.11.zip
cd /D "%CONNEXO_DIR%"

setx CARBON_HOME "%WSO2_DIR%\wso2is-4.5.0" /m

copy "%WSO2_DIR%\wso2is-4.5.0\bin\yajsw\wrapper.conf" "%WSO2_DIR%\yajsw-stable-11.11\conf"
call "%WSO2_DIR%\yajsw-stable-11.11\bat\installService.bat" < NUL
REM call "%WSO2_DIR%\yajsw-stable-11.11\bat\startService.bat" < NUL
echo WSO2 Identity Server successfully installed

cd /D "%CONNEXO_DIR%"

:facts
set INSTALLER_LICENSE=%CONNEXO_DIR%\partners\facts\facts-license.lic
set FACTS_BASE_PROPERTIES=partners\facts\facts.properties
set FACTS_BASE=%TOMCAT_BASE%\%TOMCAT_DIR%\webapps
set FACTS_DIR=%FACTS_BASE%\facts
set FACTS_PORT=%TOMCAT_HTTP_PORT%
set CONNEXO_ADMIN_ACCOUNT=admin
set CONNEXO_ADMIN_PASSWORD=admin

echo.
echo Installing Connexo Facts ...
echo ==========================================================================
if NOT [%1] == [config] (
	set /p FACTS_DB_HOST= "Please enter the oracle database host name for Facts: "
	set /p FACTS_DB_PORT= "Please enter the oracle database port for Facts: "
	set /p FACTS_DB_NAME= "Please enter the oracle database name for Facts: "
	set /p FACTS_DBUSER= "Please enter the database user for Facts: "
	set /p FACTS_DBPASSWORD= "Please enter the database password for Facts database user: "
)
rem Trick Yellowfin silent installer to get around a bug
cd /D "%FACTS_BASE%"
mkdir appserver\bin
copy "%CATALINA_BASE%\bin\catalina.bat" appserver\bin
copy "%CATALINA_BASE%\bin\catalina.sh" appserver\bin
cd /D "%CONNEXO_DIR%"

echo Applying custom properties ...
copy "%FACTS_BASE_PROPERTIES%" custom.properties
echo. >> custom.properties
echo action.adminuser.username=%CONNEXO_ADMIN_ACCOUNT%>> custom.properties
echo action.adminuser.password=%CONNEXO_ADMIN_PASSWORD%>> custom.properties
echo option.licencefile=%INSTALLER_LICENSE:\=\\%>> custom.properties
echo option.db.hostname=%FACTS_DB_HOST%>> custom.properties
echo option.db.port=%FACTS_DB_PORT%>> custom.properties
echo option.db.dbname=%FACTS_DB_NAME%>> custom.properties
echo option.db.username=%FACTS_DBUSER%>> custom.properties
echo option.db.userpassword=%FACTS_DBPASSWORD%>> custom.properties
echo option.db.dbausername=%FACTS_DBUSER%>> custom.properties
echo option.db.dbapassword=%FACTS_DBPASSWORD%>> custom.properties
echo option.serverport=%FACTS_PORT%>> custom.properties
echo option.installpath=%FACTS_BASE:\=\\%>> custom.properties

"%JAVA_HOME%/bin/jar" -uvf partners\facts\facts.jar custom.properties

"%JAVA_HOME%/bin/java" -jar partners\facts\facts.jar -silent
if not exist "%FACTS_DIR%" mkdir "%FACTS_DIR%"
copy "%FACTS_BASE%\facts.war" "%FACTS_DIR%"
cd /D "%FACTS_DIR%"
"%JAVA_HOME%/bin/jar" -xvf facts.war
del facts.war
cd /D "%CONNEXO_DIR%"
del "%FACTS_BASE%\facts.war"
if exist "%FACTS_BASE%\appserver" rmdir /s /q "%FACTS_BASE%\appserver"

echo Connexo Facts successfully installed

:flow
set FLOW_DIR=%TOMCAT_BASE%\%TOMCAT_DIR%\webapps\flow
set FLOW_TABLESPACE=flow
set FLOW_DBUSER=flow
set FLOW_DBPASSWORD=flow
set DEMOWS_DIR=%TOMCAT_BASE%\%TOMCAT_DIR%\webapps\demows
echo.
echo Installing Connexo Flow ...
echo ==========================================================================
if NOT [%1] == [config] (
	set /p FLOW_JDBC_URL= "Please enter the database url for Connexo Flow (format: jdbc:oracle:thin:@dbHost:dbPort:dbSID): "
	set /p FLOW_DB_USER= "Please enter the database user for Connexo Flow: "
	set /p FLOW_DB_PASSWORD= "Please enter the database password for Connexo Flow: "

	for /f "tokens=* delims= " %%a in ("%jdbcUrl%") do set FLOW_JDBC_URL=%%a
	for /f "tokens=* delims= " %%a in ("%dbUserName%") do set FLOW_DB_USER=%%a
	for /f "tokens=* delims= " %%a in ("%dbPassword%") do set FLOW_DB_PASSWORD=%%a

	set /p SMTP_HOST= "Please enter the mail server host name: "
	set /p SMTP_PORT= "Please enter the mail server port: "
	set /p SMTP_USER= "Please enter the mail server user: "
	set /p SMTP_PASSWORD= "Please enter the mail server user password: "
)
IF "%SMTP_HOST%"=="" (
	set SMTP_HOST=""
)
IF "%SMTP_PORT%"=="" (
	set SMTP_PORT=""
)
IF "%SMTP_USER%"=="" (
	set SMTP_USER=""
)
IF "%SMTP_PASSWORD%"=="" (
	set SMTP_PASSWORD=""
)

if not exist "%FLOW_DIR%" mkdir "%FLOW_DIR%"
copy partners\flow\flow.war "%FLOW_DIR%"
cd /D "%FLOW_DIR%"
"%JAVA_HOME%\bin\jar" -xvf flow.war
del flow.war
cd /D "%CONNEXO_DIR%"

if not exist "%DEMOWS_DIR%" mkdir "%DEMOWS_DIR%"
copy partners\flow\demows.war "%DEMOWS_DIR%"
cd /D "%DEMOWS_DIR%"
"%JAVA_HOME%\bin\jar" -xvf demows.war
del demows.war
cd /D "%CONNEXO_DIR%"

copy partners\flow\processes.zip "%CATALINA_HOME%"
cd /D "%CATALINA_HOME%"
"%JAVA_HOME%\bin\jar" -xvf processes.zip
del processes.zip
cd /D "%CONNEXO_DIR%"

copy partners\flow\resources.properties "%CATALINA_HOME%\conf"
cscript //NoLogo "%CONNEXO_DIR%/bin/replace.vbs" "%CATALINA_HOME%\conf\resources.properties" ${jdbc} %FLOW_JDBC_URL%
cscript //NoLogo "%CONNEXO_DIR%/bin/replace.vbs" "%CATALINA_HOME%\conf\resources.properties" ${user} %FLOW_DB_USER%
cscript //NoLogo "%CONNEXO_DIR%/bin/replace.vbs" "%CATALINA_HOME%\conf\resources.properties" ${password} %FLOW_DB_PASSWORD%

copy partners\flow\CustomWorkItemHandlers.conf .
cscript //NoLogo "%CONNEXO_DIR%/bin/replace.vbs" CustomWorkItemHandlers.conf ${host} %SMTP_HOST%
cscript //NoLogo "%CONNEXO_DIR%/bin/replace.vbs" CustomWorkItemHandlers.conf ${port} %SMTP_PORT%
cscript //NoLogo "%CONNEXO_DIR%/bin/replace.vbs" CustomWorkItemHandlers.conf ${user} %SMTP_USER%
cscript //NoLogo "%CONNEXO_DIR%/bin/replace.vbs" CustomWorkItemHandlers.conf ${password} %SMTP_PASSWORD%
copy CustomWorkItemHandlers.conf "%FLOW_DIR%\WEB-INF\classes\META-INF"
del CustomWorkItemHandlers.conf

copy partners\flow\SendSomeoneToInspect.bpmn .
cscript //NoLogo "%CONNEXO_DIR%/bin/replace.vbs" SendSomeoneToInspect.bpmn ${host} localhost
cscript //NoLogo "%CONNEXO_DIR%/bin/replace.vbs" SendSomeoneToInspect.bpmn ${port} TOMCAT_HTTP_PORT
"%JAVA_HOME%\bin\jar" -uvf "%CATALINA_HOME%\repositories\kie\org\jbpm\sendsomeone\1.0\sendsomeone-1.0.jar" SendSomeoneToInspect.bpmn
del SendSomeoneToInspect.bpmn
cd /D "%CONNEXO_DIR%"

echo Connexo Flow successfully installed


if /i {%CONNEXO_SERVICE%}=={y} (goto :start-connexo)
if /i {%CONNEXO_SERVICE%}=={yes} (goto :start-connexo)
goto :start-tomcat
:start-connexo
echo Starting Connexo
echo ==========================================================================
echo org.osgi.service.http.port=%CONNEXO_HTTP_PORT%>> %config_file%
echo com.elster.jupiter.bpm.url=http://localhost:%TOMCAT_HTTP_PORT%/flow>> %config_file%
echo com.elster.jupiter.yellowfin.url=http://localhost:%TOMCAT_HTTP_PORT%/facts>> %config_file%
sc start Connexo

:start-tomcat
echo Starting Apache Tomcat 7 ...
echo ==========================================================================
sc start ConnexoTomcat

:wait_for_service_start
timeout /t 10 /nobreak > NUL

:starting_service
sc query ConnexoTomcat | find "STATE" | find "RUNNING" > NUL
if errorlevel 1 goto :wait_for_service_start
cd /D "%CONNEXO_DIR%"

:reports
echo Installing Connexo Facts content...
cd /D "%CONNEXO_DIR%"
"%JAVA_HOME%/bin/java" -jar partners/facts/EncryptPassword.jar %dbPassword%>password.tmp
set /p ENCRYPTED_PASSWORD= < password.tmp
del password.tmp

copy partners\facts\open-reports.xml datasource.xml
cscript //NoLogo "%CONNEXO_DIR%/bin/replace.vbs" datasource.xml ${jdbc} %jdbcUrl%
cscript //NoLogo "%CONNEXO_DIR%/bin/replace.vbs" datasource.xml ${user} %dbUserName%
cscript //NoLogo "%CONNEXO_DIR%/bin/replace.vbs" datasource.xml ${password} %ENCRYPTED_PASSWORD%
cscript //NoLogo "%CONNEXO_DIR%/bin/replace.vbs" datasource.xml ${host} %FACTS_DB_HOST%
cscript //NoLogo "%CONNEXO_DIR%/bin/replace.vbs" datasource.xml ${port} %FACTS_DB_PORT%
cscript //NoLogo "%CONNEXO_DIR%/bin/replace.vbs" datasource.xml ${instance} %FACTS_DB_NAME%

"%JAVA_HOME%/bin/java" -cp partners\facts\yellowfin.installer.jar com.elster.jupiter.install.reports.OpenReports datasource.xml http://localhost:%TOMCAT_HTTP_PORT%/facts %CONNEXO_ADMIN_ACCOUNT% %CONNEXO_ADMIN_PASSWORD%
del datasource.xml

:processes
cd /D "%CONNEXO_DIR%"
echo Installing Connexo Flow content...
for /f "delims=" %%a in ('dir /B bundles\com.elster.jupiter.bpm-*.jar') do @set BPM_BUNDLE=%%a
"%JAVA_HOME%/bin/java" -cp bundles\%BPM_BUNDLE% com.elster.jupiter.bpm.impl.ProcessDeployer http://localhost:%TOMCAT_HTTP_PORT%/flow %CONNEXO_ADMIN_ACCOUNT% %CONNEXO_ADMIN_PASSWORD%

cd /D "%CONNEXO_DIR%"

:END