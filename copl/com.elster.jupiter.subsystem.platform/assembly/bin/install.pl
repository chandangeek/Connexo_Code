#!/usr/bin/perl
use strict;
#use warnings;
use Cwd;
use Cwd 'abs_path';
use File::Basename;
use File::Path qw(rmtree);
use File::Path qw(make_path);
use File::Copy;
use File::Copy::Recursive qw(dircopy);
use Socket;
use Sys::Hostname;
use Archive::Zip;


# Define global variables
#$ENV{JAVA_HOME}="/usr/lib/jvm/jdk1.8.0";
my $INSTALL_VERSION="v20170210";
my $OS="$^O";
my $JAVA_HOME="";
my $CURRENT_DIR=getcwd;
my $SCRIPT_DIR=dirname(abs_path($0));
my $CONNEXO_DIR="$SCRIPT_DIR/..";

my $parameter_file=0;
my $install=1;
my $cmd_line=0;
my $help=0;

my $config_file="$CONNEXO_DIR/conf/config.properties";
my $config_cmd="config.cmd";
my $SERVICE_VERSION="";

my $INSTALL_CONNEXO="yes";
my $INSTALL_FACTS="yes";
my $INSTALL_FLOW="yes";
my $INSTALL_WSO2IS="yes";
my $ACTIVATE_SSO="no";
my $APACHE_PATH;
my $HTTPS="no";
my $UPGRADE="no";
my $UPGRADE_PATH;
my $UPGRADE_OLD_SERVICE_VERSION="";

my $HOST_NAME, my $CONNEXO_HTTP_PORT, my $TOMCAT_HTTP_PORT;
my $jdbcUrl, my $dbUserName, my $dbPassword, my $CONNEXO_SERVICE, my $CONNEXO_URL;
my $FACTS_DB_HOST, my $FACTS_DB_PORT, my $FACTS_DB_NAME, my $FACTS_DBUSER, my $FACTS_DBPASSWORD, my $FACTS_LICENSE;
my $FLOW_JDBC_URL, my $FLOW_DB_USER, my $FLOW_DB_PASSWORD;

my $TOMCAT_DIR="tomcat";
my $TOMCAT_BASE="$CONNEXO_DIR/partners"; 
my $TOMCAT_ZIP="tomcat-7.0.59";
my $CATALINA_BASE="$TOMCAT_BASE/$TOMCAT_DIR";
my $CATALINA_HOME=$CATALINA_BASE;
$ENV{"CATALINA_HOME"}=$CATALINA_HOME;
my $TOMCAT_SHUTDOWN_PORT="8006";
my $TOMCAT_AJP_PORT, my $TOMCAT_SSH_PORT, my $TOMCAT_DAEMON_PORT;
my $FLOW_URL;

my $CONNEXO_ADMIN_ACCOUNT="admin";
my $CONNEXO_ADMIN_PASSWORD;
my $TOMCAT_ADMIN_PASSWORD="D3moAdmin";


# Function Definitions
sub check_root {
    if ( $> != 0 ) {
        print "Please run this script as administrator\n";
        exit (0);
    }
    if ("$OS" eq "MSWin32" || "$OS" eq "MSWin64") {
        my $output=`C:/Windows/system32/net.exe session 2>&1`;
        my $return_code = $? >> 8;
        if ($return_code != 0) {
            print "Please run this script as administrator\n";
            exit (0);
        }
    }
}

sub check_java8 {
    if ("$JAVA_HOME" eq "") {
        $JAVA_HOME=$ENV{"JAVA_HOME"};
    }
    if (-d "$JAVA_HOME") {
        $ENV{"JAVA_HOME"}=$JAVA_HOME;
    } else {
        print "The path defined in JAVA_HOME does not exist (path=$JAVA_HOME)\n";
        exit (0);
    }

    if ( !defined $JAVA_HOME || "$JAVA_HOME" eq "" ) {
        print "Please define JAVA_HOME on your system\n";
        exit (0);
    } else {
        my $JAVA_VERSION=`"$JAVA_HOME/bin/java" -fullversion 2>&1`;
        $JAVA_VERSION=~s/(.*)"(\d).(\d).(.*)/$2.$3/;
        chomp($JAVA_VERSION);
        if ( "$JAVA_VERSION" ne "1.8" ) {
            print "Please install Java 8\n";
            exit (0);
        }
    }
}

sub check_create_users {
    if ("$OS" eq "linux") {
        if ("$INSTALL_CONNEXO" eq "yes") {
            if (`cat /etc/passwd|grep connexo:` eq "") {
                system("useradd -U -r -m connexo") == 0 or die "system useradd -U -r -m connexo failed: $?";
            }
        }
        if (("$INSTALL_FACTS" eq "yes") || ("$INSTALL_FLOW" eq "yes")) {
            if (`cat /etc/passwd|grep tomcat:` eq "") {
                system("useradd -U -r -m tomcat") == 0 or die "system useradd -U -r -m tomcat failed: $?";
            }
        }
    }
}

sub read_args {
    my $num_args=$#ARGV + 1;
    for (my $i=0; $i < $num_args; $i++) {
        if ($ARGV[$i] eq "--config") {
            if (-e $config_cmd) {
                $parameter_file=1;
            }
        }
        if ($ARGV[$i] eq "--uninstall") {
            $install=0;
        }
        if ($ARGV[$i] eq "--uninstallcmd") {
            $install=0;
            $cmd_line=1;
        }
        if ($ARGV[$i] eq "--help") {
            $help=1;
        }
        if ($ARGV[$i] eq "--version") {
            print "\n    Installation script version $INSTALL_VERSION\n";
            exit (0);
        }
    }
}

sub read_config {
    if ( $parameter_file ) {
        open(my $FH,"< $config_cmd") or die "Could not open $config_cmd: $!";
        while (my $row = <$FH>) {
            $row=~s/set (.*)/$1/;
            chomp($row);
            if ( "$row" ne "") {
                my @val=split('=',$row);
                if ( "$val[0]" eq "JAVA_HOME" )                     {$JAVA_HOME=$val[1];}
                if ( "$val[0]" eq "HOST_NAME" )                     {$HOST_NAME=$val[1];}
                if ( "$val[0]" eq "ADMIN_PASSWORD" )                {$CONNEXO_ADMIN_PASSWORD=$val[1];}
                if ( "$val[0]" eq "CONNEXO_HTTP_PORT" )             {$CONNEXO_HTTP_PORT=$val[1];}
                if ( "$val[0]" eq "TOMCAT_HTTP_PORT" )              {$TOMCAT_HTTP_PORT=$val[1];}
                if ( "$val[0]" eq "SERVICE_VERSION" )               {$SERVICE_VERSION=$val[1];}
                if ( "$val[0]" eq "INSTALL_CONNEXO" )               {$INSTALL_CONNEXO=$val[1];}
                if ( "$val[0]" eq "UPGRADE" )                       {$UPGRADE=$val[1];}
                if ( "$val[0]" eq "UPGRADE_PATH" )                  {$UPGRADE_PATH=$val[1];}
                if ( "$val[0]" eq "UPGRADE_OLD_SERVICE_VERSION" )   {$UPGRADE_OLD_SERVICE_VERSION=$val[1];}
                if ( "$val[0]" eq "INSTALL_FACTS" )                 {$INSTALL_FACTS=$val[1];}
                if ( "$val[0]" eq "INSTALL_FLOW" )                  {$INSTALL_FLOW=$val[1];}
                if ( "$val[0]" eq "INSTALL_WSO2IS" )                {$INSTALL_WSO2IS=$val[1];}
                if ( "$val[0]" eq "ACTIVATE_SSO" )                  {$ACTIVATE_SSO=$val[1];}
                if ( "$val[0]" eq "APACHE_PATH" )                   {$APACHE_PATH=$val[1];}
                if ( "$val[0]" eq "HTTPS" )                         {$HTTPS=$val[1];}
                if ( "$val[0]" eq "jdbcUrl" )                       {$jdbcUrl=$val[1];}
                if ( "$val[0]" eq "dbUserName" )                    {$dbUserName=$val[1];}
                if ( "$val[0]" eq "dbPassword" )                    {$dbPassword=$val[1];}
                if ( "$val[0]" eq "CONNEXO_SERVICE" )               {$CONNEXO_SERVICE=$val[1];}
                if ( "$val[0]" eq "FACTS_DB_HOST" )                 {$FACTS_DB_HOST=$val[1];}
                if ( "$val[0]" eq "FACTS_DB_PORT" )                 {$FACTS_DB_PORT=$val[1];}
                if ( "$val[0]" eq "FACTS_DB_NAME" )                 {$FACTS_DB_NAME=$val[1];}
                if ( "$val[0]" eq "FACTS_DBUSER" )                  {$FACTS_DBUSER=$val[1];}
                if ( "$val[0]" eq "FACTS_DBPASSWORD" )              {$FACTS_DBPASSWORD=$val[1];}
                if ( "$val[0]" eq "FACTS_LICENSE" )                 {$FACTS_LICENSE=$val[1];}
                if ( "$val[0]" eq "FLOW_JDBC_URL" )                 {$FLOW_JDBC_URL=$val[1];}
                if ( "$val[0]" eq "FLOW_DB_USER" )                  {$FLOW_DB_USER=$val[1];}
                if ( "$val[0]" eq "FLOW_DB_PASSWORD" )              {$FLOW_DB_PASSWORD=$val[1];}
            }
        }
        close($FH);
        check_java8();
    } else {
        print "Parameters input\n";
        print "------------------\n";
        print "Please enter the path to your JAVA_HOME (leave empty to use the system variable): ";
        chomp($JAVA_HOME=<STDIN>);
        check_java8();
        print "Please enter the hostname (leave empty to use the system variable): ";
        chomp($HOST_NAME=<STDIN>);
        while (("$CONNEXO_ADMIN_PASSWORD" eq "") || ("$CONNEXO_ADMIN_PASSWORD" eq "admin")) {
            print "Please enter the admin password (different from \"admin\"): ";
            chomp($CONNEXO_ADMIN_PASSWORD=<STDIN>);
        }
        print "Are you performing an upgrade of an existing installation: (yes/no) ";
        chomp($UPGRADE=<STDIN>);
        if ("$UPGRADE" eq "yes") {
            print "Please enter the path containing the upgrade zip-file: ";
            chomp($UPGRADE_PATH=<STDIN>);
        }
						    
        print "Do you want to install Connexo: (yes/no)";
        chomp($INSTALL_CONNEXO=<STDIN>);
        if ("$INSTALL_CONNEXO" eq "yes") {
            print "Please enter the database url (format: jdbc:oracle:thin:\@dbHost:dbPort:dbSID): ";
            chomp($jdbcUrl=<STDIN>);
            print "Please enter the database user: ";
            chomp($dbUserName=<STDIN>);
            print "Please enter the database password: ";
            chomp($dbPassword=<STDIN>);
            print "Please enter the Connexo http port: ";
            chomp($CONNEXO_HTTP_PORT=<STDIN>);
            print "Do you want to install Connexo as a daemon: (yes/no) ";
            chomp($CONNEXO_SERVICE=<STDIN>);
        }
        
        print "\n";
        print "Do you want to install Facts: (yes/no)";
        chomp($INSTALL_FACTS=<STDIN>);
        if ("$INSTALL_FACTS" eq "yes") {
            print "Please enter the oracle database host name for Facts: ";
            chomp($FACTS_DB_HOST=<STDIN>);
            print "Please enter the oracle database port for Facts: ";
            chomp($FACTS_DB_PORT=<STDIN>);
            print "Please enter the oracle database name for Facts: ";
            chomp($FACTS_DB_NAME=<STDIN>);
            print "Please enter the database user for Facts: ";
            chomp($FACTS_DBUSER=<STDIN>);
            print "Please enter the database password for Facts database user: ";
            chomp($FACTS_DBPASSWORD=<STDIN>);
        }

        print "\n";
        print "Do you want to install Flow: (yes/no)";
        chomp($INSTALL_FLOW=<STDIN>);
        if ("$INSTALL_FLOW" eq "yes") {
            print "Please enter the database url for Connexo Flow (format: jdbc:oracle:thin:\@dbHost:dbPort:dbSID): ";
            chomp($FLOW_JDBC_URL=<STDIN>);
            print "Please enter the database user for Connexo Flow: ";
            chomp($FLOW_DB_USER=<STDIN>);
            print "Please enter the database password for Connexo Flow: ";
            chomp($FLOW_DB_PASSWORD=<STDIN>);
        }
        
        print "\n";
        if (("$INSTALL_FACTS" eq "yes") || ("$INSTALL_FLOW" eq "yes")) {
            if ("$INSTALL_CONNEXO" ne "yes") {
                print "Please enter the Connexo http port: ";
                chomp($CONNEXO_HTTP_PORT=<STDIN>);
            }
            print "Please enter the Tomcat http port: ";
            chomp($TOMCAT_HTTP_PORT=<STDIN>);
        }

        print "\n";
        print "Do you want to install WSO2IS: (yes/no) ";
        chomp($INSTALL_WSO2IS=<STDIN>);
        print "Please enter the version of your services (e.g. 10.1) or leave empty: ";
        chomp($SERVICE_VERSION=<STDIN>);        

        print "\n";
        print "Do you want to activate SSO for Facts/Flow: (yes/no) ";
        chomp($ACTIVATE_SSO=<STDIN>);
        if ("$ACTIVATE_SSO" eq "yes") {
            print "Please enter the path to your Apache HTTP 2.4: ";
            chomp($APACHE_PATH=<STDIN>);
        }
    }
    if ("$ACTIVATE_SSO" eq "yes") {
        if ("$APACHE_PATH" eq "") {
            print "If SSO has been activated, the variable APACHE_PATH needs to be filled in.\n";
            exit (0);
        }
        if (! -e "$APACHE_PATH/conf/httpd.conf") {
            print "The file $APACHE_PATH/conf/httpd.conf could not be found. Please make sure the variable APACHE_PATH has been filled in correctly.\n";
            exit (0);
        }
    }
    if (("$INSTALL_FACTS" eq "yes") || ("$INSTALL_FLOW" eq "yes")) {
        $TOMCAT_SHUTDOWN_PORT=$TOMCAT_HTTP_PORT+5;
        $TOMCAT_AJP_PORT=$TOMCAT_HTTP_PORT+6;
        $TOMCAT_SSH_PORT=$TOMCAT_HTTP_PORT+7;
        $TOMCAT_DAEMON_PORT=$TOMCAT_HTTP_PORT+8;
    }
    if ("$HOST_NAME" eq "") {
        $HOST_NAME=hostname;
    }
    if (("$CONNEXO_ADMIN_PASSWORD" eq "") || ("$CONNEXO_ADMIN_PASSWORD" eq "admin")) {
        print "Please provide an admin password (different from \"admin\")\n";
        exit (0);
    }
    $CONNEXO_URL="http://$HOST_NAME:$CONNEXO_HTTP_PORT";
    $FLOW_URL="http://$HOST_NAME:$TOMCAT_HTTP_PORT/flow";
}

