/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.yellowfin.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.SimpleTranslationKey;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.yellowfin.YellowfinFilterInfo;
import com.elster.jupiter.yellowfin.YellowfinFilterListItemInfo;
import com.elster.jupiter.yellowfin.YellowfinReportInfo;
import com.elster.jupiter.yellowfin.YellowfinService;
import com.elster.jupiter.yellowfin.security.Privileges;

import com.google.inject.AbstractModule;
import com.hof.mi.web.service.AdministrationPerson;
import com.hof.mi.web.service.AdministrationReport;
import com.hof.mi.web.service.AdministrationServiceRequest;
import com.hof.mi.web.service.AdministrationServiceResponse;
import com.hof.mi.web.service.AdministrationServiceService;
import com.hof.mi.web.service.AdministrationServiceServiceLocator;
import com.hof.mi.web.service.AdministrationServiceSoapBindingStub;
import com.hof.mi.web.service.ReportRow;
import com.hof.mi.web.service.ReportSchema;
import com.hof.mi.web.service.ReportServiceRequest;
import com.hof.mi.web.service.ReportServiceResponse;
import com.hof.mi.web.service.ReportServiceService;
import com.hof.mi.web.service.ReportServiceServiceLocator;
import com.hof.mi.web.service.ReportServiceSoapBindingStub;
import com.hof.util.Base64;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.xml.rpc.ServiceException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component(name = "com.elster.jupiter.yellowfin", service = {YellowfinService.class, TranslationKeyProvider.class}, immediate = true, property = "name=" + YellowfinService.COMPONENTNAME)
@SuppressWarnings("unused")
public class YellowfinServiceImpl implements YellowfinService, TranslationKeyProvider {
    private static final String YELLOWFIN_URL = "com.elster.jupiter.yellowfin.url";
    private static final String YELLOWFIN_EXTERNAL_URL = "com.elster.jupiter.yellowfin.externalurl";
    private static final String YELLOWFIN_WEBSERVICES_USER = "com.elster.jupiter.yellowfin.user";
    private static final String YELLOWFIN_WEBSERVICES_PASSWORD = "com.elster.jupiter.yellowfin.password";

    private static final String DEFAULT_YELLOWFIN_URL = "http://localhost";
    private static final String DEFAULT_YELLOWFIN_HOST = "localhost";
    private static final int DEFAULT_YELLOWFIN_PORT = 80;
    private static final String DEFAULT_YELLOWFIN_ROOT = "";

    private static final String DEFAULT_YELLOWFIN_USER = "admin";
    private static final String DEFAULT_YELLOWFIN_PASSWORD = "admin";

    private String yellowfinUrl = DEFAULT_YELLOWFIN_URL;
    private String yellowfinExternalUrl = DEFAULT_YELLOWFIN_URL;
    private String yellowfinHost = DEFAULT_YELLOWFIN_HOST;
    private int yellowfinPort = DEFAULT_YELLOWFIN_PORT;
    private String yellowfinRoot = DEFAULT_YELLOWFIN_ROOT;

    private String yellowfinWebServiceUser = DEFAULT_YELLOWFIN_USER;
    private String yellowfinWebServicePassword = DEFAULT_YELLOWFIN_PASSWORD;

    private boolean useSecureConnection = false;

    private volatile UserService userService;
    private volatile UpgradeService upgradeService;

