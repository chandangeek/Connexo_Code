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
my $HOST_NAME, my $TOMCAT_HTTP_PORT;
my $jdbcUrl, my $dbUserName, my $dbPassword;
my $FACTS_DB_HOST, my $FACTS_DB_PORT, my $FACTS_DB_NAME, my $FACTS_DBUSER, my $FACTS_DBPASSWORD;
my $FLOW_JDBC_URL, my $FLOW_DB_USER, my $FLOW_DB_PASSWORD;

my $CONNEXO_ADMIN_ACCOUNT="admin";
my $CONNEXO_ADMIN_PASSWORD;
my $TOMCAT_ADMIN_PASSWORD="D3moAdmin";

my $ACTIVATE_SSO="no";

my $SCRIPT_DIR=dirname(abs_path($0));
my $TOMCAT_DIR="tomcat";
my $TOMCAT_BASE="$SCRIPT_DIR/partners";

sub read_config(){
    open(my $FH,"< $SCRIPT_DIR/upgrade.cmd") or die "Could not open $SCRIPT_DIR/upgrade.cmd: $!";
    while (my $row = <$FH>) {
        $row=~s/set (.*)/$1/;
        chomp($row);
        if ( "$row" ne "") {
            my @val=split('=',$row);
            if ( "$val[0]" eq "JAVA_HOME" )                {$JAVA_HOME=$val[1];}
            if ( "$val[0]" eq "HOST_NAME" )                {$HOST_NAME=$val[1];}
            if ( "$val[0]" eq "TOMCAT_HTTP_PORT" )         {$TOMCAT_HTTP_PORT=$val[1];}
            if ( "$val[0]" eq "jdbcUrl" )                  {$jdbcUrl=$val[1];}
            if ( "$val[0]" eq "dbUserName" )               {$dbUserName=$val[1];}
            if ( "$val[0]" eq "dbPassword" )               {$dbPassword=$val[1];}
            if ( "$val[0]" eq "FACTS_DB_HOST" )            {$FACTS_DB_HOST=$val[1];}
            if ( "$val[0]" eq "FACTS_DB_PORT" )            {$FACTS_DB_PORT=$val[1];}
            if ( "$val[0]" eq "FACTS_DB_NAME" )            {$FACTS_DB_NAME=$val[1];}
            if ( "$val[0]" eq "FACTS_DBUSER" )             {$FACTS_DBUSER=$val[1];}
            if ( "$val[0]" eq "FACTS_DBPASSWORD" )         {$FACTS_DBPASSWORD=$val[1];}
            if ( "$val[0]" eq "FLOW_JDBC_URL" )            {$FLOW_JDBC_URL=$val[1];}
            if ( "$val[0]" eq "FLOW_DB_USER" )             {$FLOW_DB_USER=$val[1];}
            if ( "$val[0]" eq "FLOW_DB_PASSWORD" )         {$FLOW_DB_PASSWORD=$val[1];}
            if ( "$val[0]" eq "CONNEXO_ADMIN_ACCOUNT" )    {$CONNEXO_ADMIN_ACCOUNT=$val[1];}
            if ( "$val[0]" eq "CONNEXO_ADMIN_PASSWORD" )   {$CONNEXO_ADMIN_PASSWORD=$val[1];}
            if ( "$val[0]" eq "TOMCAT_ADMIN_PASSWORD" )    {$TOMCAT_ADMIN_PASSWORD=$val[1];}
            if ( "$val[0]" eq "ACTIVATE_SSO" )             {$ACTIVATE_SSO=$val[1];}
            if ( "$val[0]" eq "SERVICE_VERSION" )          {$SERVICE_VERSION=$val[1];}
        }
    }
    close($FH);
}

sub stop_tomcat(){
    # stop tomcat
    print "Stopping ConnexoTomcat service\n";
    if ("$OS" eq "MSWin32" || "$OS" eq "MSWin64") {
        system("sc stop ConnexoTomcat$SERVICE_VERSION");
    } else {
        system("/sbin/service ConnexoTomcat$SERVICE_VERSION stop");
    }
}

sub start_tomcat(){
    # start tomcat
    print "\nStarting ConnexoTomcat service\n";
    if ("$OS" eq "MSWin32" || "$OS" eq "MSWin64") {
        system("sc start ConnexoTomcat$SERVICE_VERSION");
    } else {
        system("/sbin/service ConnexoTomcat$SERVICE_VERSION start");
    }
}

sub upgrade_tomcat(){
    # Copy the new Tomcat installation
}

sub upgrade_facts {
    # No upgrade required from 10.1 to 10.2
}

