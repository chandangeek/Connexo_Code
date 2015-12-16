In order to enable single sign on for Connexo Flow:

1. Install Apache HTTP server (https://httpd.apache.org/download.cgi)
2. Configure redirect rules as described in the example httpd.conf provided here
3. Update Felix config.properties
    com.elster.jupiter.bpm.url=http://host.domain.com/flow/
    com.elster.jupiter.bpm.user=admin
    com.elster.jupiter.bpm.password=admin
4. Restart Felix
5. Run Gogo command getPublicKey to obtain the value of the public key to be used for token encryption (to be copied latter in Tomcat)

Specific Flow configuration needs to be updated as follows:

1. Stop Apache Tomcat 7.0 ConnexoTomcat10.1 service
2. Edit web.xml file from <Tomcat folder>/webapps/facts/WEB-INF to uncomment SSO lines and comment out default authentication
3. Edit beans.xml file from <Tomcat folder>/webapps/flow/WEB-INF to enable Connexo user sharing
4. Update <Tomcat folder>/conf/connexo.properties to specify Connexo urls (as configured in the redirect rules in httpd.conf)
    com.elster.jupiter.url=http://hostname:8080
    com.elster.jupiter.externalurl=http://hostname
5. Add the public key in the connexo.properties file as well
    com.elster.jupiter.public.key=<key value copied from the Gogo command output>
6. Start Apache Tomcat 7.0 ConnexoTomcat10.1 service
