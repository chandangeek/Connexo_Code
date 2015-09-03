#!/usr/bin/perl
use strict;
#use warnings;
use Cwd;
use Cwd 'abs_path';
use File::Basename;
use File::Path qw(rmtree);
use File::Path qw(make_path);
use File::Copy;
use Socket;


# Define global variables
#$ENV{JAVA_HOME}="/usr/lib/jvm/jdk1.8.0";
my $INSTALL_VERSION="v20150428";
my $OS="$^O";
my $JAVA_HOME=$ENV{"JAVA_HOME"};
my $CURRENT_DIR=getcwd;
my $SCRIPT_DIR=dirname(abs_path($0));
my $CONNEXO_DIR="$SCRIPT_DIR/..";

my $parameter_file=0;
my $install=1;

my $config_file="$CONNEXO_DIR/conf/config.properties";
my $config_cmd="config.cmd";
my $SERVICE_VERSION="";

my $INSTALL_CONNEXO="yes";
my $INSTALL_FACTS="yes";
my $INSTALL_FLOW="yes";
my $INSTALL_WSO2IS="yes";

my $HOST_NAME, my $CONNEXO_HTTP_PORT, my $TOMCAT_HTTP_PORT;
my $jdbcUrl, my $dbUserName, my $dbPassword, my $CONNEXO_SERVICE, my $CONNEXO_URL;
my $FACTS_DB_HOST, my $FACTS_DB_PORT, my $FACTS_DB_NAME, my $FACTS_DBUSER, my $FACTS_DBPASSWORD, my $FACTS_LICENSE;
my $FLOW_JDBC_URL, my $FLOW_DB_USER, my $FLOW_DB_PASSWORD;
my $SMTP_HOST, my $SMTP_PORT, my $SMTP_USER, my $SMTP_PASSWORD;

my $TOMCAT_DIR="tomcat";
my $TOMCAT_BASE="$CONNEXO_DIR/partners"; 
my $TOMCAT_ZIP="tomcat-7.0.59";
my $CATALINA_BASE="$TOMCAT_BASE/$TOMCAT_DIR";
my $CATALINA_HOME=$CATALINA_BASE;
$ENV{"CATALINA_HOME"}=$CATALINA_HOME;
my $TOMCAT_SHUTDOWN_PORT="8006";
my $TOMCAT_AJP_PORT, my $TOMCAT_SSH_PORT, my $TOMCAT_DAEMON_PORT;

my $CONNEXO_ADMIN_ACCOUNT="admin";
my $CONNEXO_ADMIN_PASSWORD="admin";


# Function Definitions
sub check_root {
	if ( $> != 0 ) {
		print "Please run this script as administrator\n";
		exit (0);
	}
}

