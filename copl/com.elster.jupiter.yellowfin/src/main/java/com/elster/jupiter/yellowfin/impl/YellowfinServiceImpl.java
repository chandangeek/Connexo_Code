package com.elster.jupiter.yellowfin.impl;

//import com.elster.jupiter.http.whiteboard.App;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.yellowfin.YellowfinService;
import com.google.inject.AbstractModule;
import com.hof.mi.web.service.*;
import com.hof.util.Base64;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.xml.rpc.ServiceException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component(name = "com.elster.jupiter.yellowfin", service = {YellowfinService.class}, immediate = true, property = "name=" + YellowfinService.COMPONENTNAME)
public class YellowfinServiceImpl implements YellowfinService, InstallService {
    private static final String YELLOWFIN_URL = "com.elster.jupiter.yellowfin.url";
    private static final String YELLOWFIN_WEBSERVICES_USER = "com.elster.jupiter.yellowfin.user";
    private static final String YELLOWFIN_WEBSERVICES_PASSWORD = "com.elster.jupiter.yellowfin.password";
    private static final String DEFAULT_YELLOWFIN_URL = "http://localhost:8081";

    private static final int DEFAULT_RETRY_DELAY_IN_SECONDS = 60;
    private static final String LOGOUT_QUEUE_DEST = "LogoutQueueDest";
    private static final String LOGOUT_QUEUE_SUBSC = "LogoutQueueSubsc";

    private String yellowfinHost;
    private int yellowfinPort;
    private String yellowfinUrl;
    private String yellowfinWebServiceUser;
    private String yellowfinWebServicePassword;

    private volatile MessageService messageService;

    @Activate
    public void activate(BundleContext context) {
        String url = context.getProperty(YELLOWFIN_URL);
        if (url == null || !url.startsWith("http://")) {
            url = DEFAULT_YELLOWFIN_URL;
        }

        yellowfinWebServiceUser = context.getProperty(YELLOWFIN_WEBSERVICES_USER);
        yellowfinWebServicePassword = context.getProperty(YELLOWFIN_WEBSERVICES_PASSWORD);

        yellowfinUrl = url;
        String parts[] = url.substring("http://".length()).split(":");
        yellowfinHost = parts[0];
        yellowfinPort = Integer.parseInt(parts[1]);
    }

    @Reference
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public String getYellowfinUrl(){
        return yellowfinUrl;
    }

