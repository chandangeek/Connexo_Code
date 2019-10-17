@ECHO OFF
set CLASSPATH="../bundles/*"
set VM_OPTIONS=-Dsun.java2d.d3d=false -Dos.name="Windows XP" -Dservername=ConnexoMobile
start %JAVA_HOME%\bin\java %VM_OPTIONS% -cp %CLASSPATH% com.elster.jupiter.launcher.ConnexoLauncher

