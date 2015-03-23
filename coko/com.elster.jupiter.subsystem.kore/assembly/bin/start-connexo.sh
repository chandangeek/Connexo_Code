#!/bin/sh
JAVA_HOME=/usr/lib/jvm/jdk1.8.0
export VM_OPTIONS="-Djava.util.logging.config.file=/usr/local/bin/connexo/conf/logging.properties"
CLASSPATH=.
for i in `ls /usr/local/bin/connexo/lib/*.jar`
do
	CLASSPATH=$CLASSPATH:$i
done
cd /usr/local/bin/connexo
"$JAVA_HOME/bin/java" $VM_OPTIONS -cp "$CLASSPATH" com.elster.jupiter.launcher.ConnexoLauncher

