package com.elster.partners.connexo.filters.facts;

import com.hof.mi.web.service.*;

import javax.xml.rpc.ServiceException;
import java.rmi.RemoteException;
import java.util.Optional;
import java.util.Properties;

/**
 * Created by dragos on 11/17/2015.
 */
public class ConnexoFactsWebServiceManager {
    private final String host = "localhost";
    private final int port;
    private final String contextPath;
    private final String protocol;


    private final String adminUser;
    private final String adminPwd;

    ConnexoFactsWebServiceManager(Properties properties, int port, String contextPath, String protocol) {
        this.port = port;
        this.contextPath = contextPath;
        this.protocol = protocol;

        String usr = properties.getProperty("com.elster.yellowfin.admin.usr");
        String pwd = properties.getProperty("com.elster.yellowfin.admin.pwd");

        this.adminUser = (usr!=null)?usr:"admin";
        this.adminPwd = (pwd!=null)?pwd:"admin";
    }

    Optional<String> getUser(String username)  {

        AdministrationServiceResponse rs = null;
        AdministrationServiceRequest rsr = new AdministrationServiceRequest();
        AdministrationServiceService ts = new AdministrationServiceServiceLocator(this.host, this.port, this.contextPath + "/services/AdministrationService", this.protocol.equals("https"));
        AdministrationServiceSoapBindingStub rssbs = null;
        try {
            rssbs = (AdministrationServiceSoapBindingStub) ts.getAdministrationService();
        } catch (ServiceException e) {
            e.printStackTrace();
        }

        AdministrationPerson person = new AdministrationPerson();

        person.setUserId(username);

        rsr.setLoginId(this.adminUser);
        rsr.setPassword(this.adminPwd);
        rsr.setOrgId(new Integer(1));
        rsr.setFunction("GETUSER");
        rsr.setPerson(person);

        if (rssbs != null) {
            try {
                rs = rssbs.remoteAdministrationCall(rsr);
                if (rs != null){
                    if("SUCCESS".equals(rs.getStatusCode()) ) {
                        return Optional.of("SUCCESS");
                    }
                    if(rs.getErrorCode() == 9) { // license breach
                        return Optional.of("LICENSE_BREACH");
                    }
                }

            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        return Optional.empty();
    }

    Optional<String> createUser(String username)  {

        AdministrationServiceResponse rs = null;
        AdministrationServiceRequest rsr = new AdministrationServiceRequest();
        AdministrationServiceService ts = new AdministrationServiceServiceLocator(this.host, this.port, this.contextPath + "/services/AdministrationService", this.protocol.equals("https"));
        AdministrationServiceSoapBindingStub rssbs = null;
        try {
            rssbs = (AdministrationServiceSoapBindingStub) ts.getAdministrationService();
        } catch (ServiceException e) {
            e.printStackTrace();
        }

        AdministrationPerson person = new AdministrationPerson();

        person.setUserId(username);
        person.setPassword("test");
        person.setFirstName("Connexo");
        person.setLastName(username);
        if(username.equals(this.adminUser)) {
            person.setRoleCode("YFADMIN");
        }
        else {
            person.setRoleCode("YFCORPWRITER");
        }
        person.setEmailAddress(username + "@elster.com");

        rsr.setLoginId(this.adminUser);
        rsr.setPassword(this.adminPwd);
        rsr.setOrgId(new Integer(1));
        rsr.setFunction("ADDUSER");
        rsr.setPerson(person);

        if (rssbs != null) {
            try {
                rs = rssbs.remoteAdministrationCall(rsr);
                if (rs != null){
                    if("SUCCESS".equals(rs.getStatusCode()) ) {
                        return Optional.of("SUCCESS");
                    }
                    if(rs.getErrorCode() == 9) { // license breach
                        return Optional.of("LICENSE_BREACH");
                    }
                }

            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        return Optional.empty();
    }

    Optional<String> login(String username)  {

        AdministrationServiceResponse rs = null;
        AdministrationServiceRequest rsr = new AdministrationServiceRequest();
        AdministrationServiceService ts = new AdministrationServiceServiceLocator(this.host, this.port, this.contextPath + "/services/AdministrationService", this.protocol.equals("https"));
        AdministrationServiceSoapBindingStub rssbs = null;
        try {
            rssbs = (AdministrationServiceSoapBindingStub) ts.getAdministrationService();
        } catch (ServiceException e) {
            e.printStackTrace();
        }

        rsr.setLoginId(this.adminUser);
        rsr.setPassword(this.adminPwd);
        rsr.setOrgId(new Integer(1));
        rsr.setFunction("LOGINUSERNOPASSWORD");
        AdministrationPerson ap = new AdministrationPerson();
        ap.setUserId(username);
        rsr.setPerson(ap);

        if (rssbs != null) {
            try {
                rs = rssbs.remoteAdministrationCall(rsr);
                if (rs != null){
                    if("SUCCESS".equals(rs.getStatusCode()) ) {
                        return Optional.of(rs.getLoginSessionId());
                    }
                    if(rs.getErrorCode() == 9) { // license breach
                        return Optional.of("LICENSE_BREACH");
                    }
                }

            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        return Optional.empty();
    }

    Optional<Boolean> logout(String username)  {
        AdministrationServiceResponse rs = null;
        AdministrationServiceRequest rsr = new AdministrationServiceRequest();
        AdministrationServiceService ts = new AdministrationServiceServiceLocator(this.host, this.port, this.contextPath + "/services/AdministrationService", this.protocol.equals("https"));
        AdministrationServiceSoapBindingStub rssbs = null;
        try {
            rssbs = (AdministrationServiceSoapBindingStub) ts.getAdministrationService();
        } catch (ServiceException e) {
            e.printStackTrace();
        }

        rsr.setLoginId(username);
        rsr.setOrgId(new Integer(1));
        rsr.setFunction("LOGOUTUSER");
        AdministrationPerson ap = new AdministrationPerson();
        ap.setUserId(username);
        rsr.setPerson(ap);

        if (rssbs != null) {
            try {
                rs = rssbs.remoteAdministrationCall(rsr);
                if (rs != null && "SUCCESS".equals(rs.getStatusCode()) ) {
                    return Optional.of(true);
                }
                else{
                    return Optional.of(false);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        return Optional.empty();
    }
}