sub read_uninstall_config {
    if ($cmd_line) {
        print "Please enter the version of you services (e.g. 10.1) or leave empty: ";
        chomp($SERVICE_VERSION=<STDIN>);
        print "Please enter the path to your JAVA_HOME (leave empty to use the system variable): ";
        chomp($JAVA_HOME=<STDIN>);
    } else {
        open(my $FH,"< $config_cmd") or die "Could not open $config_cmd: $!";
        while (my $row = <$FH>) {
            $row=~s/set (.*)/$1/;
            chomp($row);
            if ( "$row" ne "") {
                my @val=split('=',$row);
                if ( "$val[0]" eq "SERVICE_VERSION" )	{$SERVICE_VERSION=$val[1];}
            }
        }
        close($FH);
    }
    check_java8();
}

sub check_port {
    my $proto = getprotobyname('tcp');
    my $iaddr = inet_aton($HOST_NAME);
    my $paddr = sockaddr_in($_[0], $iaddr);
    
    socket(SOCKET, PF_INET, SOCK_STREAM, $proto) || warn "socket: $!";
    
    eval {
        #local $SIG{ALRM} = sub { die "timeout" };
        #alarm(10);
        connect(SOCKET, $paddr) || error();
        #alarm(0);
    };
    
    if ($@) {
        close SOCKET || warn "close: $!";
        return 0;
    } else {
        close SOCKET || warn "close: $!";
        return 1;
    }
}

sub checking_ports {
    my $CONNEXO_PORT=0;
    my $TOMCAT_PORT=0;
    my $TOMCAT_SHUTDOWN_PRT=0;
    my $TOMCAT_AJP_PRT=0;
    my $TOMCAT_SSH_PRT=0;
    my $TOMCAT_DAEMON_PRT=0;
    if ("$INSTALL_CONNEXO" eq "yes") { $CONNEXO_PORT=check_port($CONNEXO_HTTP_PORT); };
    if (("$INSTALL_FACTS" eq "yes") || ("$INSTALL_FLOW" eq "yes")) {
        $TOMCAT_PORT=check_port($TOMCAT_HTTP_PORT);
        $TOMCAT_SHUTDOWN_PRT=check_port($TOMCAT_SHUTDOWN_PORT);
        $TOMCAT_AJP_PRT=check_port($TOMCAT_AJP_PORT);
        $TOMCAT_SSH_PRT=check_port($TOMCAT_SSH_PORT);
        $TOMCAT_DAEMON_PRT=check_port($TOMCAT_DAEMON_PORT);
    };
    if ($CONNEXO_PORT>0 || $TOMCAT_PORT>0 || $TOMCAT_SHUTDOWN_PRT>0 || $TOMCAT_AJP_PRT>0 || $TOMCAT_SSH_PRT>0 || $TOMCAT_DAEMON_PRT>0) {
        if ($CONNEXO_PORT>0) { print "Port $CONNEXO_HTTP_PORT for Connexo already in use!\n"; }
        if ($TOMCAT_PORT>0) { print "Port $TOMCAT_HTTP_PORT for Tomcat already in use!\n"; }
        if ($TOMCAT_SHUTDOWN_PRT>0) { print "Port $TOMCAT_SHUTDOWN_PRT for Tomcat shutdown already in use!\n"; }
        if ($TOMCAT_AJP_PRT>0) { print "Port $TOMCAT_AJP_PRT for Tomcat AJP already in use!\n"; }
        if ($TOMCAT_SSH_PRT>0) { print "Port $TOMCAT_SSH_PRT for Tomcat SSH already in use!\n"; }
        if ($TOMCAT_DAEMON_PRT>0) { print "Port $TOMCAT_DAEMON_PRT for Tomcat daemon already in use!\n"; }
        exit (0);
    }
}

sub install_connexo {
	if ("$INSTALL_CONNEXO" eq "yes") {
	    if("$UPGRADE" ne "yes") {
            copy("$CONNEXO_DIR/conf/config.properties.temp","$config_file") or die "File cannot be copied: $!";
            add_to_file_if($config_file,"org.osgi.service.http.port=$CONNEXO_HTTP_PORT");
            add_to_file_if($config_file,"com.elster.jupiter.datasource.jdbcurl=$jdbcUrl");
            add_to_file_if($config_file,"com.elster.jupiter.datasource.jdbcuser=$dbUserName");
            add_to_file_if($config_file,"com.elster.jupiter.datasource.jdbcpassword=$dbPassword");
            if ("$ACTIVATE_SSO" eq "yes") {
                replace_in_file($config_file,"com.energyict.mdc.url=","com.energyict.mdc.url=http://$HOST_NAME/apps/multisense/index.html");
            } else {
                replace_in_file($config_file,"com.energyict.mdc.url=","com.energyict.mdc.url=http://$HOST_NAME:$CONNEXO_HTTP_PORT/apps/multisense/index.html");
            }
        }

		print "\n\nInstalling Connexo database schema ...\n";
		print "==========================================================================\n";
		if ("$OS" eq "MSWin32" || "$OS" eq "MSWin64") {
			system("\"$SCRIPT_DIR/Connexo.exe\" --install $CONNEXO_ADMIN_PASSWORD");
			if ("$CONNEXO_SERVICE" eq "yes") {
				system("\"$SCRIPT_DIR/ConnexoService.exe\" /install Connexo$SERVICE_VERSION");
			}
		} else {
			my $VM_OPTIONS="-Djava.util.logging.config.file=\"$CONNEXO_DIR/conf/logging.properties\"";
			my $CLASSPATH = join(":", ".", glob("$CONNEXO_DIR/lib/*.jar"));
			system("\"$JAVA_HOME/bin/java\" $VM_OPTIONS -cp \"$CLASSPATH\" com.elster.jupiter.launcher.ConnexoLauncher --install $CONNEXO_ADMIN_PASSWORD");
			if ("$CONNEXO_SERVICE" eq "yes") {
				copy("$CONNEXO_DIR/bin/connexo","/etc/init.d/Connexo$SERVICE_VERSION") or die "File cannot be copied: $!";
				chmod 0755,"/etc/init.d/Connexo$SERVICE_VERSION";
				replace_in_file("/etc/init.d/Connexo$SERVICE_VERSION",'\${CONNEXO_DIR}',"$CONNEXO_DIR");
				copy("$CONNEXO_DIR/bin/start-connexo.temp","$CONNEXO_DIR/bin/start-connexo.sh") or die "File cannot be copied: $!";
				chmod 0755,"$CONNEXO_DIR/bin/start-connexo.sh";
				replace_in_file("$CONNEXO_DIR/bin/start-connexo.sh",'\${CONNEXO_DIR}',"$CONNEXO_DIR");
				replace_in_file("$CONNEXO_DIR/bin/start-connexo.sh",'\${JAVA_HOME}',"$JAVA_HOME");
				chmod 0755,"$CONNEXO_DIR/bin/stop-connexo.sh";
			}
		}
	}
}

