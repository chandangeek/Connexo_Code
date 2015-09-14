#!/bin/sh
PID=`ps auxw|pgrep start-connexo`
pkill -P $PID

