#!/bin/sh
PID=`ps auxw | pgrep start-connexo`

for p in $PID
do
        echo -n "- killing $p ... "
        pkill -P $p
        sudo kill -9 $p
        echo "done"
done

PID=`ps auxw | pgrep ConnexoLauncher`
for p in $PID
do
        echo -n "- killing $p ... "
        pkill -P $p
        sudo kill -9 $p
        echo "done"
done