sub install_tomcat {
	if (("$INSTALL_FACTS" eq "yes") || ("$INSTALL_FLOW" eq "yes")) {
		print "\n\nExtracting Apache Tomcat 7 ...\n";
		print "==========================================================================\n";

		$ENV{JVM_OPTIONS}="-Dorg.uberfire.nio.git.ssh.port=$TOMCAT_SSH_PORT;-Dorg.uberfire.nio.git.daemon.port=$TOMCAT_DAEMON_PORT;-Dport.shutdown=$TOMCAT_SHUTDOWN_PORT;-Dport.http=$TOMCAT_HTTP_PORT;-Dflow.url=$FLOW_URL;-Dconnexo.url=$CONNEXO_URL;-Dconnexo.user=\"$CONNEXO_ADMIN_ACCOUNT\";-Dconnexo.password=\"$CONNEXO_ADMIN_PASSWORD\";-Dbtm.root=\"$CATALINA_HOME\";-Dbitronix.tm.configuration=\"$CATALINA_HOME/conf/btm-config.properties\";-Djbpm.tsr.jndi.lookup=java:comp/env/TransactionSynchronizationRegistry;-Dorg.kie.demo=false;-Dorg.kie.example=false;-Dconnexo.configuration=\"$CATALINA_HOME/conf/connexo.properties\" -Dorg.jboss.logging.provider=slf4j -Dorg.uberfire.nio.git.ssh.algorithm=RSA";

		chdir "$TOMCAT_BASE";
		print "Extracting $TOMCAT_ZIP.zip\n";
		system("\"$JAVA_HOME/bin/jar\" -xf $TOMCAT_ZIP.zip") == 0 or die "system $JAVA_HOME/bin/jar -xvf $TOMCAT_ZIP.zip failed: $?";
		if (-d "$TOMCAT_DIR") { rmtree("$TOMCAT_DIR"); }
		rename("apache-$TOMCAT_ZIP","$TOMCAT_DIR");

		if (-e "$TOMCAT_BASE/connexo.filter.jar") {
            print "    $TOMCAT_BASE/connexo.filter.jar -> $TOMCAT_BASE/tomcat/lib/connexo.filter.jar\n";
		    copy("$TOMCAT_BASE/connexo.filter.jar","$TOMCAT_BASE/tomcat/lib/connexo.filter.jar");
        }
        if (-e "$TOMCAT_BASE/simplelogger.properties") {
            copy("$TOMCAT_BASE/simplelogger.properties","$TOMCAT_BASE/tomcat/lib/simplelogger.properties");
        }
		chdir "$TOMCAT_DIR/bin";
		replace_in_file("$TOMCAT_BASE/$TOMCAT_DIR/conf/server.xml","<Connector port=\"8009\" protocol=\"AJP/1.3\" redirectPort=\"8443\" />","<Connector port=\"$TOMCAT_AJP_PORT\" protocol=\"AJP/1.3\" redirectPort=\"8443\" />");
		replace_in_file("$TOMCAT_BASE/$TOMCAT_DIR/conf/tomcat-users.xml","password=\"analyst\"","password=\"$TOMCAT_ADMIN_PASSWORD\"");
		replace_in_file("$TOMCAT_BASE/$TOMCAT_DIR/conf/tomcat-users.xml","password=\"admin\"","password=\"$TOMCAT_ADMIN_PASSWORD\"");
		replace_in_file("$TOMCAT_BASE/$TOMCAT_DIR/conf/tomcat-users.xml","password=\"developer\"","password=\"$TOMCAT_ADMIN_PASSWORD\"");
		replace_in_file("$TOMCAT_BASE/$TOMCAT_DIR/conf/tomcat-users.xml","password=\"user\"","password=\"$TOMCAT_ADMIN_PASSWORD\"");
		replace_in_file("$TOMCAT_BASE/$TOMCAT_DIR/conf/tomcat-users.xml","password=\"manager\"","password=\"$TOMCAT_ADMIN_PASSWORD\"");
		replace_in_file("$TOMCAT_BASE/$TOMCAT_DIR/conf/tomcat-users.xml","password=\"tomcat\"","password=\"$TOMCAT_ADMIN_PASSWORD\"");
        replace_in_file("$TOMCAT_BASE/$TOMCAT_DIR/bin/service.bat","set DISPLAYNAME=Apache Tomcat 7.0 ","set DISPLAYNAME=");
		print "Installing Apache Tomcat For Connexo as service ...\n";
		if ("$OS" eq "MSWin32" || "$OS" eq "MSWin64") {
			open(my $FH,"> $TOMCAT_BASE/$TOMCAT_DIR/bin/setenv.bat") or die "Could not open $TOMCAT_DIR/bin/setenv.bat: $!";
			print $FH "set CATALINA_OPTS=".$ENV{CATALINA_OPTS}." -Xmx512M -Dorg.uberfire.nio.git.dir=\"$CATALINA_HOME\" -Dorg.uberfire.metadata.index.dir=\"$CATALINA_HOME\" -Dorg.uberfire.nio.git.ssh.cert.dir=\"$CATALINA_HOME\" -Dorg.guvnor.m2repo.dir=\"$CATALINA_HOME/repositories\" -Dport.shutdown=$TOMCAT_SHUTDOWN_PORT -Dport.http=$TOMCAT_HTTP_PORT -Dflow.url=$FLOW_URL -Dconnexo.url=$CONNEXO_URL -Dconnexo.user=\"$CONNEXO_ADMIN_ACCOUNT\" -Dconnexo.password=\"$CONNEXO_ADMIN_PASSWORD\" -Dbtm.root=\"$CATALINA_HOME\" -Dbitronix.tm.configuration=\"$CATALINA_HOME/conf/btm-config.properties\" -Djbpm.tsr.jndi.lookup=java:comp/env/TransactionSynchronizationRegistry -Dorg.kie.demo=false -Dorg.kie.example=false -Dorg.jboss.logging.provider=slf4j -Dorg.uberfire.nio.git.ssh.algorithm=RSA\n";
			close($FH);
			system("service.bat install ConnexoTomcat$SERVICE_VERSION");
		} else {
            (my $replaceHOME = $CATALINA_HOME) =~ s/ /\\ /g;
            (my $replaceACCOUNT = $CONNEXO_ADMIN_ACCOUNT) =~ s/ /\\ /g;
            (my $replacePASSWORD = $CONNEXO_ADMIN_PASSWORD) =~ s/ /\\ /g;
			open(my $FH,"> $TOMCAT_BASE/$TOMCAT_DIR/bin/setenv.sh") or die "Could not open $TOMCAT_DIR/bin/setenv.sh: $!";
			print $FH "export CATALINA_OPTS=\"".$ENV{CATALINA_OPTS}." -Xmx512M -Dorg.uberfire.nio.git.dir=$replaceHOME -Dorg.uberfire.metadata.index.dir=$replaceHOME -Dorg.uberfire.nio.git.ssh.cert.dir=$replaceHOME -Dorg.guvnor.m2repo.dir=$replaceHOME/repositories -Dport.shutdown=$TOMCAT_SHUTDOWN_PORT -Dport.http=$TOMCAT_HTTP_PORT -Dflow.url=$FLOW_URL -Dconnexo.url=$CONNEXO_URL -Dconnexo.user=$replaceACCOUNT -Dconnexo.password=$replacePASSWORD -Dbtm.root=$replaceHOME -Dbitronix.tm.configuration=$replaceHOME/conf/btm-config.properties -Djbpm.tsr.jndi.lookup=java:comp/env/TransactionSynchronizationRegistry -Dorg.kie.demo=false -Dorg.kie.example=false -Dorg.jboss.logging.provider=slf4j -Dorg.uberfire.nio.git.ssh.algorithm=RSA\"\n";
			close($FH);
			
			open(my $FH,"> /etc/init.d/ConnexoTomcat$SERVICE_VERSION") or die "Could not open /etc/init.d/ConnexoTomcat$SERVICE_VERSION: $!";
                        print $FH "#!/bin/sh\n";
                        print $FH "#\n";
                        print $FH "#Startup script for Tomcat\n";
                        print $FH "#\n";
                        print $FH "#chkconfig: - 99 01\n";
                        print $FH "#description: This script starts Tomcat\n";
                        print $FH "#processname: jsvc\n";
			print $FH "\n";
            print $FH "\"$TOMCAT_BASE/$TOMCAT_DIR/bin/daemon.sh\" --catalina-home \"$CATALINA_HOME\" --java-home \"$JAVA_HOME\" --tomcat-user tomcat \$1";
			close($FH);
            chmod 0755,"/etc/init.d/ConnexoTomcat$SERVICE_VERSION";
		}
	}
}

sub install_wso2 {
	if ("$INSTALL_WSO2IS" eq "yes") {
		my $WSO2_DIR="$CONNEXO_DIR/partners";
		print "\n\nInstalling WSO2 Identity Server ...\n";
		print "==========================================================================\n";
		chdir "$WSO2_DIR";
		print "Extracting wso2is-4.5.0.zip\n";
		system("\"$JAVA_HOME/bin/jar\" -xf wso2is-4.5.0.zip") == 0 or die "$JAVA_HOME/bin/jar -xvf wso2is-4.5.0.zip failed: $?";
		print "Extracting yajsw-11.11.zip\n";
		system("\"$JAVA_HOME/bin/jar\" -xf yajsw-11.11.zip") == 0 or die "$JAVA_HOME/bin/jar -xvf yajsw-11.11.zip failed: $?";
		chdir "$CONNEXO_DIR";
		$ENV{CARBON_HOME}="$WSO2_DIR/wso2is-4.5.0";
		copy("$WSO2_DIR/wso2is-4.5.0/bin/yajsw/wrapper.conf","$WSO2_DIR/yajsw-stable-11.11/conf") or die "File cannot be copied: $!";
		replace_in_file("$WSO2_DIR/yajsw-stable-11.11/conf/wrapper.conf","wrapper.console.title=WSO2 Identity Server","wrapper.console.title=ConnexoWSO2IS$SERVICE_VERSION");
		replace_in_file("$WSO2_DIR/yajsw-stable-11.11/conf/wrapper.conf","wrapper.ntservice.name=WSO2IS","wrapper.ntservice.name=ConnexoWSO2IS$SERVICE_VERSION");
		replace_in_file("$WSO2_DIR/yajsw-stable-11.11/conf/wrapper.conf","wrapper.ntservice.displayname=WSO2 Identity Server","wrapper.ntservice.displayname=ConnexoWSO2IS$SERVICE_VERSION");
		replace_in_file("$WSO2_DIR/yajsw-stable-11.11/conf/wrapper.conf","wrapper.ntservice.description=WSO2 Identity Server","wrapper.ntservice.description=WSO2 Identity Server$SERVICE_VERSION");
		if ("$OS" eq "MSWin32" || "$OS" eq "MSWin64") {
			system("\"$WSO2_DIR/yajsw-stable-11.11/bat/installService.bat\" < NUL");
		} else {
            $ENV{PATH}="$JAVA_HOME/bin/:$ENV{PATH}";
			$ENV{java_home}="$JAVA_HOME";
			$ENV{carbon_home}="$WSO2_DIR/wso2is-4.5.0";
			chmod 0755,"$WSO2_DIR/yajsw-stable-11.11/bin/installDaemon.sh";
			chmod 0755,"$WSO2_DIR/yajsw-stable-11.11/bin/installDaemonNoPriv.sh";
			chmod 0755,"$WSO2_DIR/yajsw-stable-11.11/bin/wrapper.sh";
			system("\"$WSO2_DIR/yajsw-stable-11.11/bin/installDaemonNoPriv.sh\" /dev/null 2>&1");
		}
		print "WSO2 Identity Server successfully installed\n";
	}
}

