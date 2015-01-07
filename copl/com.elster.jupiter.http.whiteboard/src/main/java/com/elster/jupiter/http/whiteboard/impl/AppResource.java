package com.elster.jupiter.http.whiteboard.impl;

import com.elster.jupiter.http.whiteboard.App;
import com.elster.jupiter.license.License;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.json.JsonService;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import java.util.*;
import java.util.stream.Collectors;

@Path("/apps")
public class AppResource {

    static final String LOGOUT_QUEUE_DEST = "LogoutQueueDest";

    @Inject
    private WhiteBoard whiteBoard;
    @Inject
    private MessageService messageService;
    @Inject
    private JsonService jsonService;

    private static final String APP_SYSTEM_ADMIN_KEY = "SYS";
    private static final String APP_MULTI_SENSE_KEY = "MDC";
    private static final String APP_BPM_CONSOLE_KEY = "BPM";
    private static final String APP_YELLOWFIN_KEY = "YFN";

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

    @POST
    @Path("/logout")
    public void logout(@Context HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            User user =( User )session.getAttribute("user");
            if(user!=null){
                Optional<DestinationSpec> found = messageService.getDestinationSpec(LOGOUT_QUEUE_DEST);
                if (found.isPresent()) {
                    String json = jsonService.serialize(user.getName());
                    found.get().message(json).send();
                }
            }
            session.invalidate();
        }
    }

    private AppInfo appInfo(App app) {
        AppInfo appInfo = new AppInfo();
        appInfo.name = app.getName();
        appInfo.icon = app.getIcon();
        appInfo.url = getUrl(app);
        appInfo.externalUrl = app.getExternalUrl();
        appInfo.isExternal = app.getExternalUrl()!=null;
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
                    .filter(e -> e.getKey().equals(APP_SYSTEM_ADMIN_KEY))
                    .findAny();
            if (appSys.isPresent() && isUserInApp(whiteBoard.getSysAppService().getAvailablePrivileges(), privileges)) {
                applications.add(appSys.get());
        }
        Optional<App> appMdc = whiteBoard.getApps().stream()
                .filter(e -> e.getKey().equals(APP_MULTI_SENSE_KEY))
                .findAny();
        if (appMdc.isPresent() && isUserInApp(whiteBoard.getMdcAppService().getAvailablePrivileges(), privileges)) {
            applications.add(appMdc.get());
        }
        Optional<App> appYfn = whiteBoard.getApps().stream()
                .filter(e -> e.getKey().equals(APP_YELLOWFIN_KEY))
                .findAny();
        //TODO: uncomment when privileges are set into system
        if (appYfn.isPresent() /*&& isUserInApp(APP_YELLOWFIN_PRIVILEGES, privileges)*/) {
            applications.add(appYfn.get());
        }
        Optional<App> appBpm = whiteBoard.getApps().stream()
                .filter(e -> e.getKey().equals(APP_BPM_CONSOLE_KEY))
                .findAny();
        //TODO: change static BPM privilege with list of privileges defined by BPM application
        if (appBpm.isPresent() && isUserInApp(Arrays.asList("privilege.view.bpm"), privileges)) {
            applications.add(appBpm.get());
        }
        return applications;
    }

    private boolean isUserInApp (List<String> appPrivileges, Set<Privilege> userPrivileges) {
        for (String appPrivilege : appPrivileges) {
            if (userPrivileges.stream().anyMatch(e -> e.getName().equals(appPrivilege))) {
                return true;
            }
        }
        return false;
    }
}