sub check_java8 {
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
				system("useradd -m -r connexo") == 0 or die "system useradd -m -r connexo failed: $?";
			}
		}
		if (("$INSTALL_FACTS" eq "yes") || ("$INSTALL_FLOW" eq "yes")) {
			if (`cat /etc/passwd|grep tomcat:` eq "") {
				system("useradd -m -r tomcat") == 0 or die "system useradd -m -r tomcat failed: $?";
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
		if ($ARGV[$i] eq "--version") {
			print "\nInstallation script version $INSTALL_VERSION\n";
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
				if ( "$val[0]" eq "HOST_NAME" )			{$HOST_NAME=$val[1];}
				if ( "$val[0]" eq "CONNEXO_HTTP_PORT" ) {$CONNEXO_HTTP_PORT=$val[1];}
				if ( "$val[0]" eq "TOMCAT_HTTP_PORT" )  {$TOMCAT_HTTP_PORT=$val[1];}
				if ( "$val[0]" eq "SERVICE_VERSION" )	{$SERVICE_VERSION=$val[1];}
				if ( "$val[0]" eq "INSTALL_CONNEXO" )	{$INSTALL_CONNEXO=$val[1];}
				if ( "$val[0]" eq "INSTALL_FACTS" )		{$INSTALL_FACTS=$val[1];}
				if ( "$val[0]" eq "INSTALL_FLOW" )		{$INSTALL_FLOW=$val[1];}
				if ( "$val[0]" eq "INSTALL_WSO2IS" )	{$INSTALL_WSO2IS=$val[1];}
				if ( "$val[0]" eq "jdbcUrl" )           {$jdbcUrl=$val[1];}
				if ( "$val[0]" eq "dbUserName" )        {$dbUserName=$val[1];}
				if ( "$val[0]" eq "dbPassword" )        {$dbPassword=$val[1];}
				if ( "$val[0]" eq "CONNEXO_SERVICE" )   {$CONNEXO_SERVICE=$val[1];}
				if ( "$val[0]" eq "FACTS_DB_HOST" )     {$FACTS_DB_HOST=$val[1];}
				if ( "$val[0]" eq "FACTS_DB_PORT" )     {$FACTS_DB_PORT=$val[1];}
				if ( "$val[0]" eq "FACTS_DB_NAME" )     {$FACTS_DB_NAME=$val[1];}
				if ( "$val[0]" eq "FACTS_DBUSER" )      {$FACTS_DBUSER=$val[1];}
				if ( "$val[0]" eq "FACTS_DBPASSWORD" )  {$FACTS_DBPASSWORD=$val[1];}
				if ( "$val[0]" eq "FACTS_LICENSE" )		{$FACTS_LICENSE=$val[1];}
				if ( "$val[0]" eq "FLOW_JDBC_URL" )     {$FLOW_JDBC_URL=$val[1];}
				if ( "$val[0]" eq "FLOW_DB_USER" )      {$FLOW_DB_USER=$val[1];}
				if ( "$val[0]" eq "FLOW_DB_PASSWORD" )  {$FLOW_DB_PASSWORD=$val[1];}
				if ( "$val[0]" eq "SMTP_HOST" )         {$SMTP_HOST=$val[1];}
				if ( "$val[0]" eq "SMTP_PORT" )         {$SMTP_PORT=$val[1];}
				if ( "$val[0]" eq "SMTP_USER" )         {$SMTP_USER=$val[1];}
				if ( "$val[0]" eq "SMTP_PASSWORD" )     {$SMTP_PASSWORD=$val[1];}
			}
		}
		close($FH);
	} else {
		print "Connexo parameters\n";
		print "------------------\n";
		print "Please enter the database url (format: jdbc:oracle:thin:\@dbHost:dbPort:dbSID): ";
		chomp($jdbcUrl=<STDIN>);
		print "Please enter the database user: ";
		chomp($dbUserName=<STDIN>);
		print "Please enter the database password: ";
		chomp($dbPassword=<STDIN>);
		print "Please enter the Connexo http port: ";
		chomp($CONNEXO_HTTP_PORT=<STDIN>);
		print "Do you want to install Connexo as a daemon: (yes/no)";
		chomp($CONNEXO_SERVICE=<STDIN>);
	}
	$CONNEXO_URL="http://$HOST_NAME:$CONNEXO_HTTP_PORT";
	$TOMCAT_SHUTDOWN_PORT=$TOMCAT_HTTP_PORT+10;
	$TOMCAT_AJP_PORT=$TOMCAT_HTTP_PORT+11;
	$TOMCAT_SSH_PORT=$TOMCAT_HTTP_PORT+12;
	$TOMCAT_DAEMON_PORT=$TOMCAT_HTTP_PORT+13;
}

sub read_uninstall_config {
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
	if ("$INSTALL_CONNEXO" eq "yes") { $CONNEXO_PORT=check_port($CONNEXO_HTTP_PORT); };
	if (("$INSTALL_FACTS" eq "yes") || ("$INSTALL_FLOW" eq "yes")) { $TOMCAT_PORT=check_port($TOMCAT_HTTP_PORT); };
	if ($CONNEXO_PORT>0 || $TOMCAT_PORT>0) {
		if ($CONNEXO_PORT>0) { print "Port $CONNEXO_HTTP_PORT for Connexo already in use!\n"; }
		if ($TOMCAT_PORT>0) { print "Port $TOMCAT_HTTP_PORT for Tomcat already in use!\n"; }
		exit (0);
	}
}