sub install_facts {
	if ("$INSTALL_FACTS" eq "yes") {
		my $INSTALLER_LICENSE="$CONNEXO_DIR/partners/facts/facts-license.lic";
		my $FACTS_BASE_PROPERTIES="$CONNEXO_DIR/partners/facts/facts.properties";
		my $FACTS_TEMP_PROPERTIES="$CONNEXO_DIR/custom.properties";
		my $FACTS_BASE="$TOMCAT_BASE/$TOMCAT_DIR/webapps";
		my $FACTS_DIR="$FACTS_BASE/facts";
		my $FACTS_PORT=$TOMCAT_HTTP_PORT;

		print "\n\nInstalling Connexo Facts ...\n";
		print "==========================================================================\n";

		chdir "$CONNEXO_DIR/bin";
		if ("$FACTS_LICENSE" ne "") {
			chdir dirname($FACTS_LICENSE);
			if (-e basename($FACTS_LICENSE)) {
				copy(basename($FACTS_LICENSE),"$INSTALLER_LICENSE") or die "File cannot be copied: $!";
			} else {
				print "License file $FACTS_LICENSE not found, taking the default one.\n";
			}
		}
		make_path("$FACTS_BASE/appserver/bin");
		copy("$CATALINA_BASE/bin/catalina.bat","$FACTS_BASE/appserver/bin/catalina.bat") or die "File cannot be copied: $!";
		copy("$CATALINA_BASE/bin/catalina.sh","$FACTS_BASE/appserver/bin/catalina.sh") or die "File cannot be copied: $!";

		print "Applying custom properties ...\n";
		copy("$FACTS_BASE_PROPERTIES","$FACTS_TEMP_PROPERTIES");

		my $INSTALLER_LICENSE_REPL=$INSTALLER_LICENSE;
		$INSTALLER_LICENSE_REPL=~s{\\}{\\\\}g;
		my $FACTS_BASE_REPL=$FACTS_BASE;
		$FACTS_BASE_REPL=~s{\\}{\\\\}g;
		open(my $FH,">> $FACTS_TEMP_PROPERTIES") or die "Could not open $FACTS_TEMP_PROPERTIES: $!";
		print $FH "\n";
		print $FH "action.adminuser.username=$CONNEXO_ADMIN_ACCOUNT\n";
		print $FH "action.adminuser.password=$CONNEXO_ADMIN_PASSWORD\n";
		print $FH "option.licencefile=$INSTALLER_LICENSE_REPL\n";
		print $FH "option.db.hostname=$FACTS_DB_HOST\n";
		print $FH "option.db.port=$FACTS_DB_PORT\n";
		print $FH "option.db.dbname=$FACTS_DB_NAME\n";
		print $FH "option.db.username=$FACTS_DBUSER\n";
		print $FH "option.db.userpassword=$FACTS_DBPASSWORD\n";
		print $FH "option.db.dbausername=$FACTS_DBUSER\n";
		print $FH "option.db.dbapassword=$FACTS_DBPASSWORD\n";
		print $FH "option.serverport=$FACTS_PORT\n";
		print $FH "option.installpath=$FACTS_BASE_REPL\n";
		close($FH);

		chdir "$CONNEXO_DIR";
		system("\"$JAVA_HOME/bin/jar\" -uvf \"$CONNEXO_DIR/partners/facts/facts.jar\" custom.properties") == 0 or die "$JAVA_HOME/bin/jar -uvf \"$CONNEXO_DIR/partners/facts/facts.jar\" custom.properties failed: $?";
		unlink("$CONNEXO_DIR/custom.properties");
        system("\"$JAVA_HOME/bin/java\" -jar \"$CONNEXO_DIR/partners/facts/facts.jar\" -silent") == 0 or die "$JAVA_HOME/bin/java -jar \"$CONNEXO_DIR/partners/facts/facts.jar\" -silent failed: $?";
		if (!-d "$FACTS_DIR") { make_path("$FACTS_DIR"); }
		copy("$FACTS_BASE/facts.war","$FACTS_DIR/facts.war") or die "File cannot be copied: $!";
		chdir "$FACTS_DIR";
		print "Extracting facts.war\n";
		system("\"$JAVA_HOME/bin/jar\" -xf facts.war") == 0 or die "$JAVA_HOME/bin/jar -xvf facts.war failed: $?";
		unlink("$FACTS_DIR/facts.war");
		unlink("$FACTS_BASE/facts.war");
		if (-d "$FACTS_BASE/appserver") { rmtree("$FACTS_BASE/appserver"); }

		if (-e "$CONNEXO_DIR/partners/facts/facts.filter.jar") {
            print "    $CONNEXO_DIR/partners/facts/facts.filter.jar -> $FACTS_DIR/WEB-INF/lib/facts.filter.jar\n";
		    copy("$CONNEXO_DIR/partners/facts/facts.filter.jar","$FACTS_DIR/WEB-INF/lib/facts.filter.jar");
        }
		print "Connexo Facts successfully installed\n";

        add_to_file_if($config_file,"com.elster.jupiter.yellowfin.url=http://$HOST_NAME:$TOMCAT_HTTP_PORT/facts");
        add_to_file_if($config_file,"com.elster.jupiter.yellowfin.user=$CONNEXO_ADMIN_ACCOUNT");
        add_to_file_if($config_file,"com.elster.jupiter.yellowfin.password=$CONNEXO_ADMIN_PASSWORD");
		if ("$ACTIVATE_SSO" eq "yes") {
            add_to_file_if($config_file,"com.elster.jupiter.yellowfin.externalurl=http://$HOST_NAME/facts/");
        }
	}
}

sub replace_in_file {
	my ($filename,$src,$dst)=@_;
	open (IN,"$filename") or die "Cannot open file ".$filename." for read";     
	my @lines=<IN>;  
	close IN;
 
	open (OUT,">","$filename") or die "Cannot open file ".$filename." for write";
	foreach my $line (@lines) {  
		$line =~ s/$src/$dst/ig;  
		print OUT $line;  
	}  
	close OUT;
}

sub add_to_file_if {
    my $found;
	my ($filename,$text)=@_;
    open(FILE,"$filename") or die "Cannot open file ".$filename;
    if (grep{/^$text/} <FILE>){
        $found=0;
    } else {
        $found=1;
    }
    close FILE;
    if ($found>0) {
        open(FILE,">> $filename") or die "Cannot open file ".$filename;
        print FILE "$text\n";
        close FILE;
    }
}

sub add_to_file {
    my $found=0;
	my ($filename,$text)=@_;
    open(FILE,">> $filename") or die "Cannot open file ".$filename;
    print FILE "$text\n";
    close FILE;
}

sub install_flow {
	if ("$INSTALL_FLOW" eq "yes") {
		my $FLOW_DIR="$TOMCAT_BASE/$TOMCAT_DIR/webapps/flow";
		my $FLOW_TABLESPACE="flow";
		my $FLOW_DBUSER="flow";
		my $FLOW_DBPASSWORD="flow";

		print "\n\nInstalling Connexo Flow ...\n";
		print "==========================================================================\n";

		if (!-d "$FLOW_DIR") { make_path("$FLOW_DIR"); }
		copy("$CONNEXO_DIR/partners/flow/flow.war","$FLOW_DIR/flow.war");
		chdir "$FLOW_DIR";
		print "Extracting flow.war\n";
		system("\"$JAVA_HOME/bin/jar\" -xf flow.war") == 0 or die "$JAVA_HOME/bin/jar -xvf flow.war failed: $?";
		unlink("$FLOW_DIR/flow.war");

		copy("$CONNEXO_DIR/partners/flow/resources.properties","$CATALINA_HOME/conf/resources.properties");
		replace_in_file("$CATALINA_HOME/conf/resources.properties",'\$\{jdbc\}',"$FLOW_JDBC_URL");
		replace_in_file("$CATALINA_HOME/conf/resources.properties",'\$\{user\}',"$FLOW_DB_USER");
		replace_in_file("$CATALINA_HOME/conf/resources.properties",'\$\{password\}',"$FLOW_DB_PASSWORD");

		copy("$CONNEXO_DIR/partners/flow/kie-wb-deployment-descriptor.xml","$CONNEXO_DIR/kie-wb-deployment-descriptor.xml");
		replace_in_file("$CONNEXO_DIR/kie-wb-deployment-descriptor.xml",'\$\{user\}',"$CONNEXO_ADMIN_ACCOUNT");
		replace_in_file("$CONNEXO_DIR/kie-wb-deployment-descriptor.xml",'\$\{password\}',"$CONNEXO_ADMIN_PASSWORD");
		copy("$CONNEXO_DIR/kie-wb-deployment-descriptor.xml","$FLOW_DIR/WEB-INF/classes/META-INF/kie-wb-deployment-descriptor.xml");
		unlink("$CONNEXO_DIR/kie-wb-deployment-descriptor.xml");

		print "Copying extra jar files\n";
		if (-e "$CONNEXO_DIR/partners/flow/jbpm.extension.jar") {
            print "    $CONNEXO_DIR/partners/flow/jbpm.extension.jar -> $FLOW_DIR/WEB-INF/lib/jbpm.extension.jar\n";
		    copy("$CONNEXO_DIR/partners/flow/jbpm.extension.jar","$FLOW_DIR/WEB-INF/lib/jbpm.extension.jar");
        }
		if (-e "$CONNEXO_DIR/partners/flow/flow.filter.jar") {
            print "    $CONNEXO_DIR/partners/flow/flow.filter.jar -> $FLOW_DIR/WEB-INF/lib/flow.filter.jar\n";
		    copy("$CONNEXO_DIR/partners/flow/flow.filter.jar","$FLOW_DIR/WEB-INF/lib/flow.filter.jar");
        }
		print "Connexo Flow successfully installed\n";

		if ("$ACTIVATE_SSO" eq "yes") {
            replace_in_file($config_file,"com.elster.jupiter.bpm.user=","#com.elster.jupiter.bpm.user=");
            replace_in_file($config_file,"com.elster.jupiter.bpm.password=","#com.elster.jupiter.bpm.password=");
            add_to_file_if($config_file,"com.elster.jupiter.bpm.url=http://$HOST_NAME/flow/");
        } else {
            add_to_file_if($config_file,"com.elster.jupiter.bpm.url=http://$HOST_NAME:$TOMCAT_HTTP_PORT/flow");
            add_to_file_if($config_file,"com.elster.jupiter.bpm.user=$CONNEXO_ADMIN_ACCOUNT");
            add_to_file_if($config_file,"com.elster.jupiter.bpm.password=$TOMCAT_ADMIN_PASSWORD");
        }
	}
}

