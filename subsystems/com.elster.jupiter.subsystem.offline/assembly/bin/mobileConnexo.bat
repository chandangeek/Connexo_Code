@ECHO OFF
set CLASSPATH="../bundles/*"
set VM_OPTIONS=-Dsun.java2d.d3d=false -Dos.name="Windows XP" -Dservername=RO18LTC29G9H2-Mobile
%JAVA_HOME%\bin\java %VM_OPTIONS% -cp %CLASSPATH% com.elster.jupiter.launcher.ConnexoLauncher

