@ECHO ON
set CLASSPATH="../bundles/*"
rem set VM_OPTIONS=-Dsun.java2d.d3d=false -Djava.library.path="lib" -Dos.name="Windows XP" -Dservername=ConnexoMobile
%JAVA_HOME%\bin\javaw %VM_OPTIONS% -cp %CLASSPATH% com.elster.jupiter.launcher.ConnexoLauncher