sub activate_sso {
    if ("$ACTIVATE_SSO" eq "yes") {
        if (("$INSTALL_FACTS" eq "yes") || ("$INSTALL_FLOW" eq "yes")) {
            #install apache 2.2 or 2.4???
            my $PUBLIC_KEY_PROPERTIES="to be filled in";
            if (-e "$CONNEXO_DIR/publicKey.txt") {
                open(my $FH,"< $CONNEXO_DIR/publicKey.txt") or die "Could not open $CONNEXO_DIR/publicKey.txt: $!";
                $PUBLIC_KEY_PROPERTIES=<$FH>;
                chomp($PUBLIC_KEY_PROPERTIES);
                close($FH);
            }            
            #if ("$OS" eq "MSWin32" || "$OS" eq "MSWin64") {
            #    copy("$CONNEXO_DIR/bin/vcruntime140.dll","$APACHE_PATH/bin/vcruntime140.dll");
            #    system("$APACHE_PATH/bin/httpd.exe -k install -n \"Apache2.4\"");
            #} else {
            #    #install Linux daemon
            #}
            if("$UPGRADE" ne "yes") {
                add_to_file_if("$APACHE_PATH/conf/httpd.conf","Include conf/extra/httpd-connexo-vhosts$SERVICE_VERSION.conf");
                replace_in_file("$APACHE_PATH/conf/httpd.conf","#LoadModule proxy_module modules/mod_proxy.so","LoadModule proxy_module modules/mod_proxy.so");
                replace_in_file("$APACHE_PATH/conf/httpd.conf","#LoadModule proxy_http_module modules/mod_proxy_http.so","LoadModule proxy_http_module modules/mod_proxy_http.so");
                replace_in_file("$APACHE_PATH/conf/httpd.conf","#LoadModule rewrite_module modules/mod_rewrite.so","LoadModule rewrite_module modules/mod_rewrite.so");
                open(my $FH,"> $APACHE_PATH/conf/extra/httpd-connexo-vhosts$SERVICE_VERSION.conf") or die "Could not open $APACHE_PATH/conf/extra/httpd-connexo-vhosts$SERVICE_VERSION.conf: $!";
                print $FH "Define HOSTNAME $HOST_NAME\n";
                print $FH "\n";
                print $FH "<VirtualHost *:80>\n";
                print $FH "ServerName \${HOSTNAME}\n";
                print $FH "\n";
                print $FH "RewriteEngine On\n";
                print $FH "   ProxyPreserveHost on\n";
                print $FH "\n";
                print $FH "   RedirectMatch ^/\$ http://\${HOSTNAME}/apps/login/index.html\n";
                print $FH "\n";
                print $FH "   ProxyPass /flow/ http://\${HOSTNAME}:$TOMCAT_HTTP_PORT/flow/\n";
                print $FH "   ProxyPassReverse /flow/ http://\${HOSTNAME}:$TOMCAT_HTTP_PORT/flow/\n";
                print $FH "   ProxyPassReverse /flow/ http://\${HOSTNAME}/flow/\n";
                print $FH "   ProxyPass /facts/ http://\${HOSTNAME}:$TOMCAT_HTTP_PORT/facts/\n";
                print $FH "   ProxyPassReverse /facts/ http://\${HOSTNAME}:$TOMCAT_HTTP_PORT/facts/\n";
                print $FH "   ProxyPassReverse /facts/ http://\${HOSTNAME}/facts/\n";
                print $FH "\n";
                print $FH "   ProxyPassReverse / http://\${HOSTNAME}:$CONNEXO_HTTP_PORT/\n";
                print $FH "   DirectoryIndex index.html\n";
                print $FH "\n";
                print $FH "   RewriteRule ^/apps/(.+)\$ http://\${HOSTNAME}:$CONNEXO_HTTP_PORT/apps/\$1 [P]\n";
                print $FH "   RewriteRule ^/api/(.+)\$ http://\${HOSTNAME}:$CONNEXO_HTTP_PORT/api/\$1 [P]\n";
                print $FH "   RewriteRule ^/public/api/(.+)\$ http://\${HOSTNAME}:$CONNEXO_HTTP_PORT/public/api/\$1 [P]\n";
                print $FH "</VirtualHost>\n";
                close $FH;
            }

            if ("$INSTALL_FLOW" eq "yes") {
                replace_in_file("$CATALINA_BASE/webapps/flow/WEB-INF/web.xml","<!--filter>","<filter>");
                replace_in_file("$CATALINA_BASE/webapps/flow/WEB-INF/web.xml","</filter-mapping-->","</filter-mapping>");
                replace_in_file("$CATALINA_BASE/webapps/flow/WEB-INF/web.xml","<!-- Section 1: Default Flow authentication method; to be commented out when using Connexo SSO -->","<!-- Section 1: Default Flow authentication method; to be commented out when using Connexo SSO >");
                replace_in_file("$CATALINA_BASE/webapps/flow/WEB-INF/web.xml","<!-- Section 1 ends here -->","< Section 1 ends here -->");
                replace_in_file("$CATALINA_BASE/webapps/flow/WEB-INF/web.xml","<!-- Section 2: Default Flow security constraints; to be commented out when using Connexo SSO -->","<!-- Section 2: Default Flow security constraints; to be commented out when using Connexo SSO >");
                replace_in_file("$CATALINA_BASE/webapps/flow/WEB-INF/web.xml","<!-- Section 2 ends here -->","< Section 2 ends here -->");

                replace_in_file("$CATALINA_BASE/webapps/flow/WEB-INF/beans.xml","<class>org.jbpm.services.cdi.producer.JAASUserGroupInfoProducer</class>","<!--class>org.jbpm.kie.services.cdi.producer.JAASUserGroupInfoProducer</class-->");
                replace_in_file("$CATALINA_BASE/webapps/flow/WEB-INF/beans.xml","<!--class>com.elster.partners.connexo.filters.flow.identity.ConnexoUserGroupInfoProducer</class-->","<class>com.elster.partners.connexo.filters.flow.identity.ConnexoUserGroupInfoProducer</class>");
                replace_in_file("$CATALINA_BASE/webapps/flow/WEB-INF/beans.xml","<!--class>com.elster.partners.connexo.filters.flow.authorization.ConnexoAuthenticationService</class-->","<class>com.elster.partners.connexo.filters.flow.authorization.ConnexoAuthenticationService</class>");

                add_to_file("$CATALINA_BASE/conf/connexo.properties","");
                add_to_file("$CATALINA_BASE/conf/connexo.properties","com.elster.jupiter.user=$CONNEXO_ADMIN_ACCOUNT");
                add_to_file("$CATALINA_BASE/conf/connexo.properties","com.elster.jupiter.password=$CONNEXO_ADMIN_PASSWORD");
            }

            if ("$INSTALL_FACTS" eq "yes") {
                replace_in_file("$CATALINA_BASE/webapps/facts/WEB-INF/web.xml","<!--filter>","<filter>");
                replace_in_file("$CATALINA_BASE/webapps/facts/WEB-INF/web.xml","</filter-mapping-->","</filter-mapping>");

                add_to_file("$CATALINA_BASE/conf/connexo.properties","");
                add_to_file("$CATALINA_BASE/conf/connexo.properties","com.elster.yellowfin.admin.usr=$CONNEXO_ADMIN_ACCOUNT");
                add_to_file("$CATALINA_BASE/conf/connexo.properties","com.elster.yellowfin.admin.pwd=$CONNEXO_ADMIN_PASSWORD");
            }
            
            add_to_file("$CATALINA_BASE/conf/connexo.properties","");
            add_to_file_if("$CATALINA_BASE/conf/connexo.properties","com.elster.jupiter.url=http://$HOST_NAME:$CONNEXO_HTTP_PORT");
            add_to_file_if("$CATALINA_BASE/conf/connexo.properties","com.elster.jupiter.externalurl=http://$HOST_NAME");
            add_to_file_if("$CATALINA_BASE/conf/connexo.properties","$PUBLIC_KEY_PROPERTIES");

            #if ("$OS" eq "MSWin32" || "$OS" eq "MSWin64") {
            #    system("sc config \"Apache2.4\"  start= delayed-auto");
            #    system("sc start Apache2.4");
            #} else {
            #    #start Linux daemon
            #}
        }
    }
}

sub change_owner {
	if ("$OS" eq "linux") {
		system("chown -R -f connexo:connexo \"$CONNEXO_DIR\"");
		system("chown -R -f tomcat:tomcat \"$TOMCAT_BASE\"");
	}
}

sub start_connexo {
	if ("$INSTALL_CONNEXO" eq "yes") {
		if ("$CONNEXO_SERVICE" eq "yes") {
			print "\n\nStarting Connexo ...\n";
			print "==========================================================================\n";
			if ("$OS" eq "MSWin32" || "$OS" eq "MSWin64") {
				system("sc config \"Connexo$SERVICE_VERSION\"  start= delayed-auto");
				system("sc failure \"Connexo$SERVICE_VERSION\" actions= restart/10000/restart/10000/\"\"/10000 reset= 86400");
				system("sc start Connexo$SERVICE_VERSION");
			} else {
				system("/sbin/service Connexo$SERVICE_VERSION start");
				system("/sbin/chkconfig --add Connexo$SERVICE_VERSION");
                system("/sbin/chkconfig Connexo$SERVICE_VERSION on");
			}
		}
	}
}

