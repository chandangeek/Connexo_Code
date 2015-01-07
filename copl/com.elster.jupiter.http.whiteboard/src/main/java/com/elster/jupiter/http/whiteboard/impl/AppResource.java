package com.elster.jupiter.http.whiteboard.impl;

import com.elster.jupiter.http.whiteboard.App;
import com.elster.jupiter.license.License;
import com.elster.jupiter.users.User;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import java.util.List;
import java.util.stream.Collectors;

@Path("/apps")
public class AppResource {

    @Inject
    private WhiteBoard whiteBoard;


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<AppInfo> getApps(@Context SecurityContext securityContext) {
        User user = (User) securityContext.getUserPrincipal();
        return whiteBoard.getApps().stream().filter(app -> app.isAllowed(user)).map(this::appInfo).collect(Collectors.toList());
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
        appInfo.externalUrl = app.getExternalUrl();
        appInfo.isExternal = app.getExternalUrl() != null;
        return appInfo;
    }

    private String getUrl(App app) {
        return app.isInternalApp() ?
                whiteBoard.getAlias(app.getContext()) + app.getMainResource().getStartPage().getHtmlPath()
                : app.getExternalUrl();
    }

    private License getAppLicense(String app) {
        if (whiteBoard.getLicenseService().getLicensedApplicationKeys().contains(app)) {
            return whiteBoard.getLicenseService().getLicenseForApplication(app).get();
        }
        return null;
    }

}
