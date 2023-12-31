/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.yellowfin.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.yellowfin.MessageSeeds;
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
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component(name = "com.elster.jupiter.yellowfin", service = {YellowfinService.class, TranslationKeyProvider.class}, immediate = true, property = "name=" + YellowfinService.COMPONENTNAME)
@SuppressWarnings("unused")
public class YellowfinServiceImpl implements YellowfinService, MessageSeedProvider, TranslationKeyProvider {
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
    private static final String EXCEPTION_PREFIX = "Exception occured: ";
    private static final String ERROR_CODE_PREFIX = "Error code: ";

    /** YellowFin WS functions */
    public static final String YF_IMPORTCONTENT = "IMPORTCONTENT";
    public static final String YF_GETUSER = "GETUSER";
    public static final String YF_ADDUSER = "ADDUSER";
    public static final String YF_LOGINUSERNOPASSWORD = "LOGINUSERNOPASSWORD";
    public static final String YF_LOGOUTUSER = "LOGOUTUSER";
    public static final String YF_RELOADLICENCE = "RELOADLICENCE";
    public static final String YF_GETALLUSERREPORTS = "GETALLUSERREPORTS";
    public static final String YF_SCHEMA = "SCHEMA";
    public static final String YF_FILTEROPTIONS = "FILTEROPTIONS";
    public static final String YFROLE_YFREPORTCONSUMER = "YFREPORTCONSUMER";

    public static final String SUCCESS = "SUCCESS";
    public static final String YELLOW_FIN_SERVICE = "[YellowFinService] ";

