In order to enable single sign on for Connexo Flow:

1. Install Apache HTTP server (https://httpd.apache.org/download.cgi)
2. Configure redirect rules as described in the example httpd.conf provided here
3. Update Felix config.properties
    com.elster.jupiter.bpm.url=http://host.domain.com/flow/
    com.elster.jupiter.bpm.user=admin
    com.elster.jupiter.bpm.password=admin
4. Restart Felix

Specific Flow configuration needs to be updated as follows:

1. Stop Apache Tomcat 7.0 ConnexoTomcat10.1 service
2. Copy web.xml to <Tomcat folder>/webapps/flow/WEB-INF
3. Copy beans.xml to <Tomcat folder>/webapps/flow/WEB-INF
4. Start Apache Tomcat 7.0 ConnexoTomcat10.1 service
