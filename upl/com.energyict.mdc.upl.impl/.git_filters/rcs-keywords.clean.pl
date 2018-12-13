#!/usr/bin/perl -p
use POSIX qw(strftime);
$datestr = strftime "%Y-%m-%d %H:%M:%S %z (%a, %d %b %Y)",localtime;
s/\$Date[^\$]*\$/\$Date: $datestr\$/;