sub start_tomcat {
	if (("$INSTALL_FACTS" eq "yes") || ("$INSTALL_FLOW" eq "yes")) {
		print "\n\nStarting Apache Tomcat ...\n";
		print "==========================================================================\n";
        if (("$INSTALL_FACTS" eq "yes") && ("$INSTALL_FLOW" ne "yes")) {
            copy("$CONNEXO_DIR/partners/flow/resources.properties","$CATALINA_HOME/conf/resources.properties");
        }
		if ("$OS" eq "MSWin32" || "$OS" eq "MSWin64") {
			system("sc config \"ConnexoTomcat$SERVICE_VERSION\"  start= delayed-auto");
			system("sc failure \"ConnexoTomcat$SERVICE_VERSION\" actions= restart/10000/restart/10000/\"\"/10000 reset= 86400");
			system("sc start ConnexoTomcat$SERVICE_VERSION");
			sleep 10;
			while ((`sc query ConnexoTomcat$SERVICE_VERSION` =~ m/STATE.*:.*RUNNING/) eq "") {
				sleep 3;
			}
		} else {
			if (! -e "$TOMCAT_BASE/$TOMCAT_DIR/bin/jsvc") {
				chdir "$CATALINA_HOME/bin";
				system("tar xfz commons-daemon-native.tar.gz");
				chdir "$CATALINA_HOME/bin/commons-daemon-1.0.15-native-src/unix";
				system("./configure");
				system("make");
				copy("jsvc","../..");
				chdir "$CATALINA_HOME/bin";
			}
			chmod 0755,"$TOMCAT_BASE/$TOMCAT_DIR/bin/jsvc";
			chmod 0755,"$TOMCAT_BASE/$TOMCAT_DIR/bin/daemon.sh";
			#system("\"$TOMCAT_BASE/$TOMCAT_DIR/bin/daemon.sh\" start");
			system("/sbin/service ConnexoTomcat$SERVICE_VERSION start");
            system("/sbin/chkconfig --add ConnexoTomcat$SERVICE_VERSION");
            system("/sbin/chkconfig ConnexoTomcat$SERVICE_VERSION on");
		}

		if ("$INSTALL_FACTS" eq "yes") {
			print "\nInstalling Connexo Facts content...\n";
			my $ENCRYPTED_PASSWORD=`"$JAVA_HOME/bin/java" -jar \"$CONNEXO_DIR/partners/facts/EncryptPassword.jar\" $dbPassword`;

			copy("$CONNEXO_DIR/partners/facts/open-reports.xml","$CONNEXO_DIR/datasource.xml");
			replace_in_file("$CONNEXO_DIR/datasource.xml",'\$\{jdbc\}',"$jdbcUrl");
			replace_in_file("$CONNEXO_DIR/datasource.xml",'\$\{user\}',"$dbUserName");
			replace_in_file("$CONNEXO_DIR/datasource.xml",'\$\{password\}',"$ENCRYPTED_PASSWORD");
			replace_in_file("$CONNEXO_DIR/datasource.xml",'\$\{host\}',"$FACTS_DB_HOST");
			replace_in_file("$CONNEXO_DIR/datasource.xml",'\$\{port\}',"$FACTS_DB_PORT");
			replace_in_file("$CONNEXO_DIR/datasource.xml",'\$\{instance\}',"$FACTS_DB_NAME");

			chdir "$CONNEXO_DIR";
			if ("$ACTIVATE_SSO" eq "yes") {
                system("\"$JAVA_HOME/bin/java\" -cp \"$CONNEXO_DIR/partners/facts/yellowfin.installer.jar\" com.elster.jupiter.install.reports.OpenReports datasource.xml http://$HOST_NAME:$TOMCAT_HTTP_PORT/facts $CONNEXO_ADMIN_ACCOUNT $CONNEXO_ADMIN_PASSWORD") == 0 or die "Installing Connexo Facts content failed: $?";
            } else {
                system("\"$JAVA_HOME/bin/java\" -cp \"$CONNEXO_DIR/partners/facts/yellowfin.installer.jar\" com.elster.jupiter.install.reports.OpenReports datasource.xml http://$HOST_NAME:$TOMCAT_HTTP_PORT/facts $CONNEXO_ADMIN_ACCOUNT $TOMCAT_ADMIN_PASSWORD") == 0 or die "Installing Connexo Facts content failed: $?";
            }
			unlink("$CONNEXO_DIR/datasource.xml");
		}

		if ("$INSTALL_FLOW" eq "yes") {
			print "\nInstalling Connexo Flow content...\n";

			opendir(DIR,"$CONNEXO_DIR/bundles");
            my @files = grep(/com\.elster\.jupiter\.bpm-.*\.jar$/,readdir(DIR));
            closedir(DIR);

            my $BPM_BUNDLE;
            foreach my $file (@files) {
                $BPM_BUNDLE="$file";
            }

            chdir "$CONNEXO_DIR";
			if ("$ACTIVATE_SSO" eq "yes") {
			    system("\"$JAVA_HOME/bin/java\" -cp \"bundles/$BPM_BUNDLE\" com.elster.jupiter.bpm.install.ProcessDeployer createOrganizationalUnit $CONNEXO_ADMIN_ACCOUNT $CONNEXO_ADMIN_PASSWORD http://$HOST_NAME:$TOMCAT_HTTP_PORT/flow") == 0 or die "Installing Connexo Flow content failed: $?";
                sleep 5;
                system("\"$JAVA_HOME/bin/java\" -cp \"bundles/$BPM_BUNDLE\" com.elster.jupiter.bpm.install.ProcessDeployer createRepository $CONNEXO_ADMIN_ACCOUNT $CONNEXO_ADMIN_PASSWORD http://$HOST_NAME:$TOMCAT_HTTP_PORT/flow") == 0 or die "Installing Connexo Flow content failed: $?";
            } else {
                system("\"$JAVA_HOME/bin/java\" -cp \"bundles/$BPM_BUNDLE\" com.elster.jupiter.bpm.install.ProcessDeployer createOrganizationalUnit $CONNEXO_ADMIN_ACCOUNT $TOMCAT_ADMIN_PASSWORD http://$HOST_NAME:$TOMCAT_HTTP_PORT/flow") == 0 or die "Installing Connexo Flow content failed: $?";
                sleep 5;
                system("\"$JAVA_HOME/bin/java\" -cp \"bundles/$BPM_BUNDLE\" com.elster.jupiter.bpm.install.ProcessDeployer createRepository $CONNEXO_ADMIN_ACCOUNT $TOMCAT_ADMIN_PASSWORD http://$HOST_NAME:$TOMCAT_HTTP_PORT/flow") == 0 or die "Installing Connexo Flow content failed: $?";
            }

            print "\nDeploy MDC processes...\n";
            dircopy("$CONNEXO_DIR/partners/flow/mdc/kie", "$TOMCAT_BASE/$TOMCAT_DIR/repositories/kie");
            my $mdcfile = "$CONNEXO_DIR/partners/flow/mdc/processes.csv";
            if(-e $mdcfile){
                open(INPUT, $mdcfile);
                my $line = <INPUT>; # header
                while($line = <INPUT>)
                {
                    chomp($line);
                    my ($name,$deploymentid)  = split(';', $line);
                    if ("$ACTIVATE_SSO" eq "yes") {
                        system("\"$JAVA_HOME/bin/java\" -cp \"bundles/$BPM_BUNDLE\" com.elster.jupiter.bpm.install.ProcessDeployer deployProcess $CONNEXO_ADMIN_ACCOUNT $CONNEXO_ADMIN_PASSWORD http://$HOST_NAME:$TOMCAT_HTTP_PORT/flow $deploymentid") == 0 or die "Installing Connexo Flow content failed: $?";
                    } else {
                        system("\"$JAVA_HOME/bin/java\" -cp \"bundles/$BPM_BUNDLE\" com.elster.jupiter.bpm.install.ProcessDeployer deployProcess $CONNEXO_ADMIN_ACCOUNT $TOMCAT_ADMIN_PASSWORD http://$HOST_NAME:$TOMCAT_HTTP_PORT/flow $deploymentid") == 0 or die "Installing Connexo Flow content failed: $?";
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
                        system("\"$JAVA_HOME/bin/java\" -cp \"bundles/$BPM_BUNDLE\" com.elster.jupiter.bpm.install.ProcessDeployer deployProcess $CONNEXO_ADMIN_ACCOUNT $CONNEXO_ADMIN_PASSWORD http://$HOST_NAME:$TOMCAT_HTTP_PORT/flow $deploymentid") == 0 or die "Installing Connexo Flow content failed: $?";
                    } else {
                        system("\"$JAVA_HOME/bin/java\" -cp \"bundles/$BPM_BUNDLE\" com.elster.jupiter.bpm.install.ProcessDeployer deployProcess $CONNEXO_ADMIN_ACCOUNT $TOMCAT_ADMIN_PASSWORD http://$HOST_NAME:$TOMCAT_HTTP_PORT/flow $deploymentid") == 0 or die "Installing Connexo Flow content failed: $?";
                    }
                    sleep 2;
                }
                close(INPUT);
            }
		}
	}
}

sub final_steps {
    if ("$ACTIVATE_SSO" eq "yes") {
        print "\nFinal steps:\n";
		print "==========================================================================\n";
        print "Before you can use SSO, make sure you execute the following steps:\n";
        print "1. Install Apache HTTP 2.4 as a service:\n";
        print "   -> $APACHE_PATH/bin/httpd.exe -k install -n \"Apache2.4\"\n";
        print "2. Change Startup Type to Automatic (Delayed Start):\n";
        print "   -> sc config \"Apache2.4\"  start= delayed-auto\n";
        print "3. Start Apache HTTP 2.4 service:\n";
        print "   -> sc start Apache2.4\n";
    }
}

sub uninstall_tomcat_for_upgrade() {
	if ("$OS" eq "MSWin32" || "$OS" eq "MSWin64") {
		print "Stop and remove ConnexoTomcat$UPGRADE_OLD_SERVICE_VERSION service";
		system("\"$CONNEXO_DIR/partners/tomcat/bin/service.bat\" remove ConnexoTomcat$UPGRADE_OLD_SERVICE_VERSION");
		while ((`sc query ConnexoTomcat$UPGRADE_OLD_SERVICE_VERSION` =~ m/STATE.*:.*/) ne "") {
			sleep 3;
		}
    } else {
		print "Stop and remove ConnexoTomcat$UPGRADE_OLD_SERVICE_VERSION service";
		system("/sbin/service ConnexoTomcat$UPGRADE_OLD_SERVICE_VERSION stop");
		sleep 3;
		system("/sbin/chkconfig --del ConnexoTomcat$UPGRADE_OLD_SERVICE_VERSION");
		unlink("/etc/init.d/ConnexoTomcat$UPGRADE_OLD_SERVICE_VERSION");
	}
}

sub uninstall_all {
	my $WSO2_DIR="$CONNEXO_DIR/partners";
	$ENV{CARBON_HOME}="$WSO2_DIR/wso2is-4.5.0";
	if ("$OS" eq "MSWin32" || "$OS" eq "MSWin64") {
		print "Stop and remove Connexo$SERVICE_VERSION service";
		system("\"$CONNEXO_DIR/bin/ConnexoService.exe\" /uninstall Connexo$SERVICE_VERSION");
		print "Stop and remove ConnexoTomcat$SERVICE_VERSION service";
		system("\"$CONNEXO_DIR/partners/tomcat/bin/service.bat\" remove ConnexoTomcat$SERVICE_VERSION");
		while ((`sc query ConnexoTomcat$SERVICE_VERSION` =~ m/STATE.*:.*/) ne "") {
			sleep 3;
		}
        print "Stop and remove wso service";
		system("\"$WSO2_DIR/yajsw-stable-11.11/bat/uninstallService.bat\" < NUL");
	} else {
		print "Stop and remove Connexo$SERVICE_VERSION service";
		system("/sbin/service Connexo$SERVICE_VERSION stop");
		sleep 3;
		system("pgrep -u connexo | xargs kill -9");
		sleep 3;
		system("userdel -r connexo");
		system("/sbin/chkconfig --del Connexo$SERVICE_VERSION");
		unlink("/etc/init.d/Connexo$SERVICE_VERSION");
		print "Stop and remove ConnexoTomcat$SERVICE_VERSION service";
		system("/sbin/service ConnexoTomcat$SERVICE_VERSION stop");
		sleep 3;
		system("userdel -r tomcat");
        system("/sbin/chkconfig --del ConnexoTomcat$SERVICE_VERSION");
		unlink("/etc/init.d/ConnexoTomcat$SERVICE_VERSION");
		print "Stop and remove wso service";
        $ENV{PATH}="$JAVA_HOME/bin/:$ENV{PATH}";
		$ENV{java_home}="$JAVA_HOME";
		chmod 0755,"$WSO2_DIR/yajsw-stable-11.11/bin/uninstallDaemon.sh";
		chmod 0755,"$WSO2_DIR/yajsw-stable-11.11/bin/uninstallDaemonNoPriv.sh";
		chmod 0755,"$WSO2_DIR/yajsw-stable-11.11/bin/wrapper.sh";
		system("\"$WSO2_DIR/yajsw-stable-11.11/bin/uninstallDaemonNoPriv.sh\" /dev/null 2>&1");
	}
    #uninstall Apache httpd 2.2 or 2.4
	print "Remove folders (tomcat, wso2is, yajsw)\n";
	if (-d "$CONNEXO_DIR/partners/tomcat") { rmtree("$CONNEXO_DIR/partners/tomcat"); }
	if (-d "$CONNEXO_DIR/partners/wso2is-4.5.0") { rmtree("$CONNEXO_DIR/partners/wso2is-4.5.0"); }
	if (-d "$CONNEXO_DIR/partners/yajsw-stable-11.11") { rmtree("$CONNEXO_DIR/partners/yajsw-stable-11.11"); }
	if (-e "$CONNEXO_DIR/conf/config.properties") { unlink("$CONNEXO_DIR/conf/config.properties"); }
}

