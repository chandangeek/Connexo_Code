package com.elster.jupiter.http.whiteboard.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.http.whiteboard.App;
import com.elster.jupiter.license.License;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.json.JsonService;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import java.util.List;
import java.util.stream.Collectors;

@Path("/apps")
public class AppResource {

    @Inject
    private WhiteBoard whiteBoard;
    @Inject
    private EventService eventService;
    @Inject
    private JsonService jsonService;


    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public List<AppInfo> getApps(@Context SecurityContext securityContext) {
        User user = (User) securityContext.getUserPrincipal();
        return whiteBoard.getApps().stream().filter(app -> app.isAllowed(user)).map(this::appInfo).collect(Collectors.toList());
    }

    @GET
    @Path("/status/{app}/")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public LicenseInfo getAppLicenseStatus(@PathParam("app") String app) {
        License license = getAppLicense(app);
        if (license != null) {
            if (license.getStatus().equals(License.Status.ACTIVE)) {
                return new LicenseInfo(License.Status.ACTIVE.name());
            } else if (license.getStatus().equals(License.Status.EXPIRED)) {
                int gracePeriod = license.getGracePeriodInDays();
                if (gracePeriod > 0) {
                    return new LicenseInfo(Integer.toString(gracePeriod));
                }
                return new LicenseInfo(License.Status.EXPIRED.name());
            }
        }
        return  new LicenseInfo("NO_LICENSE");
    }

    @POST
    @Path("/logout")
    public void logout(@Context HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            User user =( User )session.getAttribute("user");
            if(user!=null){
                eventService.postEvent(EventType.LOGOUT.topic(), user.getName());
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