sub install_connexo {
	if ("$INSTALL_CONNEXO" eq "yes") {
		copy("$CONNEXO_DIR/conf/config.properties.temp","$config_file") or die "File cannot be copied: $!";
		open(my $FH,">> $config_file") or die "Could not open $config_file: $!";
		print $FH "org.osgi.service.http.port=$CONNEXO_HTTP_PORT\n";
		print $FH "com.elster.jupiter.datasource.jdbcurl=$jdbcUrl\n";
		print $FH "com.elster.jupiter.datasource.jdbcuser=$dbUserName\n";
		print $FH "com.elster.jupiter.datasource.jdbcpassword=$dbPassword\n";
		print $FH "\n";
		#print $FH "mail.smtp.host=\n";
		#print $FH "mail.user=\n";
		#print $FH "mail.password=\n";
		#print $FH "mail.from=\n";
		close($FH);

		print "\n\nInstalling Connexo database schema ...\n";
		print "==========================================================================\n";
		if ("$OS" eq "MSWin32" || "$OS" eq "MSWin64") {
			system("\"$SCRIPT_DIR/Connexo.exe\" --install");
			if ("$CONNEXO_SERVICE" eq "yes") {
				system("\"$SCRIPT_DIR/ConnexoService.exe\" /install Connexo$SERVICE_VERSION");
			}
		} else {
			my $VM_OPTIONS="-Djava.util.logging.config.file=\"$CONNEXO_DIR/conf/logging.properties\"";
			my $CLASSPATH = join(":", ".", glob("$CONNEXO_DIR/lib/*.jar"));
			system("\"$JAVA_HOME/bin/java\" $VM_OPTIONS -cp \"$CLASSPATH\" com.elster.jupiter.launcher.ConnexoLauncher --install");
			if ("$CONNEXO_SERVICE" eq "yes") {
				copy("$CONNEXO_DIR/bin/connexo","/etc/init.d/connexo") or die "File cannot be copied: $!";
				chmod 0755,"/etc/init.d/connexo";
				replace_in_file("/etc/init.d/connexo",'\${CONNEXO_DIR}',"$CONNEXO_DIR");
				copy("$CONNEXO_DIR/bin/start-connexo.temp","$CONNEXO_DIR/bin/start-connexo.sh") or die "File cannot be copied: $!";
				chmod 0755,"$CONNEXO_DIR/bin/start-connexo.sh";
				replace_in_file("$CONNEXO_DIR/bin/start-connexo.sh",'\${CONNEXO_DIR}',"$CONNEXO_DIR");
				replace_in_file("$CONNEXO_DIR/bin/start-connexo.sh",'\${JAVA_HOME}',"$JAVA_HOME");
			}
		}
	}
}

