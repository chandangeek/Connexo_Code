In order to enable single sign on for Connexo Facts:

1. Install Apache HTTP server (https://httpd.apache.org/download.cgi)
2. Configure redirect rules as described in the example httpd.conf provided here
3. Update Felix config.properties
    com.elster.jupiter.yellowfin.url=http://host:8081/facts
    com.elster.jupiter.yellowfin.externalurl=http://host.domain.com/facts/
4. Restart Felix

Specific Facts configuration needs to be updated as follows:

1. Stop Apache Tomcat 7.0 ConnexoTomcat10.1 service
2. Edit web.xml file from <Tomcat folder>/webapps/facts/WEB-INF to uncomment SSO lines
4. Start Apache Tomcat 7.0 ConnexoTomcat10.1 service
