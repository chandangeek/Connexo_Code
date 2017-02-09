package com.elster.partners.connexo.filters.facts;

import com.hof.mi.web.service.AdministrationPerson;
import com.hof.mi.web.service.AdministrationServiceRequest;
import com.hof.mi.web.service.AdministrationServiceResponse;
import com.hof.mi.web.service.AdministrationServiceService;
import com.hof.mi.web.service.AdministrationServiceServiceLocator;
import com.hof.mi.web.service.AdministrationServiceSoapBindingStub;

import javax.xml.rpc.ServiceException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Optional;

public class ConnexoFactsWebServiceManager {
    private final String host = "localhost";
    private final int port;
    private final String contextPath;
    private final String protocol;


    private final String adminUser;
    private final String adminPwd;

    ConnexoFactsWebServiceManager(int port, String contextPath, String protocol) {
        this.port = port;
        this.contextPath = contextPath;
        this.protocol = protocol;

        String usr = System.getProperty("com.elster.yellowfin.admin.usr");
        String pwd = System.getProperty("com.elster.yellowfin.admin.pwd");

        this.adminUser = (usr!=null)?usr:"admin";
        this.adminPwd = (pwd!=null)?pwd:"admin";
    }

    Optional<String> getUser(String username, List<String> roles) {
        System.out.println("YFN: Get user at " + this.protocol + "://" + this.host + ":" + this.port + this.contextPath + "/services/AdministrationService");
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
                        if(rs.getPerson().getRoleCode().equals("YFREPORTCONSUMER")){
                            return updateUser(username, roles);
                        }
                        else {
                            return Optional.of("SUCCESS");
                        }
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

    Optional<String> createUser(String username, List<String> roles) {
        System.out.println("YFN: Create user at " + this.protocol + "://" + this.host + ":" + this.port + this.contextPath + "/services/AdministrationService");
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
        if (roles.contains("Report administrator")) {
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

    Optional<String> updateUser(String username, List<String> roles) {
        System.out.println("YFN: Update user at " + this.protocol + "://" + this.host + ":" + this.port + this.contextPath + "/services/AdministrationService");
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
        if (roles.contains("Report administrator")) {
            person.setRoleCode("YFADMIN");
        } else {
            person.setRoleCode("YFCORPWRITER");
        }

        rsr.setLoginId(this.adminUser);
        rsr.setPassword(this.adminPwd);
        rsr.setOrgId(new Integer(1));
        rsr.setFunction("UPDATEUSER");
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
        System.out.println("YFN: Login at " + this.protocol + "://" + this.host + ":" + this.port + this.contextPath + "/services/AdministrationService");
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
