#!/usr/bin/perl
use strict;
#use warnings;
use Cwd;
use Cwd 'abs_path';
use File::Basename;

my $UPGRADE_FROM="10.1";
my $UPGRADE_TO="10.2";
my $UPGRADE_FACTS="yes";
my $UPGRADE_FLOW="yes";

my $OS="$^O";
my $JAVA_HOME="";
my $FLOW_JDBC_URL, my $FLOW_DB_USER, my $FLOW_DB_PASSWORD;

my $SCRIPT_DIR=dirname(abs_path($0));

sub read_config(){
    $JAVA_HOME = $ARGV[0];
    $UPGRADE_FROM = $ARGV[1];
    $UPGRADE_TO = $ARGV[2];
    $FLOW_JDBC_URL = $ARGV[3];
    $FLOW_DB_USER = $ARGV[4];
    $FLOW_DB_PASSWORD = $ARGV[5];
}

sub upgrade_facts {
    # No upgrade required from 10.1 to 10.2
}

sub upgrade_flow {
    chdir "$SCRIPT_DIR/..";
    # For upgrading from 10.1 to 10.2, Flow database needs to be upgraded
    if ("$OS" eq "MSWin32" || "$OS" eq "MSWin64") {
        # classpath separator on Windows is ;
        system("\"$JAVA_HOME/bin/java\" -cp \"lib/com.elster.jupiter.installer.util.jar;partners/flow/ojdbc6-11.2.0.3.jar\" com.elster.jupiter.bpm.install.FlowUpgrader $UPGRADE_TO $FLOW_JDBC_URL $FLOW_DB_USER $FLOW_DB_PASSWORD $SCRIPT_DIR/flow/flow-upgrade-10.1-to-10.2.xml upgrade") == 0 or die "Cannot execute Flow upgrade script!";
    } else {
        # classpath separator on Linux is :
        system("\"$JAVA_HOME/bin/java\" -cp \"lib/com.elster.jupiter.installer.util.jar:partners/flow/ojdbc6-11.2.0.3.jar\" com.elster.jupiter.bpm.install.FlowUpgrader $UPGRADE_TO $FLOW_JDBC_URL $FLOW_DB_USER $FLOW_DB_PASSWORD $SCRIPT_DIR/flow/flow-upgrade-10.1-to-10.2.xml upgrade") == 0 or die "Cannot execute Flow upgrade script!";
    }
}

# Main
if ($#ARGV + 1 != 6) {
    print "Invalid syntax when calling upgrade.pl\n";
    print "Usage: upgrade.pl java_home old_version new_version flow_jdbc flow_user flow_password\n";
    exit 1;
}

print "Partners upgrade started\n";
read_config();
if($UPGRADE_FACTS){
    print "Upgrading Connexo Facts...\n";
    upgrade_facts();
}
if($UPGRADE_FLOW){
    print "Upgrading Connexo Flow...\n";
    upgrade_flow();
}
print "Partners upgrade completed\n";