    public static final String DEFAULT_PASS = "Pass4Yellow.Fin";

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

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    @Activate
    public void activate(BundleContext context) {
        loadProperties(context);
        parseUrl();
        logActivationParameters();
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
            AdministrationServiceService ts = createAdminService();
            AdministrationServiceSoapBindingStub rssbs = null;
            try {
                rssbs = (AdministrationServiceSoapBindingStub) ts.getAdministrationService();
            } catch (ServiceException e) {
                e.printStackTrace();
            }

            rsr.setLoginId(yellowfinWebServiceUser);
            rsr.setPassword(yellowfinWebServicePassword);
            rsr.setOrgId(1);
            rsr.setFunction(YF_IMPORTCONTENT);
            rsr.setParameters(new String[]{Base64.encodeBytes(data)});

            logFunctionCall(ts, rsr);

            if (rssbs != null) {
                try {
                    AdministrationServiceResponse rs = rssbs.remoteAdministrationCall(rsr);
                    if (rs != null) {
                        logResponse(YF_IMPORTCONTENT, rs);
                        if (isSuccess(rs)){
                            return true;
                        }
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Optional<String> getUser(String username) {
        AdministrationServiceResponse rs;
        AdministrationServiceRequest rsr = new AdministrationServiceRequest();
        AdministrationServiceService ts = createAdminService();
        AdministrationServiceSoapBindingStub rssbs;
        try {
            rssbs = (AdministrationServiceSoapBindingStub) ts.getAdministrationService();
        } catch (ServiceException e) {
            e.printStackTrace();
            return Optional.of(EXCEPTION_PREFIX + e.getLocalizedMessage());
        }

        AdministrationPerson person = new AdministrationPerson();

        person.setUserId(username);

        rsr.setLoginId(yellowfinWebServiceUser);
        rsr.setPassword(yellowfinWebServicePassword);
        rsr.setOrgId(1);
        rsr.setFunction(YF_GETUSER);
        rsr.setPerson(person);

        logFunctionCall(ts, rsr);

        if (rssbs != null) {
            try {
                rs = rssbs.remoteAdministrationCall(rsr);
                if (rs != null) {
                    logResponse(YF_GETUSER, rs);
                    if (isSuccess(rs)) {
                        return Optional.of(SUCCESS);
                    }
                    if (isLicenseBreach(rs)) {
                        return Optional.of(ERROR_CODE_PREFIX + "LICENSE_BREACH");
                    }
                    return Optional.of("NOT_FOUND");
                }
            } catch (RemoteException e) {
                e.printStackTrace();
                return Optional.of(EXCEPTION_PREFIX + e.getLocalizedMessage());
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<String> createUser(String username, String email) {
        AdministrationServiceRequest rsr = new AdministrationServiceRequest();
        AdministrationServiceService ts = createAdminService();
        AdministrationServiceSoapBindingStub rssbs;
        try {
            rssbs = (AdministrationServiceSoapBindingStub) ts.getAdministrationService();
        } catch (ServiceException e) {
            e.printStackTrace();
            return Optional.of(EXCEPTION_PREFIX + e.getLocalizedMessage());
        }

        AdministrationPerson person = new AdministrationPerson();

        person.setUserId(username);
        person.setPassword(DEFAULT_PASS); //TODO is this ever used?!
        person.setFirstName("Connexo");
        person.setLastName(username);
        person.setRoleCode(YFROLE_YFREPORTCONSUMER);
        if (email != null) {
            person.setEmailAddress(email);
        } else {
            person.setEmailAddress("Not Available");
        }

        rsr.setLoginId(yellowfinWebServiceUser);
        rsr.setPassword(yellowfinWebServicePassword);
        rsr.setOrgId(1);
        rsr.setFunction(YF_ADDUSER);
        rsr.setPerson(person);

        logFunctionCall(ts, rsr);

        if (rssbs != null) {
            try {
                AdministrationServiceResponse rs = rssbs.remoteAdministrationCall(rsr);
                if (rs != null) {
                    logResponse(YF_ADDUSER, rs);
                    if (isSuccess(rs)) {
                        return Optional.of(SUCCESS);
                    }
                    if (isLicenseBreach(rs)){
                        return Optional.of(ERROR_CODE_PREFIX + "LICENSE_BREACH");
                    }
                    return Optional.of(ERROR_CODE_PREFIX + rs.getErrorCode());
                }

            } catch (RemoteException e) {
                e.printStackTrace();
                return Optional.of(EXCEPTION_PREFIX + e.getLocalizedMessage());
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> createUser(String username) {
        return createUser(username, null);
    }

    @Override
    public Optional<String> login(String username) {
        return login(username, null);
    }

    @Override
    public Optional<String> login(String username, String email) {
        AdministrationServiceRequest rsr = new AdministrationServiceRequest();
        AdministrationServiceService ts = createAdminService();
        AdministrationServiceSoapBindingStub rssbs = null;
        try {
            rssbs = (AdministrationServiceSoapBindingStub) ts.getAdministrationService();
        } catch (ServiceException e) {
            e.printStackTrace();
        }

        rsr.setLoginId(yellowfinWebServiceUser);
        rsr.setPassword(yellowfinWebServicePassword);
        rsr.setOrgId(1);
        rsr.setFunction(YF_LOGINUSERNOPASSWORD);
        AdministrationPerson ap = new AdministrationPerson();
        ap.setUserId(username);
        if (null != email) {
            ap.setEmailAddress(email);
        }
        rsr.setPerson(ap);

        logFunctionCall(ts, rsr);

        if (rssbs != null) {
            try {
                AdministrationServiceResponse rs = rssbs.remoteAdministrationCall(rsr);
                if (rs != null) {
                    logResponse(YF_LOGINUSERNOPASSWORD, rs);
                    if (isSuccess(rs)){
                        return Optional.of(rs.getLoginSessionId());
                    }
                    if (isLicenseBreach(rs)){
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
        AdministrationServiceService ts = createAdminService();
        AdministrationServiceSoapBindingStub rssbs = null;
        try {
            rssbs = (AdministrationServiceSoapBindingStub) ts.getAdministrationService();
        } catch (ServiceException e) {
            e.printStackTrace();
        }

        rsr.setLoginId(yellowfinWebServiceUser);
        rsr.setPassword(yellowfinWebServicePassword);
        rsr.setOrgId(1);
        rsr.setFunction(YF_LOGOUTUSER);
        AdministrationPerson ap = new AdministrationPerson();
        ap.setUserId(username);
        rsr.setPerson(ap);

        logFunctionCall(ts, rsr);

        if (rssbs != null) {
            try {
                AdministrationServiceResponse rs = rssbs.remoteAdministrationCall(rsr);
                if (rs!=null){
                    logResponse(YF_LOGOUTUSER, rs);
                }
                if (rs != null && isSuccess(rs)) {
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
        AdministrationServiceService ts = createAdminService();
        AdministrationServiceSoapBindingStub rssbs = null;
        try {
            rssbs = (AdministrationServiceSoapBindingStub) ts.getAdministrationService();
        } catch (ServiceException e) {
            e.printStackTrace();
        }

        rsr.setLoginId(yellowfinWebServiceUser);
        rsr.setPassword(yellowfinWebServicePassword);

        rsr.setOrgId(1);
        rsr.setFunction(YF_RELOADLICENCE);

        logFunctionCall(ts, rsr);

        if (rssbs != null) {
            try {
                AdministrationServiceResponse rs = rssbs.remoteAdministrationCall(rsr);
                if (rs != null) {
                    logResponse(YF_RELOADLICENCE, rs);
                    if (isSuccess(rs)) {
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
            AdministrationServiceService ts = createAdminService();
            AdministrationServiceSoapBindingStub rssbs = (AdministrationServiceSoapBindingStub) ts.getAdministrationService();

            rsr.setLoginId(yellowfinWebServiceUser);
            rsr.setPassword(yellowfinWebServicePassword);
            rsr.setOrgId(1);
            rsr.setFunction(YF_GETALLUSERREPORTS);

            AdministrationPerson ap = new AdministrationPerson();
            ap.setUserId(username);
            rsr.setPerson(ap);

            logFunctionCall(ts, rsr);

            if (rssbs != null) {
                try {
                    AdministrationServiceResponse rs = rssbs.remoteAdministrationCall(rsr);
                    if (rs != null) {
                        logResponse(YF_GETALLUSERREPORTS, rs);
                        if (isSuccess(rs)) {
                            AdministrationReport[] reports = rs.getReports();
                            for (AdministrationReport report : reports) {
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
            ReportServiceService ts = createReportService();
            ReportServiceSoapBindingStub rssbs;
            ReportServiceRequest rsr = new ReportServiceRequest();

            rssbs = (ReportServiceSoapBindingStub) ts.getReportService();
            rsr.setLoginId(yellowfinWebServiceUser);
            rsr.setPassword(yellowfinWebServicePassword);
            rsr.setOrgId(1);
            rsr.setReportRequest(YF_SCHEMA);
            rsr.setReportId(reportId);

            logFunctionCall(ts,  rsr);

            try {
                ReportServiceResponse reportServiceResponse = rssbs.remoteReportCall(rsr);
                if (reportServiceResponse != null) {
                    logResponse(YF_SCHEMA, reportId, reportServiceResponse);
                    if (isSuccess(reportServiceResponse)) {
                        ReportSchema[] rs = reportServiceResponse.getColumns();
                        for (ReportSchema r : rs) {
                            if (r.getFilterId() != null) {
                                YellowfinFilterInfoImpl userFilter = new YellowfinFilterInfoImpl();
                                userFilter.setId(r.getFilterId());
                                userFilter.setFilterDataValue1(r.getDefaultValue1());
                                userFilter.setFilterDataValue2(r.getDefaultValue2());
                                userFilter.setFilterDisplayType(r.getFilterDisplayType());
                                userFilter.setFilterName(r.getColumnName());
                                userFilter.setFilterType(r.getFilterType());
                                userFilter.setFilterOmittable(r.getFilterOmittable());
                                userFilter.setFilterDisplayName(r.getDisplayName());
                                userFilter.setFilterPrompt(r.getPrompt());
                                userFilter.setFilterAllowPrompt(r.getAllowPrompt());
                                userFilter.setFilterMaxValue(r.getMaximumValue());
                                userFilter.setFilterMinValue(r.getMinimumValue());
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
        ReportServiceService ts = createReportService();
        ReportServiceSoapBindingStub rssbs;
        ReportServiceRequest rsr = new ReportServiceRequest();
        ReportRow[] reportRows = null;
        try {
            rssbs = (ReportServiceSoapBindingStub) ts.getReportService();
            rsr.setLoginId(yellowfinWebServiceUser);
            rsr.setPassword(yellowfinWebServicePassword);
            rsr.setOrgId(1);
            rsr.setReportRequest(YF_FILTEROPTIONS);
            rsr.setObjectName(filterId);
            rsr.setReportId(reportId);

            logFunctionCall(ts, rsr);

            try {
                ReportServiceResponse reportServiceResponse = rssbs.remoteReportCall(rsr);
                if (reportServiceResponse != null) {
                    logResponse(YF_FILTEROPTIONS, reportId, reportServiceResponse);
                    if (isSuccess(reportServiceResponse)) {
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
            ReportServiceService ts = createReportService();
            ReportServiceSoapBindingStub rssbs;
            ReportServiceRequest rsr = new ReportServiceRequest();

            rssbs = (ReportServiceSoapBindingStub) ts.getReportService();
            rsr.setLoginId(yellowfinWebServiceUser);
            rsr.setPassword(yellowfinWebServicePassword);
            rsr.setOrgId(1);
            rsr.setReportRequest(YF_FILTEROPTIONS);
            rsr.setObjectName(filterId);
            rsr.setReportId(reportId);

            logFunctionCall(ts,rsr);

            try {
                ReportServiceResponse reportServiceResponse = rssbs.remoteReportCall(rsr);
                if (reportServiceResponse != null) {
                    logResponse(YF_FILTEROPTIONS, reportId, reportServiceResponse);
                    if (isSuccess(reportServiceResponse)) {
                        ReportRow[] reportRows = reportServiceResponse.getResults();
                        for (ReportRow reportRow : reportRows) {
                            String[] dataValues = reportRow.getDataValue();
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
        return Stream.of(
                Arrays.stream(Privileges.values()))
                .flatMap(Function.identity())
                .collect(Collectors.toList());
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }


    /**
     * Checks if web-service request was successful
     */
    private boolean isSuccess(AdministrationServiceResponse response) {
        return (SUCCESS.equals(response.getStatusCode()));
    }


    /**
     * Checks if web-service request was successful
     */
    private boolean isSuccess(ReportServiceResponse response) {
        return (SUCCESS.equals(response.getStatusCode()));
    }


    /**
     * Checks if web-service responded with a license-breach error-code
     */
    private boolean isLicenseBreach(AdministrationServiceResponse rs) {
        return (rs.getErrorCode() == YellowfinWebServicesErrorCodes.LICENCE_BREACH.getErrorCode());
    }


    /**
     * @return an {@link AdministrationServiceService} object using the provided connection parameters
     */
    private AdministrationServiceService createAdminService() {
        String serviceUrl = buildServiceUrl("/services/AdministrationService");
        return new AdministrationServiceServiceLocator(yellowfinHost, yellowfinPort, serviceUrl , useSecureConnection);
    }


    /**
     * @return an {@link ReportServiceService} object using the provided connection parameters
     */
    private ReportServiceService createReportService() {
        String serviceUrl = buildServiceUrl("/services/ReportService");
        return new ReportServiceServiceLocator(yellowfinHost, yellowfinPort, serviceUrl , useSecureConnection);
    }


    /**
     * Builds a complete url from the YellowFin root URL with sanitization of double slashes
     *
     * @param path path to be appended to the root
     * @return sanitized url or fallback to simple appender
     */
    private String buildServiceUrl(String path) {
        String rootUrl = yellowfinRoot;

        // remove trailing slash
        if (rootUrl.endsWith("/") ){
            rootUrl = rootUrl.substring(0, rootUrl.length()-1 );
        }

        if (!path.startsWith("/")){
            path = "/"+path;
        }

        return rootUrl+path;
    }


    /**
     * @return our eyes in the dark, our salvation, our response to all unknowns ... the mighty logger
     */
    private Logger getLogger() {
        return logger;
    }


    /**
     * Logs the parameters used to initialize the YellowFin service.
     * Most of them are important to troubleshoot connectivity issues or badly-configured parameters.
     */
    private void logActivationParameters() {
        StringBuilder builder = new StringBuilder();

        builder.append("YellowFin service activated with the following parameters: ");
        builder.append("yellowfinUrl=").append(yellowfinUrl).append(", ");
        builder.append("yellowfinExternalUrl=").append(yellowfinExternalUrl).append(", ");
        builder.append("yellowfinWebServiceUser=").append(yellowfinWebServiceUser).append(", ");
        builder.append("yellowfinHost=").append(yellowfinHost).append(", ");
        builder.append("yellowfinPort=").append(yellowfinPort).append(", ");
        builder.append("useSecureConnection=").append(useSecureConnection);

        getLogger().info(builder.toString());
    }


    /**
     * Logs the start of a YellowFin web-service call to AdministrationService function
     *
     * @param adminService administration service parameters
     * @param request request parameters
     */
    private void logFunctionCall(AdministrationServiceService adminService, AdministrationServiceRequest request) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Calling ").append(request.getFunction());

        if (request.getPerson()!=null){
            stringBuilder.append(", person=");
            stringBuilder.append(request.getPerson().getUserId());
            if (request.getPerson().getRoleCode()!=null) {
                stringBuilder.append("(role: ").append(request.getPerson().getRoleCode()).append(")");
            }
        }

        stringBuilder.append(", using loginId=").append(request.getLoginId());

        log(stringBuilder.toString());
    }


    /**
     * Logs the start of a YellowFin web-service call to ReportService function
     *
     * @param reportService report service parameters
     * @param request request parameters
     */
    private void logFunctionCall(ReportServiceService reportService, ReportServiceRequest request) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Calling ").append(request.getReportRequest());

        if (request.getReportId()!=null){
            stringBuilder.append(", reportId=");
            stringBuilder.append(request.getReportId());
        }

        if (request.getObjectName()!=null){
            stringBuilder.append(", object=");
            stringBuilder.append(request.getObjectName());
        }

        stringBuilder.append(", using loginId=").append(request.getLoginId());

        log(stringBuilder.toString());
    }


    /**
     * Logs details about a YellowFin web-service response after a certain function was called.
     *
     * @param function the function called originally (to know exactly where this is coming from)
     * @param rs the object containing the response details
     */
    private void logResponse(String function, AdministrationServiceResponse rs) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Response of called function [");
        stringBuilder.append(function).append("]: ");
        stringBuilder.append(" responseCode=");
        stringBuilder.append(rs.getStatusCode());

        appendErrorDescription(stringBuilder, rs.getErrorCode());

        if (isSuccess(rs)) {
            log(stringBuilder.toString());
        } else {
            logWarn(stringBuilder.toString());
        }
    }


    /**
     * Logs details about a YellowFin web-service report call.
     * @param function the function called
     * @param reportId report identifier in YellowFin
     * @param rs the object containing the response details
     */
    private void logResponse(String function, int reportId, ReportServiceResponse rs) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Response of called function [");
        stringBuilder.append(function);
        stringBuilder.append(", reportId=").append(reportId).append("]: ");
        stringBuilder.append(" responseCode=");
        stringBuilder.append(rs.getStatusCode());

        appendErrorDescription(stringBuilder, rs.getErrorCode());

        if (isSuccess(rs)) {
            log(stringBuilder.toString());
        } else {
            logWarn(stringBuilder.toString());
        }
    }


    /**
     * Extracts the specific web-service name and description and appends it to the string builder
     *
     * @param stringBuilder string builder to append to
     * @param errorCode the web-service error-code
     */
    private void appendErrorDescription(StringBuilder stringBuilder, Integer errorCode) {
        stringBuilder.append(", error Code=");
        stringBuilder.append(errorCode);

        Optional<YellowfinWebServicesErrorCodes> error = YellowfinWebServicesErrorCodes.getError(errorCode);
        if (error.isPresent()){
            stringBuilder.append(": ");
            stringBuilder.append(error.get().getName());
            stringBuilder.append(" - ");
            stringBuilder.append(error.get().getDescription());
        }
    }


    /**
     * Generic log with a prefix for easy filtering
     *
     * @param message message to be logged
     */
    private void log(String message) {
        getLogger().info(YELLOW_FIN_SERVICE +message);
    }


    /**
     * You would not gonna believe it! Something wrong happened while calling YellowFin!
     * Let's check the log to see what's wrong, so we can fix it!
     *
     * @param warningMessage a great message explaining the poor application engineer what's wrong with Facts
     */
    private void logWarn(String warningMessage) {
        getLogger().warning(YELLOW_FIN_SERVICE +warningMessage);
    }
}