sub upgrade_facts_content(){
    # For upgrading from 10.1 to 10.2, Facts reports need to be updated
    my $ENCRYPTED_PASSWORD=`"$JAVA_HOME/bin/java" -jar \"$SCRIPT_DIR/facts/EncryptPassword.jar\" $dbPassword`;

    copy("$SCRIPT_DIR/facts/open-reports.xml","$SCRIPT_DIR/../datasource.xml");
    replace_in_file("$SCRIPT_DIR/../datasource.xml",'\${jdbc}',"$jdbcUrl");
    replace_in_file("$SCRIPT_DIR/../datasource.xml",'\${user}',"$dbUserName");
    replace_in_file("$SCRIPT_DIR/../datasource.xml",'\${password}',"$ENCRYPTED_PASSWORD");
    replace_in_file("$SCRIPT_DIR/../datasource.xml",'\${host}',"$FACTS_DB_HOST");
    replace_in_file("$SCRIPT_DIR/../datasource.xml",'\${port}',"$FACTS_DB_PORT");
    replace_in_file("$SCRIPT_DIR/../datasource.xml",'\${instance}',"$FACTS_DB_NAME");

    chdir "$SCRIPT_DIR/..";
    system("\"$JAVA_HOME/bin/java\" -cp \"$SCRIPT_DIR/facts/yellowfin.installer.jar\" com.elster.jupiter.install.reports.OpenReports datasource.xml http://$HOST_NAME:$TOMCAT_HTTP_PORT/facts $CONNEXO_ADMIN_ACCOUNT $CONNEXO_ADMIN_PASSWORD") == 0 or die "Upgrading Connexo Facts content failed: $?";
    unlink("$SCRIPT_DIR/../datasource.xml");
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
    # Copy the new Connexo Flow web app
    my $FLOW_DIR="$TOMCAT_BASE/$TOMCAT_DIR/webapps/flow";
    if (!-d "$FLOW_DIR") { make_path("$FLOW_DIR"); }
    copy("$CONNEXO_DIR/partners/flow/flow.war","$FLOW_DIR/flow.war");
    chdir "$FLOW_DIR";
    print "Extracting flow.war\n";
    system("\"$JAVA_HOME/bin/jar\" -xf flow.war") == 0 or die "$JAVA_HOME/bin/jar -xvf flow.war failed: $?";
    unlink("$FLOW_DIR/flow.war");

    my $BPM_BUNDLE = get_bpm_bundle();
    chdir "$SCRIPT_DIR/..";

    # For upgrading from 10.1 to 10.2, Flow database needs to be upgraded
    system("\"$JAVA_HOME/bin/java\" -cp \"bundles/$BPM_BUNDLE\" com.elster.jupiter.bpm.impl.FlowUpgrader $FLOW_JDBC_URL $FLOW_DB_USER $FLOW_DB_PASSWORD $SCRIPT_DIR/flow/flow-upgrade-10.1-to-10.2.sql");
}

sub upgrade_flow_content(){
    my $BPM_BUNDLE = get_bpm_bundle();
    chdir "$SCRIPT_DIR/..";

    # For upgrading from 10.1 to 10.2, additional Flow processes need to be deployed
    print "\nDeploy MDC processes...\n";
    dircopy("$SCRIPT_DIR/flow/mdc/kie", "$TOMCAT_BASE/$TOMCAT_DIR/repositories/kie");
    my $mdcfile = "$SCRIPT_DIR/flow/mdc/processes.csv";
    if(-e $mdcfile){
        open(INPUT, $mdcfile);
        my $line = <INPUT>; # header
        while($line = <INPUT>)
        {
            chomp($line);
            my ($name,$deploymentid)  = split(';', $line);
            if ("$ACTIVATE_SSO" eq "yes") {
                system("\"$JAVA_HOME/bin/java\" -cp \"bundles/$BPM_BUNDLE\" com.elster.jupiter.bpm.impl.ProcessDeployer deployProcess $CONNEXO_ADMIN_ACCOUNT $CONNEXO_ADMIN_PASSWORD http://$HOST_NAME:$TOMCAT_HTTP_PORT/flow $deploymentid") == 0 or die "Installing Connexo Flow content failed: $?";
            } else {
                system("\"$JAVA_HOME/bin/java\" -cp \"bundles/$BPM_BUNDLE\" com.elster.jupiter.bpm.impl.ProcessDeployer deployProcess $CONNEXO_ADMIN_ACCOUNT $TOMCAT_ADMIN_PASSWORD http://$HOST_NAME:$TOMCAT_HTTP_PORT/flow $deploymentid") == 0 or die "Installing Connexo Flow content failed: $?";
            }
            sleep 2;
        }
        close(INPUT);
    }

    print "\nDeploy INSIGHT processes...\n";
    dircopy("$CONNEXO_DIR/partners/flow/insight/kie", "$TOMCAT_BASE/$TOMCAT_DIR/repositories/kie");
    my $insightfile = "$CONNEXO_DIR/partners/flow/insight/processes.csv";
    if(-e $insightfile){
        open(INPUT, $insightfile);
        my $line = <INPUT>; # header
        while($line = <INPUT>)
        {
            chomp($line);
            my ($name,$deploymentid)  = split(';', $line);
            if ("$ACTIVATE_SSO" eq "yes") {
                system("\"$JAVA_HOME/bin/java\" -cp \"bundles/$BPM_BUNDLE\" com.elster.jupiter.bpm.impl.ProcessDeployer deployProcess $CONNEXO_ADMIN_ACCOUNT $CONNEXO_ADMIN_PASSWORD http://$HOST_NAME:$TOMCAT_HTTP_PORT/flow $deploymentid") == 0 or die "Installing Connexo Flow content failed: $?";
            } else {
                system("\"$JAVA_HOME/bin/java\" -cp \"bundles/$BPM_BUNDLE\" com.elster.jupiter.bpm.impl.ProcessDeployer deployProcess $CONNEXO_ADMIN_ACCOUNT $TOMCAT_ADMIN_PASSWORD http://$HOST_NAME:$TOMCAT_HTTP_PORT/flow $deploymentid") == 0 or die "Installing Connexo Flow content failed: $?";
            }
            sleep 2;
        }
        close(INPUT);
    }
}

# Main
print "Partners upgrade started\n";
read_config();
stop_tomcat();
upgrade_tomcat();
if($UPGRADE_FACTS){
    print "Upgrading Connexo Facts...\n";
    upgrade_facts();
}
if($UPGRADE_FLOW){
    print "Upgrading Connexo Flow...\n";
    upgrade_flow();
}
start_tomcat();
if($UPGRADE_FACTS){
    print "Upgrading Connexo Facts content...\n";
    upgrade_facts_content();
}
if($UPGRADE_FLOW){
    print "Upgrading Connexo Flow content...\n";
    upgrade_flow_content();
}
print "Partners upgrade completed\n";