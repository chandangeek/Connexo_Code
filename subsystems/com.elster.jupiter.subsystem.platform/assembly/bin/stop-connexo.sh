#!/bin/sh
PID=`ps auxw | pgrep start-connexo`
for p in $PID
do
	pkill -P $p
done

