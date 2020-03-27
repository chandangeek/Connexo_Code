#!/bin/sh
JAVA_HOME=/etc/alternatives/java_sdk_openjdk

CXO=${CONNEXO_DIR}

VMFILE=$(<$CXO/conf/Connexo.vmoptions)

if [  "$2"  = "--debug" ]
then
   echo "Adding ../conf/Connexo-debug.vmoptions "
   DEBUG_VMFILE=$(<$CXO/conf/Connexo-debug.vmoptions)
   echo "Removing felix-cache"
   rm -rf $CXO/felix-cache
else
   DEBUG_VMFILE=""
fi

CLASSPATH=.:$CXO/conf/

for i in `ls $CXO/lib/*.jar`
do
        CLASSPATH=$CLASSPATH:$i
done


export VM_OPTIONS="$VMFILE $DEBUG_VMFILE"
export LD_LIBRARY_PATH=$CXO/bin/:$CXO/lib:$CXO/conf:$LD_LIBRARY_PATH
export CLASSPATH=$CLASSPATH

# Various  Java distributions use different options, set them all
export JAVA_OPTS=$VM_OPTIONS
export JAVA_TOOL_OPTIONS=$VM_OPTIONS
export _JAVA_OPTIONS=$VM_OPTIONS

cd $CXO


"$JAVA_HOME/bin/java" -cp "$CLASSPATH" com.elster.jupiter.launcher.ConnexoLauncher "$1"