    @Override
    public boolean importContent(String filePath) {

        FileInputStream inputStream = null;
        try {
            File file = new File(filePath);
            inputStream = new FileInputStream(file);
            byte[] data = new byte[(int)file.length()];
            inputStream.read(data);

            AdministrationServiceResponse rs = null;
            AdministrationServiceRequest rsr = new AdministrationServiceRequest();
            AdministrationServiceService ts = new AdministrationServiceServiceLocator(yellowfinHost, yellowfinPort, "/services/AdministrationService", false);
            AdministrationServiceSoapBindingStub rssbs = null;
            try {
                rssbs = (AdministrationServiceSoapBindingStub) ts.getAdministrationService();
            } catch (ServiceException e) {
                e.printStackTrace();
            }

            rsr.setLoginId(yellowfinWebServiceUser);
            rsr.setPassword(yellowfinWebServicePassword);
            //rsr.setPassword(null);
            rsr.setOrgId(new Integer(1));
            rsr.setFunction("IMPORTCONTENT");
            rsr.setParameters( new String[] { Base64.encodeBytes(data) } );

            if (rssbs != null) {
                try {
                    rs = rssbs.remoteAdministrationCall(rsr);
                    if (rs != null){
                        if("SUCCESS".equals(rs.getStatusCode()) ) {
                            return true;
                        }
                    }

                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
            catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

        return false;
    }

    @Override
    public String login(String username)  {

        AdministrationServiceResponse rs = null;
        AdministrationServiceRequest rsr = new AdministrationServiceRequest();
        AdministrationServiceService ts = new AdministrationServiceServiceLocator(yellowfinHost, yellowfinPort, "/services/AdministrationService", false);
        AdministrationServiceSoapBindingStub rssbs = null;
        try {
            rssbs = (AdministrationServiceSoapBindingStub) ts.getAdministrationService();
        } catch (ServiceException e) {
            e.printStackTrace();
        }

        rsr.setLoginId(yellowfinWebServiceUser);
        rsr.setPassword(yellowfinWebServicePassword);
        //rsr.setPassword(null);
        rsr.setOrgId(new Integer(1));
        rsr.setFunction("LOGINUSERNOPASSWORD");
        AdministrationPerson ap = new AdministrationPerson();
        ap.setUserId(username);
        //ap.setPassword("admin");
        rsr.setPerson(ap);

        if (rssbs != null) {
            try {
                rs = rssbs.remoteAdministrationCall(rsr);
                if (rs != null){
                    if("SUCCESS".equals(rs.getStatusCode()) ) {
                        return rs.getLoginSessionId();
                    }
                    if(rs.getErrorCode() == 9) { // license breach
                        return "LICENSE_BREACH";
                    }
                }

            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        return null;
    }



    @Override
    public boolean logout(String username)  {
        AdministrationServiceResponse rs = null;
        AdministrationServiceRequest rsr = new AdministrationServiceRequest();
        AdministrationServiceService ts = new AdministrationServiceServiceLocator(yellowfinHost, yellowfinPort, "/services/AdministrationService", false);
        AdministrationServiceSoapBindingStub rssbs = null;
        try {
            rssbs = (AdministrationServiceSoapBindingStub) ts.getAdministrationService();
        } catch (ServiceException e) {
            e.printStackTrace();
        }

        rsr.setLoginId(yellowfinWebServiceUser);
        rsr.setPassword(yellowfinWebServicePassword);
        rsr.setOrgId(new Integer(1));
        rsr.setFunction("LOGOUTUSER");
        AdministrationPerson ap = new AdministrationPerson();
        ap.setUserId(username);
        //ap.setPassword("admin");
        rsr.setPerson(ap);

// This is the Session ID
        //rsr.setLoginSessionId(sessionId);
        if (rssbs != null) {
            try {
                rs = rssbs.remoteAdministrationCall(rsr);
                if (rs != null && "SUCCESS".equals(rs.getStatusCode()) ) {
                    return true;
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    public String reloadLicense(String username)  {

        AdministrationServiceResponse rs = null;
        AdministrationServiceRequest rsr = new AdministrationServiceRequest();
        AdministrationServiceService ts = new AdministrationServiceServiceLocator(yellowfinHost, yellowfinPort, "/services/AdministrationService", false);
        AdministrationServiceSoapBindingStub rssbs = null;
        try {
            rssbs = (AdministrationServiceSoapBindingStub) ts.getAdministrationService();
        } catch (ServiceException e) {
            e.printStackTrace();
        }

        rsr.setLoginId(yellowfinWebServiceUser);
        rsr.setPassword(yellowfinWebServicePassword);
        //rsr.setPassword(null);
        rsr.setOrgId(new Integer(1));
        rsr.setFunction("RELOADLICENCE");


        if (rssbs != null) {
            try {
                rs = rssbs.remoteAdministrationCall(rsr);
                if (rs != null){
                    if("SUCCESS".equals(rs.getStatusCode()) ) {
                        return rs.getSessionId();
                    }
                    if(rs.getErrorCode() == 9) { // license breach
                        return "LICENSE_BREACH";
                    }
                }

            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    @Override
    public void install() {
        try {
            QueueTableSpec defaultQueueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
            DestinationSpec destinationSpec = defaultQueueTableSpec.createDestinationSpec(LOGOUT_QUEUE_DEST, DEFAULT_RETRY_DELAY_IN_SECONDS);
            destinationSpec.activate();
            destinationSpec.subscribe(LOGOUT_QUEUE_SUBSC);
        } catch (Exception e) {
            Logger logger = Logger.getLogger(YellowfinServiceImpl.class.getName());
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("ORM", "MSG");
    }
}
