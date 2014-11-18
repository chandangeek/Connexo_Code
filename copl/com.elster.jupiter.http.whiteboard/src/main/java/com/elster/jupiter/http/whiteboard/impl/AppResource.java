package com.elster.jupiter.http.whiteboard.impl;

import com.elster.jupiter.http.whiteboard.App;
import com.elster.jupiter.license.License;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.User;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Path("/apps")
public class AppResource {

    @Inject
    private WhiteBoard whiteBoard;

    private static final String[] SYSTEM_ADMIN_PRIVILEGES = {"privilege.administrate.userAndRole","privilege.view.userAndRole","privilege.view.license","privilege.upload.license","privilege.administrate.period","privilege.view.period",
                                        "privilege.administrate.dataExportTask","privilege.view.dataExportTask","privilege.update.dataExportTask","privilege.update.schedule.dataExportTask","privilege.run.dataExportTask"};
    private static final String[] MULTI_SENSE_PRIVILEGES = {"privilege.view.issue","privilege.comment.issue","privilege.close.issue","privilege.assign.issue","privilege.action.issue",
                                        "privilege.view.creationRule","privilege.administrate.creationRule","privilege.view.assignmentRule","privilege.administrate.validationConfiguration",
                                        "privilege.view.validationConfiguration","privilege.administrate.schedule","privilege.view.schedule","privilege.administrate.communicationInfrastructure",
                                        "privilege.view.communicationInfrastructure","privilege.administrate.protocol","privilege.view.protocol","privilege.administrate.deviceConfiguration",
                                        "privilege.view.deviceConfiguration","privilege.administrate.device","privilege.view.device","privilege.view.validateManual","privilege.view.fineTuneValidationConfiguration","privilege.view.scheduleDevice",
                                        "privilege.administrate.deviceGroup","privilege.administrate.deviceOfEnumeratedGroup","privilege.view.deviceGroupDetail",
                                        "privilege.import.inventoryManagement","privilege.revoke.inventoryManagement","privilege.create.inventoryManagement","privilege.administrate.deviceSecurity",
                                        "privilege.view.deviceSecurity"};
    private static final String[] APP_YELLOWFIN_PRIVILEGES = {"privilege.view.*"};
    private static final String[] BPM_CONSOLE_PRIVILEGES = {"privilege.view.bpm"};
    private static final String APP_SYSTEM_ADMIN_NAME = "Connexo System Admin";
    private static final String APP_MULTI_SENSE_NAME = "Connexo Multi Sense";
    private static final String APP_BPM_CONSOLE_NAME = "BPM console";
    private static final String APP_YELLOWFIN_NAME = "Reports";


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<AppInfo> getApps(@Context SecurityContext securityContext) {
        User user = (User) securityContext.getUserPrincipal();
        Set<Privilege> privileges = user.getPrivileges();
        return getAllowedApps(privileges).stream().map(this::appInfo).collect(Collectors.toList());
    }

    @GET
    @Path("/status/{app}/")
    @Produces(MediaType.APPLICATION_JSON)
    public String getAppLicenseStatus(@PathParam("app") String app) {
        License license = getAppLicense(app);
        if (license != null) {
            if (license.getStatus().equals(License.Status.ACTIVE)) {
                return "ACTIVE";
            } else if (license.getStatus().equals(License.Status.EXPIRED)) {
                int gracePeriod = license.getGracePeriodInDays();
                if (gracePeriod > 0) {
                    return Integer.toString(gracePeriod);
                }
                return "EXPIRED";
            }
        }
        return "NO_LICENSE";
    }

    private AppInfo appInfo(App app) {
        AppInfo appInfo = new AppInfo();
        appInfo.name = app.getName();
        appInfo.icon = app.getIcon();
        appInfo.url = getUrl(app);
        return appInfo;
    }

    private String getUrl(App app) {
        return app.isInternalApp() ?
                whiteBoard.getAlias(app.getContext()) + app.getMainResource().getStartPage().getHtmlPath()
                : app.getExternalUrl();
    }

    private License getAppLicense(String app) {
        if (whiteBoard.getLicenseService().getLicensedApplicationKeys().contains(app)){
            return whiteBoard.getLicenseService().getLicenseForApplication(app).get();
        }
        return null;
    }

    private List<App> getAllowedApps(Set<Privilege> privileges) {
        List<App> applications = new ArrayList<>();

        Optional<App> appSys = whiteBoard.getApps().stream()
                .filter(e -> e.getName().equals(APP_SYSTEM_ADMIN_NAME))
                .findAny();
        if (appSys.isPresent() && isUserInApp(SYSTEM_ADMIN_PRIVILEGES, privileges)) {
            applications.add(appSys.get());
        }
        Optional<App> appMdc = whiteBoard.getApps().stream()
                .filter(e -> e.getName().equals(APP_MULTI_SENSE_NAME))
                .findAny();
        if (appMdc.isPresent() && isUserInApp(MULTI_SENSE_PRIVILEGES, privileges)) {
                    applications.add(appMdc.get());
        }
        Optional<App> appYfn = whiteBoard.getApps().stream()
                .filter(e -> e.getName().equals(APP_YELLOWFIN_NAME))
                .findAny();
        //TODO: uncomment when privileges are set into system
        if (appYfn.isPresent() /*&& isUserInApp(APP_YELLOWFIN_PRIVILEGES, privileges)*/) {
            applications.add(appYfn.get());
        }
        Optional<App> appBpm = whiteBoard.getApps().stream()
                .filter(e -> e.getName().equals(APP_BPM_CONSOLE_NAME))
                .findAny();
        if (appBpm.isPresent() && isUserInApp(BPM_CONSOLE_PRIVILEGES, privileges)) {
            applications.add(appBpm.get());
        }
        return applications;
    }

    private boolean isUserInApp (String[] appPrivileges, Set<Privilege> userPrivileges) {
        for (int i=0; i<appPrivileges.length; i++) {
            String privilege = appPrivileges[i];
            if (userPrivileges.stream().anyMatch(e -> e.getName().equals(privilege))){
                return true;
            }
        }
        return false;
    }
}
