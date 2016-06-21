In order to enable single sign on for Connexo Flow:

1. Install Apache HTTP server (https://httpd.apache.org/download.cgi)
2. Configure redirect rules as described in the example httpd.conf provided here
3. Update Felix config.properties
    # add or update the following line
    com.elster.jupiter.bpm.url=http://host.domain.com/flow/
    # comment the following lines if present
    #com.elster.jupiter.bpm.user=admin
    #com.elster.jupiter.bpm.password=admin
4. Run Gogo command createNewTokenKey <path-to-file> to obtain export in a file the value of the public key to be used for token encryption (to be copied latter in Tomcat)
5. Restart Connexo10.2 service

Specific Flow configuration needs to be updated as follows:

1. Stop ConnexoTomcat10.2 service
2. Edit web.xml file from <Tomcat folder>/webapps/flow/WEB-INF to uncomment SSO lines and comment out default authentication
3. Edit beans.xml file from <Tomcat folder>/webapps/flow/WEB-INF to enable Connexo user sharing
4. Update <Tomcat folder>/conf/connexo.properties to specify Connexo urls (as configured in the redirect rules in httpd.conf)
    com.elster.jupiter.url=http://hostname:8080
    com.elster.jupiter.externalurl=http://hostname
5. Add the public key in the connexo.properties file as well
    com.elster.jupiter.sso.public.key=<key value copied from the exported file>
6. Start ConnexoTomcat10.2 service
