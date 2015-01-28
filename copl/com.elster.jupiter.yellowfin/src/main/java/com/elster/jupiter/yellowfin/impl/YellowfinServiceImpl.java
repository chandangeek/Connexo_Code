package com.elster.jupiter.yellowfin.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.orm.callback.InstallService;

import com.elster.jupiter.users.UserService;
import com.elster.jupiter.yellowfin.YellowfinFilterInfo;
import com.elster.jupiter.yellowfin.YellowfinFilterListItemInfo;
import com.elster.jupiter.yellowfin.YellowfinReportInfo;
import com.elster.jupiter.yellowfin.YellowfinService;
import com.elster.jupiter.yellowfin.security.Privileges;
import com.hof.mi.web.service.*;

import com.hof.util.Base64;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.xml.rpc.ServiceException;
import java.io.*;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component(name = "com.elster.jupiter.yellowfin", service = {YellowfinService.class, InstallService.class}, immediate = true, property = "name=" + YellowfinService.COMPONENTNAME)
public class YellowfinServiceImpl implements YellowfinService, InstallService {
    private static final String YELLOWFIN_URL = "com.elster.jupiter.yellowfin.url";
    private static final String YELLOWFIN_EXTERNAL_URL = "com.elster.jupiter.yellowfin.externalurl";
    private static final String YELLOWFIN_WEBSERVICES_USER = "com.elster.jupiter.yellowfin.user";
    private static final String YELLOWFIN_WEBSERVICES_PASSWORD = "com.elster.jupiter.yellowfin.password";

    private static final String DEFAULT_YELLOWFIN_URL = "http://localhost:8081";
    private static final String DEFAULT_YELLOWFIN_HOST = "localhost";
    private static final int DEFAULT_YELLOWFIN_PORT = 8081;

    private static final String DEFAULT_YELLOWFIN_USER = "admin";
    private static final String DEFAULT_YELLOWFIN_PASSWORD = "admin";

    private static final int DEFAULT_RETRY_DELAY_IN_SECONDS = 60;
    private static final String LOGOUT_QUEUE_DEST = "LogoutQueueDest";
    private static final String LOGOUT_QUEUE_SUBSC = "LogoutQueueSubsc";

    private String yellowfinUrl = DEFAULT_YELLOWFIN_URL;
    private String yellowfinExternalUrl = DEFAULT_YELLOWFIN_URL;
    private String yellowfinHost = DEFAULT_YELLOWFIN_HOST;
    private int yellowfinPort = DEFAULT_YELLOWFIN_PORT;

    private String yellowfinWebServiceUser = DEFAULT_YELLOWFIN_USER;
    private String yellowfinWebServicePassword = DEFAULT_YELLOWFIN_PASSWORD;

    private volatile MessageService messageService;
    private volatile UserService userService;

    public YellowfinServiceImpl(){

    }

    @Activate
    public void activate(BundleContext context) {

        yellowfinUrl = context.getProperty(YELLOWFIN_URL);
        yellowfinExternalUrl = context.getProperty(YELLOWFIN_EXTERNAL_URL);

        yellowfinWebServiceUser = context.getProperty(YELLOWFIN_WEBSERVICES_USER);
        yellowfinWebServiceUser = (yellowfinWebServiceUser == null) ? DEFAULT_YELLOWFIN_USER : yellowfinWebServiceUser;

        yellowfinWebServicePassword = context.getProperty(YELLOWFIN_WEBSERVICES_PASSWORD);
        yellowfinWebServicePassword = (yellowfinWebServicePassword == null) ? DEFAULT_YELLOWFIN_PASSWORD : yellowfinWebServicePassword;

        if(yellowfinUrl != null){
            Pattern pattern = Pattern.compile("(https?://)([^:^/]*):([0-9]\\d*)?(.*)?");
            Matcher matcher = pattern.matcher(yellowfinUrl);
            matcher.find();
            if(matcher.matches()){
                yellowfinHost   = matcher.group(2);
                yellowfinPort   = Integer.parseInt(matcher.group(3));
            }
        }

        yellowfinUrl = (yellowfinUrl == null) ? DEFAULT_YELLOWFIN_URL : yellowfinUrl;
        yellowfinExternalUrl = (yellowfinExternalUrl == null) ? yellowfinUrl : yellowfinExternalUrl;
    }

