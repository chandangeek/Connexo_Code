#!/usr/bin/perl -p

$date=localtime();
s/\$Date[^\$]*\$/\$Date: $date\$/;
