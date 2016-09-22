#!/usr/bin/perl
#
# Usage:
#    .git_filter/rcs-keywords.smudge file_path < file_contents
# 
# To add keyword expansion:
#    <project>/.gitattributes                    - *.c filter=rcs-keywords
#    <project>/.git_filters/rcs-keywords.smudge  - copy this file to project
#    <project>/.git_filters/rcs-keywords.clean   - copy companion to project
#    ~/.gitconfig                                - add [filter] lines below
#
# [filter "rcs-keywords"]
#	clean  = .git_filters/rcs-keywords.clean
#	smudge = .git_filters/rcs-keywords.smudge %f
#

$path = shift;
$path =~ /.*\/(.*)/;
$filename = $1;

if (0 == length($filename)) {
    $filename = $path;
}

# Need to grab filename and to use git log for this to be accurate.
$rev = `git log -- $path | head -n 3`;
$rev =~ /^Date:\s*(.*)\s*$/m;
$date = $1;

while (<STDIN>) {
    s/\$Date[^\$]*\$/\$Date: $date \$/;
} continue {
    print or die "-p destination: $!\n";
}