sub find_string_value {
    my @bundle_arr;
    open my $fh, $_[0] or die;
    while (my $line = <$fh>) {
        if ($line =~ /Bundle-SymbolicName/) {
            $line =~s/Bundle-SymbolicName: //g;
            $line =~ s/\r|\n//g;
            @bundle_arr[0]=$line;
        }
        if ($line =~ /Bundle-Version/) {
            $line =~s/Bundle-Version: //g;
            $line =~ s/\r|\n//g;
            @bundle_arr[1]=$line;
        }
        if ($line =~ /Git-SHA-1/) {
            $line =~s/Git-SHA-1: //g;
            $line =~ s/\r|\n//g;
            @bundle_arr[2]=$line;
        }
    }
    close($fh);
    return @bundle_arr;
}

sub print_screen_file {
    my $fh = shift;
    my $TXT = shift;
    print "$TXT";
    print $fh "$TXT";
}

sub perform_upgrade {
    if ("$UPGRADE_PATH" eq "") {
        print "Please enter a value for the UPGRADE_PATH (currently empty).\n";
        exit (0);
    }
    if (! -d "$UPGRADE_PATH") {
        print "The path defined in UPGRADE_PATH does not exist (path=$UPGRADE_PATH)\n";
        exit (0);
    }
    my @ZIPfiles = glob( $UPGRADE_PATH . '/*.zip' );
    if ($#ZIPfiles < 0) {
        print "UPGRADE_PATH doesn't contain any upgrade zip-file.\n";
        exit (0);
    }

    # stop connexo
    print "Stopping Connexo services\n";
    if ("$OS" eq "MSWin32" || "$OS" eq "MSWin64") {
        system("sc stop Connexo$UPGRADE_OLD_SERVICE_VERSION");
        system("sc stop ConnexoTomcat$UPGRADE_OLD_SERVICE_VERSION");
        my $STATE_STRING="-1";
		while (($STATE_STRING ne "0") && ($STATE_STRING ne "1")) {
			sleep 3;
            $STATE_STRING=(`sc query Connexo$UPGRADE_OLD_SERVICE_VERSION`);
            $STATE_STRING =~ s/.*(STATE\s*:\s\d).*/$1/sg;
            $STATE_STRING =~ s/.*: //g;
            $STATE_STRING = $STATE_STRING*1;
		}
        $STATE_STRING="-1";
		while (($STATE_STRING ne "0") && ($STATE_STRING ne "1")) {
			sleep 3;
            $STATE_STRING=(`sc query ConnexoTomcat$UPGRADE_OLD_SERVICE_VERSION`);
            $STATE_STRING =~ s/.*(STATE\s*:\s\d).*/$1/sg;
            $STATE_STRING =~ s/.*: //g;
            $STATE_STRING = $STATE_STRING*1;
		}
    } else {
        system("/sbin/service Connexo$UPGRADE_OLD_SERVICE_VERSION stop");
		system("/sbin/service ConnexoTomcat$UPGRADE_OLD_SERVICE_VERSION stop");
    }

    #remove old obsolete folders
    if ( -d "$CONNEXO_DIR/bin_obsolete") { rmtree("$CONNEXO_DIR/bin_obsolete"); }
    if ( -d "$CONNEXO_DIR/bundles_obsolete") { rmtree("$CONNEXO_DIR/bundles_obsolete"); }
    if ( -d "$CONNEXO_DIR/lib_obsolete") { rmtree("$CONNEXO_DIR/lib_obsolete"); }
    if ( -d "$CONNEXO_DIR/licenses_obsolete") { rmtree("$CONNEXO_DIR/licenses_obsolete"); }
    if ( -d "$CONNEXO_DIR/partners_obsolete") { rmtree("$CONNEXO_DIR/partners_obsolete"); }
    if ( -e "$CONNEXO_DIR/conf/config.properties_obsolete") { unlink("$CONNEXO_DIR/conf/config.properties_obsolete"); }

    #rename bundles folder
    print "Renaming bundles to bundles_obsolete\n";
    rename("$CONNEXO_DIR/bundles","$CONNEXO_DIR/bundles_obsolete");
    print "Creating new bundles folder\n";
    make_path("$CONNEXO_DIR/bundles");

    foreach my $zipfile (@ZIPfiles) {
        print "Extracting $zipfile\n";
        my $zip = Archive::Zip->new($zipfile);
        $zip->extractTree("","$UPGRADE_PATH/temp/");

        #copy content of bundles folder
        if (! -d "$UPGRADE_PATH/temp/bundles") {
            print "No bundles folder found in $zipfile.\n";
        } else {
            print "Copying upgrade bundles\n";
            my @JARfiles = glob( $UPGRADE_PATH . '/temp/bundles/*.jar' );
            foreach my $jarfile (@JARfiles) {
                copy("$jarfile","$CONNEXO_DIR/bundles");
            }
        }
        #rmtree("$UPGRADE_PATH/temp");
    }

    print "Pass 1\n";
    chdir "$CONNEXO_DIR/bundles";
    my @NEW_JARS;
    my @JARfiles = glob( $CONNEXO_DIR . '/bundles/*.jar' );
    foreach my $jarfile (@JARfiles) {
        #check differences with obsolete bundle
        print "Processing $jarfile...\n";
        system("\"$JAVA_HOME/bin/jar\" -xf $jarfile META-INF/MANIFEST.MF");
        my @VALUES=find_string_value("$CONNEXO_DIR/bundles/META-INF/MANIFEST.MF");
        push(@NEW_JARS,\@VALUES);
        rmtree("$CONNEXO_DIR/bundles/META-INF");
    }

    print "\nPass 2\n";
    chdir "$CONNEXO_DIR/bundles_obsolete";
    my @OLD_JARS;
    my @JARfiles = glob( $CONNEXO_DIR . '/bundles_obsolete/*.jar' );
    foreach my $jarfile (@JARfiles) {
        #check differences with obsolete bundle
        print "Processing $jarfile...\n";
        system("\"$JAVA_HOME/bin/jar\" -xf $jarfile META-INF/MANIFEST.MF");
        my @VALUES=find_string_value("$CONNEXO_DIR/bundles_obsolete/META-INF/MANIFEST.MF");
        push(@OLD_JARS,\@VALUES);
        rmtree("$CONNEXO_DIR/bundles_obsolete/META-INF");
    }
    chdir "$CONNEXO_DIR/bin";

    open my $upgrade_log, '>>', $UPGRADE_PATH.'/upgrade.log' or die "$!";

    print_screen_file($upgrade_log,"\nUpgrade performed at ".localtime(time)."\n");
    print_screen_file($upgrade_log,"\nUpdated bundles:\n");
    print_screen_file($upgrade_log,"================\n");
    for my $i (0 .. $#NEW_JARS) {
        my @result = map { $OLD_JARS[$_][1], $OLD_JARS[$_][2] }
               grep { $NEW_JARS[$i][0] eq $OLD_JARS[$_][0] }
                 0 .. $#OLD_JARS;  
        if (("$NEW_JARS[$i][1]" ne "$result[0]") && ("$result[0]" ne "")) {
            print_screen_file($upgrade_log,"    $NEW_JARS[$i][0]\n");
            print_screen_file($upgrade_log,"        NEW version: $NEW_JARS[$i][1] (git: $NEW_JARS[$i][2])\n");
            print_screen_file($upgrade_log,"        OLD version: $result[0] (git: $result[1])\n");
        }
    }

    print_screen_file($upgrade_log,"\nNew bundles:\n");
    print_screen_file($upgrade_log,"============\n");
    for my $i (0 .. $#NEW_JARS) {
        my @result = map { $OLD_JARS[$_][1], $OLD_JARS[$_][2] }
               grep { $NEW_JARS[$i][0] eq $OLD_JARS[$_][0] }
                 0 .. $#OLD_JARS;  
        if ("$result[0]" eq "") {
            print_screen_file($upgrade_log,"    $NEW_JARS[$i][0]\n");
            print_screen_file($upgrade_log,"        Version: $NEW_JARS[$i][1] (git: $NEW_JARS[$i][2])\n");
        }
    }

    print_screen_file($upgrade_log,"\nRemoved bundles:\n");
    print_screen_file($upgrade_log,"================\n");
    for my $i (0 .. $#OLD_JARS) {
        my @result = map { $NEW_JARS[$_][1], $NEW_JARS[$_][2] }
               grep { $OLD_JARS[$i][0] eq $NEW_JARS[$_][0] }
                 0 .. $#NEW_JARS;  
        if ("$result[0]" eq "") {
            print_screen_file($upgrade_log,"    $OLD_JARS[$i][0]\n");
            print_screen_file($upgrade_log,"        Version: $OLD_JARS[$i][1] (git: $OLD_JARS[$i][2])\n");
        }
    }

    close($upgrade_log);
    
    print "\n\n";
    print "Make sure you have made a backup of your oracle schemas before starting the upgrade.\n";
    print "Without backup you won't be able to re-install Connexo if changes were already made to the oracle schemas.\n\n";
    print "If, and only if, this is an upgrade from 10.1 to 10.2 you need to execute the following sql script on the Connexo database : flywaymeta.sql\n";
    print "(make sure to use ',' (comma) as decimal separator)\n";
    print "This file can be found in the folder ".dirname(abs_path($0))."\n\n";
    my $CONT_UPG = "";
    while (("$CONT_UPG" ne "yes") && ("$CONT_UPG" ne "no")) {
        print "Are you sure you want to do the upgrade (yes/no): ";
        chomp($CONT_UPG=<STDIN>);
    }
    if ("$CONT_UPG" eq "no") {
        #upgrade cancelled; revert bundles folder
        open my $upgrade_log, '>>', $UPGRADE_PATH.'/upgrade.log' or die "$!";
        print_screen_file($upgrade_log,"\nUpgrade cancelled!!!\n");
        close($upgrade_log);
        rmtree("$CONNEXO_DIR/bundles");
        rename("$CONNEXO_DIR/bundles_obsolete","$CONNEXO_DIR/bundles");
		
		# start connexo & tomcat
		print "\nStarting Connexo & ConnexoTomcat services\n";
		if ("$OS" eq "MSWin32" || "$OS" eq "MSWin64") {
			system("sc start Connexo$UPGRADE_OLD_SERVICE_VERSION");
			system("sc start ConnexoTomcat$UPGRADE_OLD_SERVICE_VERSION");
		} else {
			system("/sbin/service Connexo$UPGRADE_OLD_SERVICE_VERSION start");
			system("/sbin/service ConnexoTomcat$UPGRADE_OLD_SERVICE_VERSION start");
		}
    } else {
        #also start copying all other folders

        #rename lib folder
        print "Renaming lib to lib_obsolete\n";
        rename("$CONNEXO_DIR/lib","$CONNEXO_DIR/lib_obsolete");
        print "Creating new lib folder\n";
        make_path("$CONNEXO_DIR/lib");

        #rename licenses folder
        print "Renaming licenses to licenses_obsolete\n";
        rename("$CONNEXO_DIR/licenses","$CONNEXO_DIR/licenses_obsolete");
        print "Creating new licenses folder\n";
        make_path("$CONNEXO_DIR/licenses");

        #uninstall old Connexo service version
		if ("$OS" eq "MSWin32" || "$OS" eq "MSWin64") {
			print "Stop and remove Connexo$UPGRADE_OLD_SERVICE_VERSION service";
			system("\"$CONNEXO_DIR/bin/ConnexoService.exe\" /uninstall Connexo$UPGRADE_OLD_SERVICE_VERSION");
		} else {
			print "Stop and remove Connexo$UPGRADE_OLD_SERVICE_VERSION service";
			system("/sbin/service Connexo$UPGRADE_OLD_SERVICE_VERSION stop");
			sleep 3;
			system("pgrep -u connexo | xargs kill -9");
			sleep 3;
			system("/sbin/chkconfig --del Connexo$UPGRADE_OLD_SERVICE_VERSION");
			unlink("/etc/init.d/Connexo$UPGRADE_OLD_SERVICE_VERSION");
		}
	
        #rename bin folder
        print "Renaming bin to bin_obsolete\n";
        make_path("$CONNEXO_DIR/bin_obsolete");
        my @BINfiles = glob( $CONNEXO_DIR . '/bin/*' );
        foreach my $binfile (@BINfiles) {
            if ((basename($binfile) eq "config.cmd") || (basename($binfile) eq "install.exe") || (basename($binfile) eq "install.pl")) {
                copy("$binfile","$CONNEXO_DIR/bin_obsolete");
            } else {
                move("$binfile","$CONNEXO_DIR/bin_obsolete/".basename($binfile));
            }
        }

        #uninstall old tomcat service version
        uninstall_tomcat_for_upgrade();

        #rename partners folder
        print "Copying partners to partners_obsolete\n";
        rename("$CONNEXO_DIR/partners","$CONNEXO_DIR/partners_obsolete");
        make_path("$CONNEXO_DIR/partners");

        #recreate config.properties
        if (! -d "$UPGRADE_PATH/temp/conf") {
            print "No conf folder found in $UPGRADE_PATH/temp.\n";
        } else {
            print "Copying and adapting config.properties\n";
            rename("$config_file","$config_file"."_obsolete");
            copy("$UPGRADE_PATH/temp/conf/config.properties.temp","$config_file") or die "File cannot be copied: $!";
            add_to_file_if($config_file,"org.osgi.service.http.port=$CONNEXO_HTTP_PORT");
            add_to_file_if($config_file,"com.elster.jupiter.datasource.jdbcurl=$jdbcUrl");
            add_to_file_if($config_file,"com.elster.jupiter.datasource.jdbcuser=$dbUserName");
            add_to_file_if($config_file,"com.elster.jupiter.datasource.jdbcpassword=$dbPassword");
            if ("$INSTALL_FACTS" eq "yes") {
                add_to_file_if($config_file,"com.elster.jupiter.yellowfin.url=http://$HOST_NAME:$TOMCAT_HTTP_PORT/facts");
                add_to_file_if($config_file,"com.elster.jupiter.yellowfin.user=$CONNEXO_ADMIN_ACCOUNT");
                add_to_file_if($config_file,"com.elster.jupiter.yellowfin.password=$CONNEXO_ADMIN_PASSWORD");
                if ("$ACTIVATE_SSO" eq "yes") {
                    add_to_file_if($config_file,"com.elster.jupiter.yellowfin.externalurl=http://$HOST_NAME/facts/");
                }
            }
            if ("$INSTALL_FLOW" eq "yes") {
                if ("$ACTIVATE_SSO" eq "yes") {
                    replace_in_file($config_file,"com.elster.jupiter.bpm.user=","#com.elster.jupiter.bpm.user=");
                    replace_in_file($config_file,"com.elster.jupiter.bpm.password=","#com.elster.jupiter.bpm.password=");
                    add_to_file_if($config_file,"com.elster.jupiter.bpm.url=http://$HOST_NAME/flow/");
                } else {
                    add_to_file_if($config_file,"com.elster.jupiter.bpm.url=http://$HOST_NAME:$TOMCAT_HTTP_PORT/flow");
                    add_to_file_if($config_file,"com.elster.jupiter.bpm.user=$CONNEXO_ADMIN_ACCOUNT");
                    add_to_file_if($config_file,"com.elster.jupiter.bpm.password=$TOMCAT_ADMIN_PASSWORD");
                }
            }
            add_to_file_if($config_file,"upgrade=true");
        }

        #copy content of lib folder
        if (! -d "$UPGRADE_PATH/temp/lib") {
            print "No lib folder found in $UPGRADE_PATH/temp.\n";
        } else {
            print "Copying upgrade lib\n";
            dircopy("$UPGRADE_PATH/temp/lib","$CONNEXO_DIR/lib");
        }

        #copy content of licenses folder
        if (! -d "$UPGRADE_PATH/temp/licenses") {
            print "No licenses folder found in $UPGRADE_PATH/temp.\n";
        } else {
            print "Copying upgrade licenses\n";
            dircopy("$UPGRADE_PATH/temp/licenses","$CONNEXO_DIR/licenses");
        }

        #copy content of bin folder
        if (! -d "$UPGRADE_PATH/temp/bin") {
            print "No bin folder found in $UPGRADE_PATH/temp.\n";
        } else {
            print "Copying upgrade bin\n";
            my @BINfiles = glob( $UPGRADE_PATH . '/temp/bin/*' );
            foreach my $binfile (@BINfiles) {
                if ((basename($binfile) ne "config.cmd") && (basename($binfile) ne "install.exe") && (basename($binfile) ne "install.pl")) {
                    copy("$binfile","$CONNEXO_DIR/bin");
                }
            }
        }

        #copy content of partners folder
        if (! -d "$UPGRADE_PATH/temp/partners") {
            print "No partners folder found in $UPGRADE_PATH/temp.\n";
        } else {
            print "Copying upgrade partners\n";
            dircopy("$UPGRADE_PATH/temp/partners","$CONNEXO_DIR/partners");

            print "Starting upgrade of partners\n";
            my $upgrade_params = "\"$JAVA_HOME\" $UPGRADE_OLD_SERVICE_VERSION $SERVICE_VERSION $FLOW_JDBC_URL $FLOW_DB_USER $FLOW_DB_PASSWORD";
            my $upgrade_exe = "$UPGRADE_PATH/temp/partners/upgrade.pl";
            if ("$OS" eq "MSWin32" || "$OS" eq "MSWin64") {
                $upgrade_exe = "$UPGRADE_PATH/temp/partners/upgrade.exe";
            } else {
                chmod 0755,"$UPGRADE_PATH/temp/partners/upgrade.pl";
            }
            if (-e "$upgrade_exe") {
                system("$upgrade_exe $upgrade_params") == 0 or die "Could not execute partners upgrade script!";
            } else {
                print "No upgrade of facts/flow found.\n";
            }
        }

        print "Install new Connexo service version";
        install_connexo();

        print "Install new versions for tomcat and partner apps";
        install_tomcat();

        dircopy("$CONNEXO_DIR/partners_obsolete/tomcat/webapps/facts", "$CONNEXO_DIR/partners/tomcat/webapps/facts");
        make_path("$UPGRADE_PATH/temp/partners/facts/unpacked");
        chdir "$UPGRADE_PATH/temp/partners/facts/unpacked";
        system("\"$JAVA_HOME/bin/jar\" -xf \"$UPGRADE_PATH/temp/partners/facts/facts.jar\"") == 0 or die "$JAVA_HOME/bin/jar -xvf \"$UPGRADE_PATH/temp/partners/facts/facts.jar\" failed: $?";
        dircopy("$UPGRADE_PATH/temp/partners/facts/unpacked/resources/customcss", "$CONNEXO_DIR/partners/tomcat/webapps/facts/customcss");
        dircopy("$UPGRADE_PATH/temp/partners/facts/unpacked/resources/customimages", "$CONNEXO_DIR/partners/tomcat/webapps/facts/customimages");
        copy("$UPGRADE_PATH/temp/partners/facts/unpacked/resources/header.jsp", "$CONNEXO_DIR/partners/tomcat/webapps/facts/header.jsp");
        copy("$UPGRADE_PATH/temp/partners/facts/unpacked/resources/index_jupiter.jsp", "$CONNEXO_DIR/partners/tomcat/webapps/facts/index_jupiter.jsp");
        chdir "$CONNEXO_DIR/bin";

        install_flow();

        #copy existing flow repository
        print "Copying Flow repository\n";
        dircopy("$CONNEXO_DIR/partners_obsolete/tomcat/.niogit","$CONNEXO_DIR/partners/tomcat/.niogit");
        dircopy("$CONNEXO_DIR/partners_obsolete/tomcat/repositories", "$CONNEXO_DIR/partners/tomcat/repositories");

        activate_sso();
        change_owner();

        print "Removing felix-cache\n";
        rmtree("$CONNEXO_DIR/felix-cache");

        print "Starting Connexo...";
        start_connexo();
        start_tomcat(); #will also upgrade Flow processes & Facts reports

        rmtree("$UPGRADE_PATH/temp");

        # final note
        print "\nIMPORTANT: if the upgrade was successful, you can remove all temporary \"obsolete\" folders/files:\n";
        print   "           - $CONNEXO_DIR/bin_obsolete\n";
        print   "           - $CONNEXO_DIR/bundles_obsolete\n";
        print   "           - $CONNEXO_DIR/lib_obsolete\n";
        print   "           - $CONNEXO_DIR/licenses_obsolete\n";
        print   "           - $CONNEXO_DIR/partners_obsolete\n";
        print   "           - $CONNEXO_DIR/conf/config.properties_obsolete\n";
    }
}

