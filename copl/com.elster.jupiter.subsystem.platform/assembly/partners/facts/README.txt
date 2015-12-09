In order to enable single sign on for Connexo Facts:

1. Install Apache HTTP server (https://httpd.apache.org/download.cgi)
2. Configure redirect rules as described in the example httpd.conf provided here

Specific Facts configuration needs to be updated as follows:

1. Stop Apache Tomcat 7.0 ConnexoTomcat10.1 service
2. Copy web.xml to <Tomcat folder>/webapps/facts/WEB-INF
4. Start Apache Tomcat 7.0 ConnexoTomcat10.1 service