sub install_tomcat {
	if (("$INSTALL_FACTS" eq "yes") || ("$INSTALL_FLOW" eq "yes")) {
		print "\n\nExtracting Apache Tomcat 7 ...\n";
		print "==========================================================================\n";

		if ( !$parameter_file ) {
			print "Please enter the Tomcat http port: ";
			chomp($TOMCAT_HTTP_PORT=<STDIN>);
		}

		$ENV{JVM_OPTIONS}="-Dorg.uberfire.nio.git.ssh.port=$TOMCAT_SSH_PORT;-Dorg.uberfire.nio.git.daemon.port=$TOMCAT_DAEMON_PORT;-Dport.shutdown=$TOMCAT_SHUTDOWN_PORT;-Dport.http=$TOMCAT_HTTP_PORT;-Dconnexo.url=$CONNEXO_URL;-Dbtm.root=$CATALINA_HOME;-Dbitronix.tm.configuration=$CATALINA_HOME/conf/btm-config.properties;-Djbpm.tsr.jndi.lookup=java:comp/env/TransactionSynchronizationRegistry;-Dorg.kie.demo=false;-Dorg.kie.example=false";

		chdir "$TOMCAT_BASE";
		print "Extracting $TOMCAT_ZIP.zip\n";
		system("\"$JAVA_HOME/bin/jar\" -xf $TOMCAT_ZIP.zip") == 0 or die "system $JAVA_HOME/bin/jar -xvf $TOMCAT_ZIP.zip failed: $?";
		if (-d "$TOMCAT_DIR") { rmtree("$TOMCAT_DIR"); }
		rename("apache-$TOMCAT_ZIP","$TOMCAT_DIR");

		chdir "$TOMCAT_DIR/bin";
		replace_in_file("$TOMCAT_BASE/$TOMCAT_DIR/conf/server.xml","<Connector port=\"8009\" protocol=\"AJP/1.3\" redirectPort=\"8443\" />","<Connector port=\"$TOMCAT_AJP_PORT\" protocol=\"AJP/1.3\" redirectPort=\"8443\" />");
		print "Installing Apache Tomcat For Connexo as service ...\n";
		if ("$OS" eq "MSWin32" || "$OS" eq "MSWin64") {
			open(my $FH,"> $TOMCAT_BASE/$TOMCAT_DIR/bin/setenv.bat") or die "Could not open $TOMCAT_DIR/bin/setenv.bat: $!";
			print $FH "set CATALINA_OPTS=".$ENV{CATALINA_OPTS}." -Xmx512M -Dport.shutdown=$TOMCAT_SHUTDOWN_PORT -Dport.http=$TOMCAT_HTTP_PORT -Dconnexo.url=$CONNEXO_URL -Dbtm.root=$CATALINA_HOME -Dbitronix.tm.configuration=$CATALINA_HOME/conf/btm-config.properties -Djbpm.tsr.jndi.lookup=java:comp/env/TransactionSynchronizationRegistry -Dorg.kie.demo=false -Dorg.kie.example=false\n";
			close($FH);
			system("service.bat install ConnexoTomcat$SERVICE_VERSION");
		} else {
			open(my $FH,"> $TOMCAT_BASE/$TOMCAT_DIR/bin/setenv.sh") or die "Could not open $TOMCAT_DIR/bin/setenv.sh: $!";
			print $FH "export CATALINA_OPTS=\"".$ENV{CATALINA_OPTS}." -Xmx512M -Dorg.uberfire.nio.git.dir=$CATALINA_HOME -Dorg.uberfire.metadata.index.dir=$CATALINA_HOME -Dorg.uberfire.nio.git.ssh.cert.dir=$CATALINA_HOME -Dorg.guvnor.m2repo.dir=$CATALINA_HOME/repositories -Dport.shutdown=$TOMCAT_SHUTDOWN_PORT -Dport.http=$TOMCAT_HTTP_PORT -Dconnexo.url=$CONNEXO_URL -Dbtm.root=$CATALINA_HOME -Dbitronix.tm.configuration=$CATALINA_HOME/conf/btm-config.properties -Djbpm.tsr.jndi.lookup=java:comp/env/TransactionSynchronizationRegistry -Dorg.kie.demo=false -Dorg.kie.example=false\"\n";
			close($FH);
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
		replace_in_file("$WSO2_DIR/yajsw-stable-11.11/conf/wrapper.conf","wrapper.console.title=WSO2 Identity Server","wrapper.console.title=WSO2 Identity Server$SERVICE_VERSION");
		replace_in_file("$WSO2_DIR/yajsw-stable-11.11/conf/wrapper.conf","wrapper.ntservice.name=WSO2IS","wrapper.ntservice.name=ConnexoWSO2IS$SERVICE_VERSION");
		replace_in_file("$WSO2_DIR/yajsw-stable-11.11/conf/wrapper.conf","wrapper.ntservice.displayname=WSO2 Identity Server","wrapper.ntservice.displayname=WSO2 Identity Server$SERVICE_VERSION");
		replace_in_file("$WSO2_DIR/yajsw-stable-11.11/conf/wrapper.conf","wrapper.ntservice.description=WSO2 Identity Server","wrapper.ntservice.description=WSO2 Identity Server$SERVICE_VERSION");
		if ("$OS" eq "MSWin32" || "$OS" eq "MSWin64") {
			system("\"$WSO2_DIR/yajsw-stable-11.11/bat/installService.bat\" < NUL");
		} else {
			chmod 0755,"$WSO2_DIR/yajsw-stable-11.11/bin/installDaemon.sh";
			chmod 0755,"$WSO2_DIR/yajsw-stable-11.11/bin/installDaemonNoPriv.sh";
			chmod 0755,"$WSO2_DIR/yajsw-stable-11.11/bin/wrapper.sh";
			system("\"$WSO2_DIR/yajsw-stable-11.11/bin/installDaemon.sh\" /dev/null 2>&1");
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
		if ( !$parameter_file ) {
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
		system("\"$JAVA_HOME/bin/java\" -jar \"$CONNEXO_DIR/partners/facts/facts.jar\" -silent") == 0 or die "$JAVA_HOME/bin/java -jar \"$CONNEXO_DIR/partners/facts/facts.jar\" -silent failed: $?";
		if (!-d "$FACTS_DIR") { make_path("$FACTS_DIR"); }
		copy("$FACTS_BASE/facts.war","$FACTS_DIR/facts.war") or die "File cannot be copied: $!";
		chdir "$FACTS_DIR";
		print "Extracting facts.war\n";
		system("\"$JAVA_HOME/bin/jar\" -xf facts.war") == 0 or die "$JAVA_HOME/bin/jar -xvf facts.war failed: $?";
		unlink("$FACTS_DIR/facts.war");
		unlink("$FACTS_BASE/facts.war");
		if (-d "$FACTS_BASE/appserver") { rmtree("$FACTS_BASE/appserver"); }

		print "Connexo Facts successfully installed\n";
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

sub install_flow {
	if ("$INSTALL_FLOW" eq "yes") {
		my $FLOW_DIR="$TOMCAT_BASE/$TOMCAT_DIR/webapps/flow";
		my $FLOW_TABLESPACE="flow";
		my $FLOW_DBUSER="flow";
		my $FLOW_DBPASSWORD="flow";
		my $DEMOWS_DIR="$TOMCAT_BASE/$TOMCAT_DIR/webapps/demows";

		print "\n\nInstalling Connexo Flow ...\n";
		print "==========================================================================\n";
		if ( !$parameter_file ) {
			print "Please enter the database url for Connexo Flow (format: jdbc:oracle:thin:\@dbHost:dbPort:dbSID): ";
			chomp($FLOW_JDBC_URL=<STDIN>);
			print "Please enter the database user for Connexo Flow: ";
			chomp($FLOW_DB_USER=<STDIN>);
			print "Please enter the database password for Connexo Flow: ";
			chomp($FLOW_DB_PASSWORD=<STDIN>);

			print "Please enter the mail server host name: ";
			chomp($SMTP_HOST=<STDIN>);
			print "Please enter the mail server port: ";
			chomp($SMTP_PORT=<STDIN>);
			print "Please enter the mail server user: ";
			chomp($SMTP_USER=<STDIN>);
			print "Please enter the mail server user password: ";
			chomp($SMTP_PASSWORD=<STDIN>);
		}

		if (!-d "$FLOW_DIR") { make_path("$FLOW_DIR"); }
		copy("$CONNEXO_DIR/partners/flow/flow.war","$FLOW_DIR/flow.war");
		chdir "$FLOW_DIR";
		print "Extracting flow.war\n";
		system("\"$JAVA_HOME/bin/jar\" -xf flow.war") == 0 or die "$JAVA_HOME/bin/jar -xvf flow.war failed: $?";
		unlink("$FLOW_DIR/flow.war");

		if (!-d "$DEMOWS_DIR") { make_path("$DEMOWS_DIR"); }
		copy("$CONNEXO_DIR/partners/flow/demows.war","$DEMOWS_DIR/demows.war");
		chdir "$DEMOWS_DIR";
		print "Extracting demows.war\n";
		system("\"$JAVA_HOME/bin/jar\" -xf demows.war") == 0 or die "$JAVA_HOME/bin/jar -xvf demows.war failed: $?";
		unlink("$DEMOWS_DIR/demows.war");

		copy("$CONNEXO_DIR/partners/flow/processes.zip","$CATALINA_HOME/processes.zip");
		chdir "$CATALINA_HOME";
		print "Extracting processes.zip\n";
		system("\"$JAVA_HOME/bin/jar\" -xf processes.zip") == 0 or die "$JAVA_HOME/bin/jar -xvf processes.zip failed: $?";
		unlink("$CATALINA_HOME/processes.zip");

		copy("$CONNEXO_DIR/partners/flow/resources.properties","$CATALINA_HOME/conf/resources.properties");
		replace_in_file("$CATALINA_HOME/conf/resources.properties",'\${jdbc}',"$FLOW_JDBC_URL");
		replace_in_file("$CATALINA_HOME/conf/resources.properties",'\${user}',"$FLOW_DB_USER");
		replace_in_file("$CATALINA_HOME/conf/resources.properties",'\${password}',"$FLOW_DB_PASSWORD");

		copy("$CONNEXO_DIR/partners/flow/CustomWorkItemHandlers.conf","$CONNEXO_DIR/CustomWorkItemHandlers.conf");
		replace_in_file("$CONNEXO_DIR/CustomWorkItemHandlers.conf",'\${host}',"$SMTP_HOST");
		replace_in_file("$CONNEXO_DIR/CustomWorkItemHandlers.conf",'\${port}',"$SMTP_PORT");
		replace_in_file("$CONNEXO_DIR/CustomWorkItemHandlers.conf",'\${user}',"$SMTP_USER");
		replace_in_file("$CONNEXO_DIR/CustomWorkItemHandlers.conf",'\${password}',"$SMTP_PASSWORD");
		copy("$CONNEXO_DIR/CustomWorkItemHandlers.conf","$FLOW_DIR/WEB-INF/classes/META-INF/CustomWorkItemHandlers.conf");
		unlink("$CONNEXO_DIR/CustomWorkItemHandlers.conf");

		copy("$CONNEXO_DIR/partners/flow/SendSomeoneToInspect.bpmn","$CONNEXO_DIR/SendSomeoneToInspect.bpmn");
		replace_in_file("$CONNEXO_DIR/SendSomeoneToInspect.bpmn",'\${host}',"$HOST_NAME");
		replace_in_file("$CONNEXO_DIR/SendSomeoneToInspect.bpmn",'\${port}',"$TOMCAT_HTTP_PORT");
		chdir "$CONNEXO_DIR";
		system("\"$JAVA_HOME/bin/jar\" -uvf \"$CATALINA_HOME/repositories/kie/org/jbpm/sendsomeone/1.0/sendsomeone-1.0.jar\" SendSomeoneToInspect.bpmn") == 0 or die "$JAVA_HOME/bin/jar -uvf \"$CATALINA_HOME/repositories/kie/org/jbpm/sendsomeone/1.0/sendsomeone-1.0.jar\" SendSomeoneToInspect.bpmn failed: $?";
		unlink("$CONNEXO_DIR/SendSomeoneToInspect.bpmn");

		print "Connexo Flow successfully installed\n";
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
			open(my $FH,">> $config_file") or die "Could not open $config_file: $!";
			print $FH "com.elster.jupiter.bpm.url=http://$HOST_NAME:$TOMCAT_HTTP_PORT/flow\n";
			print $FH "com.elster.jupiter.yellowfin.url=http://$HOST_NAME:$TOMCAT_HTTP_PORT/facts\n";
			close($FH);
			if ("$OS" eq "MSWin32" || "$OS" eq "MSWin64") {
				system("sc config \"Connexo$SERVICE_VERSION\"  start= delayed-auto");
				system("sc failure \"Connexo$SERVICE_VERSION\" actions= restart/10000/restart/10000/\"\"/10000 reset= 86400");
				system("sc start Connexo$SERVICE_VERSION");
			} else {
				system("/sbin/service connexo start");
			}
		}
	}
}

sub start_tomcat {
	if (("$INSTALL_FACTS" eq "yes") || ("$INSTALL_FLOW" eq "yes")) {
		print "\n\nStarting Apache Tomcat 7 ...\n";
		print "==========================================================================\n";
		if ("$OS" eq "MSWin32" || "$OS" eq "MSWin64") {
			system("sc config \"ConnexoTomcat$SERVICE_VERSION\"  start= delayed-auto");
			system("sc failure \"ConnexoTomcat$SERVICE_VERSION\" actions= restart/10000/restart/10000/\"\"/10000 reset= 86400");
			system("sc start ConnexoTomcat$SERVICE_VERSION");
			sleep 10;
			while (`sc query ConnexoTomcat$SERVICE_VERSION | find "STATE" | find "RUNNING"` eq "") {
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
			system("\"$TOMCAT_BASE/$TOMCAT_DIR/bin/daemon.sh\" start");
		}

		if ("$INSTALL_FACTS" eq "yes") {
			print "\nInstalling Connexo Facts content...\n";
			my $ENCRYPTED_PASSWORD=`"$JAVA_HOME/bin/java" -jar \"$CONNEXO_DIR/partners/facts/EncryptPassword.jar\" $dbPassword`;

			copy("$CONNEXO_DIR/partners/facts/open-reports.xml","$CONNEXO_DIR/datasource.xml");
			replace_in_file("$CONNEXO_DIR/datasource.xml",'\${jdbc}',"$jdbcUrl");
			replace_in_file("$CONNEXO_DIR/datasource.xml",'\${user}',"$dbUserName");
			replace_in_file("$CONNEXO_DIR/datasource.xml",'\${password}',"$ENCRYPTED_PASSWORD");
			replace_in_file("$CONNEXO_DIR/datasource.xml",'\${host}',"$FACTS_DB_HOST");
			replace_in_file("$CONNEXO_DIR/datasource.xml",'\${port}',"$FACTS_DB_PORT");
			replace_in_file("$CONNEXO_DIR/datasource.xml",'\${instance}',"$FACTS_DB_NAME");

			chdir "$CONNEXO_DIR";
			system("\"$JAVA_HOME/bin/java\" -cp \"$CONNEXO_DIR/partners/facts/yellowfin.installer.jar\" com.elster.jupiter.install.reports.OpenReports datasource.xml http://$HOST_NAME:$TOMCAT_HTTP_PORT/facts $CONNEXO_ADMIN_ACCOUNT $CONNEXO_ADMIN_PASSWORD") == 0 or die "Installing Connexo Facts content failed: $?";
			system("\"$JAVA_HOME/bin/java\" -cp $CONNEXO_DIR/partners/facts/yellowfin.installer.jar com.elster.jupiter.install.reports.OpenReports datasource.xml http://$HOST_NAME:$TOMCAT_HTTP_PORT/facts $CONNEXO_ADMIN_ACCOUNT $CONNEXO_ADMIN_PASSWORD") == 0 or die "Installing Connexo Facts content failed: $?";
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
			system("\"$JAVA_HOME/bin/java\" -cp \"bundles/$BPM_BUNDLE\" com.elster.jupiter.bpm.impl.ProcessDeployer http://$HOST_NAME:$TOMCAT_HTTP_PORT/flow $CONNEXO_ADMIN_ACCOUNT $CONNEXO_ADMIN_PASSWORD") == 0 or die "Installing Connexo Flow content failed: $?";
		}
	}
}

sub uninstall_all {
	my $WSO2_DIR="$CONNEXO_DIR/partners";
	$ENV{CARBON_HOME}="$WSO2_DIR/wso2is-4.5.0";
	if ("$OS" eq "MSWin32" || "$OS" eq "MSWin64") {
		print "Stop and remove connexo service";
		system("\"$CONNEXO_DIR/bin/ConnexoService.exe\" /uninstall Connexo$SERVICE_VERSION");
		print "Stop and remove tomcat service";
		system("\"$CONNEXO_DIR/partners/tomcat/bin/service.bat\" remove ConnexoTomcat$SERVICE_VERSION");
		print "Stop and remove wso service";
		system("$CONNEXO_DIR/partners/yajsw-stable-11.11/bat/uninstallService.bat");
	} else {
		print "Stop and remove connexo service";
		system("/sbin/service connexo stop");
		sleep 3;
		system("pgrep -u connexo | xargs kill -9");
		sleep 3;
		system("userdel -r connexo");
		unlink("/etc/init.d/connexo");
		print "Stop and remove tomcat service";
		system("\"$TOMCAT_BASE/$TOMCAT_DIR/bin/daemon.sh\" stop");
		sleep 3;
		system("userdel -r tomcat");
		print "Stop and remove wso service";
		#uninstall wso
	}
	print "Remove folders (tomcat, wso2is, yajsw)\n";
	if (-d "$CONNEXO_DIR/partners/tomcat") { rmtree("$CONNEXO_DIR/partners/tomcat"); }
	if (-d "$CONNEXO_DIR/partners/wso2is-4.5.0") { rmtree("$CONNEXO_DIR/partners/wso2is-4.5.0"); }
	if (-d "$CONNEXO_DIR/partners/yajsw-stable-11.11") { rmtree("$CONNEXO_DIR/partners/yajsw-stable-11.11"); }
	if (-e "$CONNEXO_DIR/conf/config.properties") { unlink("$CONNEXO_DIR/conf/config.properties"); }
}


# Main
print ",---------------------------------------------------------,\n";
print "| Installation script started at ".localtime(time)." |\n";
print "'---------------------------------------------------------'\n";
check_root();
check_java8();
check_create_users();
read_args();
if ($install) {
	read_config();
	checking_ports();
	install_connexo();
	install_tomcat();
	install_wso2();
	install_facts();
	install_flow();
	change_owner();
	start_connexo();
	start_tomcat();
} else {
	read_uninstall_config();
	uninstall_all();
}
chdir "$CURRENT_DIR";
print "\n\n,----------------------------------------------------------,\n";
print "| Installation script finished at ".localtime(time)." |\n";
print "'----------------------------------------------------------'\n";