sub show_help {
    print "\n";
    print "    Usage: $0 <option>\n";
    print "\n";
    print "    Possible options\n";
    print "        --help         : show this help function\n";
    print "        --version      : version of this installation script\n";
    print "        --config       : start unattended installation; using the values in bin/config.cmd\n";
    print "        no option      : start installation with console input\n";
    print "        --uninstall    : remove installation; ; using the values in bin/config.cmd\n";
    print "        --uninstallcmd : remove installation with console input\n";
}

# Main
print ",---------------------------------------------------------,\n";
print "| Installation script started at ".localtime(time)." |\n";
print "'---------------------------------------------------------'\n";
check_root();
check_create_users();
read_args();
if ($help) {
    show_help();
} elsif ($install) {
	read_config();
    if ("$UPGRADE" eq "yes") {
        perform_upgrade();
    } else {
        checking_ports();
        install_connexo();
        install_tomcat();
        install_wso2();
        install_facts();
        install_flow();
        activate_sso();
        change_owner();
        start_connexo();
        start_tomcat();
        final_steps();
    }
} else {
	read_uninstall_config();
	uninstall_all();
}
chdir "$CURRENT_DIR";
print "\n\n,----------------------------------------------------------,\n";
print "| Installation script finished at ".localtime(time)." |\n";
print "'----------------------------------------------------------'\n";