    @Activate
    public void activate(BundleContext context) {
        loadProperties(context);
        parseUrl();
        DataModel dataModel = upgradeService.newNonOrmDataModel();
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(UserService.class).toInstance(userService);
                bind(YellowfinService.class).toInstance(YellowfinServiceImpl.this);
            }
        });
        upgradeService.register(InstallIdentifier.identifier("Pulse", "YFA"), dataModel, Installer.class, Collections.emptyMap());
    }

    private void loadProperties(BundleContext context) {
        yellowfinUrl = context.getProperty(YELLOWFIN_URL);
        yellowfinExternalUrl = context.getProperty(YELLOWFIN_EXTERNAL_URL);

        yellowfinWebServiceUser = context.getProperty(YELLOWFIN_WEBSERVICES_USER);
        yellowfinWebServiceUser = (yellowfinWebServiceUser == null) ? DEFAULT_YELLOWFIN_USER : yellowfinWebServiceUser;

        yellowfinWebServicePassword = context.getProperty(YELLOWFIN_WEBSERVICES_PASSWORD);
        yellowfinWebServicePassword = (yellowfinWebServicePassword == null) ? DEFAULT_YELLOWFIN_PASSWORD : yellowfinWebServicePassword;
    }

    private void parseUrl() {
        if (yellowfinUrl != null) {
            Pattern pattern = Pattern.compile("(https?://)([^:^/]*):?([0-9]\\d*)?(.*)?");
            Matcher matcher = pattern.matcher(yellowfinUrl);
            if (matcher.matches()) {
                yellowfinHost = matcher.group(2);
                if (matcher.group(3) != null) {
                    yellowfinPort = Integer.parseInt(matcher.group(3));
                }
                yellowfinRoot = matcher.group(4);
                if (yellowfinUrl.startsWith("https")) {
                    useSecureConnection = true;
                }
            }
        }
        yellowfinUrl = (yellowfinUrl == null) ? DEFAULT_YELLOWFIN_URL : yellowfinUrl;
        yellowfinExternalUrl = (yellowfinExternalUrl == null) ? yellowfinUrl : yellowfinExternalUrl;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Override
    public String getYellowfinUrl() {
        return yellowfinExternalUrl;
    }

    @Override
    public boolean importContent(String filePath) {
        File file = new File(filePath);
        try (FileInputStream inputStream = new FileInputStream(file)) {
            byte[] data = new byte[(int) file.length()];
            inputStream.read(data);

            AdministrationServiceRequest rsr = new AdministrationServiceRequest();
            AdministrationServiceService ts = new AdministrationServiceServiceLocator(yellowfinHost, yellowfinPort, yellowfinRoot + "/services/AdministrationService", useSecureConnection);
            AdministrationServiceSoapBindingStub rssbs = null;
            try {
                rssbs = (AdministrationServiceSoapBindingStub) ts.getAdministrationService();
            } catch (ServiceException e) {
                e.printStackTrace();
            }

            rsr.setLoginId(yellowfinWebServiceUser);
            rsr.setPassword(yellowfinWebServicePassword);
            rsr.setOrgId(1);
            rsr.setFunction("IMPORTCONTENT");
            rsr.setParameters(new String[]{Base64.encodeBytes(data)});

            if (rssbs != null) {
                try {
                    AdministrationServiceResponse rs = rssbs.remoteAdministrationCall(rsr);
                    if (rs != null) {
                        if ("SUCCESS".equals(rs.getStatusCode())) {
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
        return false;
    }

    @Override
    public Optional<String> getUser(String username) {
        AdministrationServiceResponse rs;
        AdministrationServiceRequest rsr = new AdministrationServiceRequest();
        AdministrationServiceService ts = new AdministrationServiceServiceLocator(yellowfinHost, yellowfinPort, yellowfinRoot + "/services/AdministrationService", useSecureConnection);
        AdministrationServiceSoapBindingStub rssbs = null;
        try {
            rssbs = (AdministrationServiceSoapBindingStub) ts.getAdministrationService();
        } catch (ServiceException e) {
            e.printStackTrace();
        }

        AdministrationPerson person = new AdministrationPerson();

        person.setUserId(username);

        rsr.setLoginId(yellowfinWebServiceUser);
        rsr.setPassword(yellowfinWebServicePassword);
        rsr.setOrgId(new Integer(1));
        rsr.setFunction("GETUSER");
        rsr.setPerson(person);

        if (rssbs != null) {
            try {
                rs = rssbs.remoteAdministrationCall(rsr);
                if (rs != null) {
                    if ("SUCCESS".equals(rs.getStatusCode())) {
                        return Optional.of("SUCCESS");
                    }
                    if (rs.getErrorCode() == 9) { // license breach
                        return Optional.of("LICENSE_BREACH");
                    }
                    return Optional.of("NOT_FOUND");
                }

            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<String> createUser(String username) {

        AdministrationServiceRequest rsr = new AdministrationServiceRequest();
        AdministrationServiceService ts = new AdministrationServiceServiceLocator(yellowfinHost, yellowfinPort, yellowfinRoot + "/services/AdministrationService", useSecureConnection);
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
        person.setRoleCode("YFREPORTCONSUMER");
        person.setEmailAddress(username + "@elster.com");

        rsr.setLoginId(yellowfinWebServiceUser);
        rsr.setPassword(yellowfinWebServicePassword);
        rsr.setOrgId(new Integer(1));
        rsr.setFunction("ADDUSER");
        rsr.setPerson(person);

        if (rssbs != null) {
            try {
                AdministrationServiceResponse rs = rssbs.remoteAdministrationCall(rsr);
                if (rs != null) {
                    if ("SUCCESS".equals(rs.getStatusCode())) {
                        return Optional.of("SUCCESS");
                    }
                    if (rs.getErrorCode() == 9) { // license breach
                        return Optional.of("LICENSE_BREACH");
                    }
                }

            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<String> login(String username) {
        AdministrationServiceRequest rsr = new AdministrationServiceRequest();
        AdministrationServiceService ts = new AdministrationServiceServiceLocator(yellowfinHost, yellowfinPort, yellowfinRoot + "/services/AdministrationService", useSecureConnection);
        AdministrationServiceSoapBindingStub rssbs = null;
        try {
            rssbs = (AdministrationServiceSoapBindingStub) ts.getAdministrationService();
        } catch (ServiceException e) {
            e.printStackTrace();
        }

        rsr.setLoginId(yellowfinWebServiceUser);
        rsr.setPassword(yellowfinWebServicePassword);
        rsr.setOrgId(new Integer(1));
        rsr.setFunction("LOGINUSERNOPASSWORD");
        AdministrationPerson ap = new AdministrationPerson();
        ap.setUserId(username);
        rsr.setPerson(ap);

        if (rssbs != null) {
            try {
                AdministrationServiceResponse rs = rssbs.remoteAdministrationCall(rsr);
                if (rs != null) {
                    if ("SUCCESS".equals(rs.getStatusCode())) {
                        return Optional.of(rs.getLoginSessionId());
                    }
                    if (rs.getErrorCode() == 9) { // license breach
                        return Optional.of("LICENSE_BREACH");
                    }
                }

            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        return Optional.empty();
    }


    @Override
    public Optional<Boolean> logout(String username) {
        AdministrationServiceRequest rsr = new AdministrationServiceRequest();
        AdministrationServiceService ts = new AdministrationServiceServiceLocator(yellowfinHost, yellowfinPort, yellowfinRoot + "/services/AdministrationService", useSecureConnection);
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
        rsr.setPerson(ap);

        if (rssbs != null) {
            try {
                AdministrationServiceResponse rs = rssbs.remoteAdministrationCall(rsr);
                if (rs != null && "SUCCESS".equals(rs.getStatusCode())) {
                    return Optional.of(true);
                } else {
                    return Optional.of(false);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        return Optional.empty();
    }

    public String reloadLicense(String username) {
        AdministrationServiceRequest rsr = new AdministrationServiceRequest();
        AdministrationServiceService ts = new AdministrationServiceServiceLocator(yellowfinHost, yellowfinPort, yellowfinRoot + "/services/AdministrationService", useSecureConnection);
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
                AdministrationServiceResponse rs = rssbs.remoteAdministrationCall(rsr);
                if (rs != null) {
                    if ("SUCCESS".equals(rs.getStatusCode())) {
                        return rs.getSessionId();
                    }
                }

            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    @Override
    public Optional<List<YellowfinReportInfo>> getUserReports(String username, String category, String subCategory, String reportUUId) {

        List<YellowfinReportInfo> userReports = new ArrayList<>();
        try {
            AdministrationServiceRequest rsr = new AdministrationServiceRequest();
            AdministrationServiceService ts = new AdministrationServiceServiceLocator(yellowfinHost, yellowfinPort, yellowfinRoot + "/services/AdministrationService", useSecureConnection);
            AdministrationServiceSoapBindingStub rssbs = (AdministrationServiceSoapBindingStub) ts.getAdministrationService();

            rsr.setLoginId(yellowfinWebServiceUser);
            rsr.setPassword(yellowfinWebServicePassword);
            rsr.setOrgId(new Integer(1));
            rsr.setFunction("GETALLUSERREPORTS");

            AdministrationPerson ap = new AdministrationPerson();
            ap.setUserId(username);
            rsr.setPerson(ap);
            if (rssbs != null) {
                try {
                    AdministrationServiceResponse rs = rssbs.remoteAdministrationCall(rsr);
                    if (rs != null) {
                        if ("SUCCESS".equals(rs.getStatusCode())) {
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
                    return Optional.empty();
                }
            }
        } catch (ServiceException e) {
            e.printStackTrace();
            return Optional.empty();
        }


        return Optional.of(userReports);
    }

    @Override
    public Optional<List<YellowfinFilterInfo>> getReportFilters(int reportId) {
        List<YellowfinFilterInfo> userFilters = new ArrayList<>();
        try {
            ReportServiceService ts = new ReportServiceServiceLocator(yellowfinHost, yellowfinPort, yellowfinRoot + "/services/ReportService", useSecureConnection);
            ReportServiceSoapBindingStub rssbs;
            ReportServiceRequest rsr = new ReportServiceRequest();

            rssbs = (ReportServiceSoapBindingStub) ts.getReportService();
            rsr.setLoginId(yellowfinWebServiceUser);
            rsr.setPassword(yellowfinWebServicePassword);
            rsr.setOrgId(new Integer(1));
            rsr.setReportRequest("SCHEMA");
            rsr.setReportId(reportId);
            try {
                ReportServiceResponse reportServiceResponse = rssbs.remoteReportCall(rsr);
                if (reportServiceResponse != null) {
                    if ("SUCCESS".equals(reportServiceResponse.getStatusCode())) {
                        ReportSchema[] rs = reportServiceResponse.getColumns();
                        for (int i = 0; i < rs.length; i++) {
                            if (rs[i].getFilterId() != null) {
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
                            }
                        }
                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
                return Optional.empty();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
        return Optional.of(userFilters);

    }

    public ReportRow[] getFilterList(String filterId, int reportId) {
        ReportServiceService ts = new ReportServiceServiceLocator(yellowfinHost, yellowfinPort, yellowfinRoot + "/services/ReportService", useSecureConnection);
        ReportServiceSoapBindingStub rssbs;
        ReportServiceRequest rsr = new ReportServiceRequest();
        ReportRow[] reportRows = null;
        try {
            rssbs = (ReportServiceSoapBindingStub) ts.getReportService();
            rsr.setLoginId(yellowfinWebServiceUser);
            rsr.setPassword(yellowfinWebServicePassword);
            rsr.setOrgId(new Integer(1));
            rsr.setReportRequest("FILTEROPTIONS");
            rsr.setObjectName(filterId);
            rsr.setReportId(reportId);
            try {
                ReportServiceResponse reportServiceResponse = rssbs.remoteReportCall(rsr);
                if (reportServiceResponse != null) {
                    if ("SUCCESS".equals(reportServiceResponse.getStatusCode())) {
                        reportRows = reportServiceResponse.getResults();
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


    public Optional<List<YellowfinFilterListItemInfo>> getFilterListItems(String filterId, int reportId) {
        List<YellowfinFilterListItemInfo> listItems = new ArrayList<>();
        try {
            ReportServiceService ts = new ReportServiceServiceLocator(yellowfinHost, yellowfinPort, yellowfinRoot + "/services/ReportService", useSecureConnection);
            ReportServiceSoapBindingStub rssbs;
            ReportServiceRequest rsr = new ReportServiceRequest();


            rssbs = (ReportServiceSoapBindingStub) ts.getReportService();
            rsr.setLoginId(yellowfinWebServiceUser);
            rsr.setPassword(yellowfinWebServicePassword);
            rsr.setOrgId(new Integer(1));
            rsr.setReportRequest("FILTEROPTIONS");
            rsr.setObjectName(filterId);
            rsr.setReportId(reportId);
            try {
                ReportServiceResponse reportServiceResponse = rssbs.remoteReportCall(rsr);
                if (reportServiceResponse != null) {
                    if ("SUCCESS".equals(reportServiceResponse.getStatusCode())) {
                        ReportRow[] reportRows = reportServiceResponse.getResults();
                        for (int i = 0; i < reportRows.length; i++) {
                            String[] dataValues = reportRows[i].getDataValue();
                            YellowfinFilterListItemInfo listItem = new YellowfinFilterListItemInfoImpl();
                            if (dataValues[0].contains("__##SEARCH_RESULTS##__")) {
                                continue;
                            }
                            listItem.setValue1(dataValues[0]);
                            listItem.setValue2(dataValues[1]);
                            listItems.add(listItem);
                        }
                    }
                }

            } catch (RemoteException e) {
                e.printStackTrace();
                return Optional.empty();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
        return Optional.of(listItems);
    }

    @Override
    public String getComponentName() {
        return YellowfinService.COMPONENTNAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        List<TranslationKey> translationKeys = new ArrayList<>();
        translationKeys.add(new SimpleTranslationKey("error.facts.unavailable", "Connexo Facts is not available."));
        translationKeys.addAll(Arrays.asList(Privileges.values()));
        return translationKeys;
    }
}
