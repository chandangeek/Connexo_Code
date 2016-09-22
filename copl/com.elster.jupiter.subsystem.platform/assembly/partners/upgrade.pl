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

my $JAVA_HOME="";
my $FLOW_JDBC_URL, my $FLOW_DB_USER, my $FLOW_DB_PASSWORD;

my $SCRIPT_DIR=dirname(abs_path($0));

sub read_config(){
    open(my $FH,"< $SCRIPT_DIR/upgrade.cmd") or die "Could not open $SCRIPT_DIR/upgrade.cmd: $!";
    while (my $row = <$FH>) {
        $row=~s/set (.*)/$1/;
        chomp($row);
        if ( "$row" ne "") {
            my @val=split('=',$row);
            if ( "$val[0]" eq "JAVA_HOME" )                {$JAVA_HOME=$val[1];}
            if ( "$val[0]" eq "FLOW_JDBC_URL" )            {$FLOW_JDBC_URL=$val[1];}
            if ( "$val[0]" eq "FLOW_DB_USER" )             {$FLOW_DB_USER=$val[1];}
            if ( "$val[0]" eq "FLOW_DB_PASSWORD" )         {$FLOW_DB_PASSWORD=$val[1];}
            if ( "$val[0]" eq "UPGRADE_FROM" )             {$UPGRADE_FROM=$val[1];}
			if ( "$val[0]" eq "UPGRADE_TO" )               {$UPGRADE_TO=$val[1];}
        }
    }
    close($FH);
}

sub upgrade_facts {
    # No upgrade required from 10.1 to 10.2
}

sub get_bpm_bundle(){
    opendir(DIR,"$SCRIPT_DIR/../bundles");
    my @files = grep(/com\.elster\.jupiter\.bpm-.*\.jar$/,readdir(DIR));
    closedir(DIR);

    my $bundle;
    foreach my $file (@files) {
        $bundle="$file";
    }
    return $bundle;
}

sub upgrade_flow {
    my $BPM_BUNDLE = get_bpm_bundle();
    chdir "$SCRIPT_DIR/..";

    # For upgrading from 10.1 to 10.2, Flow database needs to be upgraded
    system("\"$JAVA_HOME/bin/java\" -cp \"bundles/$BPM_BUNDLE\";\"partners/flow/ojdbc6-11.2.0.3.jar\" com.elster.jupiter.bpm.install.FlowUpgrader $UPGRADE_TO $FLOW_JDBC_URL $FLOW_DB_USER $FLOW_DB_PASSWORD $SCRIPT_DIR/flow/flow-upgrade-10.1-to-10.2.xml upgrade") or die "Cannot execute Flow upgrade script!";
}

# Main
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