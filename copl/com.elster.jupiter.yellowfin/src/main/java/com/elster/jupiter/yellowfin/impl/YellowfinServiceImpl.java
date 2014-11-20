package com.elster.jupiter.yellowfin.impl;

//import com.elster.jupiter.http.whiteboard.App;

import com.elster.jupiter.yellowfin.YellowfinService;
import com.hof.mi.web.service.*;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import javax.xml.rpc.ServiceException;
import java.rmi.RemoteException;

@Component(name = "com.elster.jupiter.yellowfin", service = {YellowfinService.class}, immediate = true, property = "name=" + YellowfinService.COMPONENTNAME)
public class YellowfinServiceImpl implements YellowfinService {
    private static final String YELLOWFIN_URL = "com.elster.jupiter.yellowfin.url";
    private static final String DEFAULT_YELLOWFIN_URL = "http://localhost:8081";

    private String yellowfinHost;
    private int yellowfinPort;
    private String yellowfinUrl;

    @Activate
    public void activate(BundleContext context) {
        String url = context.getProperty(YELLOWFIN_URL);
        if (url == null || !url.startsWith("http://")) {
            url = DEFAULT_YELLOWFIN_URL;
        }

        yellowfinUrl = url;
        String parts[] = url.substring("http://".length()).split(":");
        yellowfinHost = parts[0];
        yellowfinPort = Integer.parseInt(parts[1]);

    }

    @Override
    public String getYellowfinUrl(){
        return yellowfinUrl;
    }

    @Override
    public String login(String authentication)  {
        AdministrationServiceResponse rs = null;
        AdministrationServiceRequest rsr = new AdministrationServiceRequest();
        AdministrationServiceService ts = new AdministrationServiceServiceLocator(yellowfinHost, yellowfinPort, "/services/AdministrationService", false);
        AdministrationServiceSoapBindingStub rssbs = null;
        try {
            rssbs = (AdministrationServiceSoapBindingStub) ts.getAdministrationService();
        } catch (ServiceException e) {
            e.printStackTrace();
        }

        rsr.setLoginId("admin");
        rsr.setPassword("admin");
        rsr.setOrgId(new Integer(1));
        rsr.setFunction("LOGINUSER");
        AdministrationPerson ap = new AdministrationPerson();
        ap.setUserId("admin");
        ap.setPassword("admin");
        rsr.setPerson(ap);

        if (rssbs != null) {
            try {
                rs = rssbs.remoteAdministrationCall(rsr);
                if (rs != null && "SUCCESS".equals(rs.getStatusCode()) ) {
                    return rs.getLoginSessionId();
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /*
    @Override
    public boolean logout(String username, String password, String sessionId)  {
        ReportServiceResponse rs = null;
        ReportServiceRequest  rsr = new ReportServiceRequest ();
        ReportServiceServiceLocator ts = new ReportServiceServiceLocator(yellowfinHost, yellowfinPort, "/services/ReportService", false);
        ReportServiceSoapBindingStub rssbs = null;
        try {
            rssbs = (ReportServiceSoapBindingStub ) ts.getReportService();
        }
        catch (ServiceException e) {
            return false;
        }

        rsr.setLoginId(username);
        rsr.setPassword(password);
        rsr.setOrgId(new Integer(1));
        rsr.setReportRequest("EXPIRESESSION");
        //AdministrationPerson ap = new AdministrationPerson();
        //ap.setUserId("admin");
        //ap.setPassword("admin");
        //rsr.setPerson(ap);
        rsr.setSessionId(sessionId);

        if (rssbs != null) {
            try {
                rs = rssbs.remoteReportCall(rsr);
                if (rs != null && "SUCCESS".equals(rs.getStatusCode()) ) {
                    return true;
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        return false;
    }
    */


    @Override
    public boolean logout(String username, String password, String sessionId)  {
        AdministrationServiceResponse rs = null;
        AdministrationServiceRequest rsr = new AdministrationServiceRequest();
        AdministrationServiceService ts = new AdministrationServiceServiceLocator(yellowfinHost, yellowfinPort, "/services/AdministrationService", false);
        AdministrationServiceSoapBindingStub rssbs = null;
        try {
            rssbs = (AdministrationServiceSoapBindingStub) ts.getAdministrationService();
        } catch (ServiceException e) {
            e.printStackTrace();
        }

        rsr.setLoginId(username);
        rsr.setPassword(password);
        rsr.setOrgId(new Integer(1));
        rsr.setFunction("LOGOUTUSER");
        AdministrationPerson ap = new AdministrationPerson();
        ap.setUserId("admin");
        ap.setPassword("admin");
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

}