    @Reference
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }


    @Override
    public String getYellowfinUrl(){
        return yellowfinExternalUrl;
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
    public List<YellowfinReportInfo> getUserReports(String username, String category, String subCategory,String reportUUId)  {

        List<YellowfinReportInfo> userReports = new ArrayList<>();
        try {
            AdministrationServiceResponse rs = null;
            AdministrationServiceRequest rsr = new AdministrationServiceRequest();
            AdministrationServiceService ts = new AdministrationServiceServiceLocator(yellowfinHost, yellowfinPort, "/services/AdministrationService", false);
            AdministrationServiceSoapBindingStub rssbs = null;

            rssbs = (AdministrationServiceSoapBindingStub) ts.getAdministrationService();

            rsr.setLoginId(yellowfinWebServiceUser);
            rsr.setPassword(yellowfinWebServicePassword);
            rsr.setOrgId(new Integer(1));
            rsr.setFunction("GETALLUSERREPORTS");

            AdministrationPerson ap = new AdministrationPerson();
            ap.setUserId(username);
            rsr.setPerson(ap);
            if (rssbs != null) {
                try {
                    rs = rssbs.remoteAdministrationCall(rsr);
                    if (rs != null){
                        if("SUCCESS".equals(rs.getStatusCode()) ) {
                            AdministrationReport[] reports = rs.getReports();
                            for (int i = 0; i < reports.length; i++) {
                                AdministrationReport report = reports[i];
                                YellowfinReportInfoImpl userReport = new YellowfinReportInfoImpl();
                                if ((category == null || category.equalsIgnoreCase(report.getReportCategory())) &&
                                        (subCategory == null || subCategory.equalsIgnoreCase(report.getReportSubCategory())) &&
                                        (reportUUId == null || reportUUId.equalsIgnoreCase(report.getReportUUID()))) {
                                    userReport.setCategory(report.getReportCategory());
                                    userReport.setName(report.getReportName());
                                    userReport.setSubCategory(report.getReportSubCategory());
                                    userReport.setDescription(report.getReportDescription());
                                    userReport.setReportUUID(report.getReportUUID());
                                    userReport.setDescription(report.getReportDescription());
                                    userReport.setReportId(report.getReportId());
                                    userReports.add(userReport);

                                }
                            }
                        }
                    }

                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        } catch (ServiceException e) {
            e.printStackTrace();
        }


        return userReports;
    }

    @Override
    public List<YellowfinFilterInfo> getReportFilters(int reportId) {
        List<YellowfinFilterInfo> userFilters = new ArrayList<>();
        try {
            ReportServiceService ts = new ReportServiceServiceLocator(yellowfinHost, yellowfinPort, "/services/ReportService", false);
            ReportServiceSoapBindingStub rssbs;
            ReportServiceRequest rsr = new ReportServiceRequest();

            rssbs = (ReportServiceSoapBindingStub) ts.getReportService();
            ReportServiceResponse reportServiceResponse = null;
            rsr.setLoginId(yellowfinWebServiceUser);
            rsr.setPassword(yellowfinWebServicePassword);
            rsr.setOrgId(new Integer(1));
            rsr.setReportRequest("SCHEMA");
            rsr.setReportId(reportId);
            try {
                reportServiceResponse = rssbs.remoteReportCall(rsr);
                if(reportServiceResponse != null){
                    if("SUCCESS".equals(reportServiceResponse.getStatusCode())){
                        ReportSchema[] rs = reportServiceResponse.getColumns();
                        for(int i = 0 ; i<rs.length;i++){
                            if(rs[i].getFilterId()!=null){
                                YellowfinFilterInfoImpl userFilter = new YellowfinFilterInfoImpl();
                                userFilter.setId(rs[i].getFilterId());
                                userFilter.setFilterDataValue1(rs[i].getDefaultValue1());
                                userFilter.setFilterDataValue2(rs[i].getDefaultValue2());
                                userFilter.setFilterDisplayType(rs[i].getFilterDisplayType());
                                userFilter.setFilterName(rs[i].getColumnName());
                                userFilter.setFilterType(rs[i].getFilterType());
                                userFilter.setFilterOmittable(rs[i].getFilterOmittable());
                                userFilter.setFilterDisplayName(rs[i].getDisplayName());
                                userFilter.setFilterPrompt(rs[i].getPrompt());
                                userFilter.setFilterAllowPrompt(rs[i].getAllowPrompt());
                                userFilter.setFilterMaxValue(rs[i].getMaximumValue());
                                userFilter.setFilterMinValue(rs[i].getMinimumValue());
                                userFilters.add(userFilter);
                                //ReportRow[] reportRows = getFilterList(rs[i].getFilterId().toString(),reportId);
                                //for(int j=0;j<reportRows.length;j++){
                                //    System.out.println(reportRows[j].getDataValue()[0]);

                                //}
                            }
                        }
                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userFilters;

    }

    public ReportRow[] getFilterList(String filterId,int reportId) {
        ReportServiceService ts = new ReportServiceServiceLocator(yellowfinHost, yellowfinPort, "/services/ReportService", false);
        ReportServiceSoapBindingStub rssbs;
        ReportServiceRequest rsr = new ReportServiceRequest();
        ReportRow[] reportRows = null;
        try {
            rssbs = (ReportServiceSoapBindingStub) ts.getReportService();
            ReportServiceResponse reportServiceResponse = null;
            rsr.setLoginId(yellowfinWebServiceUser);
            rsr.setPassword(yellowfinWebServicePassword);
            rsr.setOrgId(new Integer(1));
            rsr.setReportRequest("FILTEROPTIONS");
            rsr.setObjectName(filterId);
            rsr.setReportId(reportId);
            try {
                reportServiceResponse = rssbs.remoteReportCall(rsr);
                if (reportServiceResponse != null) {
                    if ("SUCCESS".equals(reportServiceResponse.getStatusCode())) {
                        reportRows= reportServiceResponse.getResults();
                    }
                }

            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return reportRows;
    }


    public List<YellowfinFilterListItemInfo> getFilterListItems(String filterId,int reportId) {
        List<YellowfinFilterListItemInfo> listItems = new ArrayList<>();
        try {
            ReportServiceService ts = new ReportServiceServiceLocator(yellowfinHost, yellowfinPort, "/services/ReportService", false);
            ReportServiceSoapBindingStub rssbs;
            ReportServiceRequest rsr = new ReportServiceRequest();


            rssbs = (ReportServiceSoapBindingStub) ts.getReportService();
            ReportServiceResponse reportServiceResponse = null;
            rsr.setLoginId(yellowfinWebServiceUser);
            rsr.setPassword(yellowfinWebServicePassword);
            rsr.setOrgId(new Integer(1));
            rsr.setReportRequest("FILTEROPTIONS");
            rsr.setObjectName(filterId);
            rsr.setReportId(reportId);
            try {
                reportServiceResponse = rssbs.remoteReportCall(rsr);
                if (reportServiceResponse != null) {
                    if ("SUCCESS".equals(reportServiceResponse.getStatusCode())) {
                        ReportRow[] reportRows = reportServiceResponse.getResults();
                        for(int i=0;i<reportRows.length;i++){
                            String[] dataValues = reportRows[i].getDataValue();
                            YellowfinFilterListItemInfo listItem = new YellowfinFilterListItemInfoImpl();
                            if(dataValues[0].contains("__##SEARCH_RESULTS##__"))
                                continue;
                            listItem.setValue1(dataValues[0]);
                            listItem.setValue2(dataValues[1]);
                            listItems.add(listItem);
                        }
                    }
                }

            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listItems;
    }



    @Override
    public void install() {
        try {
            QueueTableSpec defaultQueueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
            DestinationSpec destinationSpec = defaultQueueTableSpec.createDestinationSpec(LOGOUT_QUEUE_DEST, DEFAULT_RETRY_DELAY_IN_SECONDS);
            destinationSpec.activate();
            destinationSpec.subscribe(LOGOUT_QUEUE_SUBSC);
            createPrivileges(userService);

        } catch (Exception e) {
            Logger logger = Logger.getLogger(YellowfinServiceImpl.class.getName());
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("ORM", "MSG", "USR");
    }

    private void createPrivileges(UserService userService) {
        userService.createResourceWithPrivileges("MDC", "reportMdc.reports", "reportMdc.reports.description", new String[] { Privileges.VIEW_REPORTS });
        userService.createResourceWithPrivileges("YFN", "reportYfn.reports", "reportYfn.reports.description", new String[] { Privileges.DESIGN_REPORTS });
    }

}
