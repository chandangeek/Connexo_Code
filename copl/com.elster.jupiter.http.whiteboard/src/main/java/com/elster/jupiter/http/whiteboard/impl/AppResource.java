package com.elster.jupiter.http.whiteboard.impl;

import com.elster.jupiter.http.whiteboard.App;
import com.elster.jupiter.http.whiteboard.SecurityToken;
import com.elster.jupiter.license.License;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.json.JsonService;

import javax.inject.Inject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Path("/apps")
public class AppResource {

    @Inject
    private WhiteBoardImpl whiteBoard;
    @Inject
    private UserService userService;
    @Inject
    private JsonService jsonService;


    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public List<AppInfo> getApps(@Context SecurityContext securityContext) {
        // TODO: sessions will not be used, so we need to explicitly set the security context principal on authentication
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
    @Path("/login")
    public void login() {
        // Empty method, to be used for performing basic authentication over REST calls
    }

    @POST
    @Path("/logout")
    public void logout(@Context SecurityContext securityContext, @Context HttpServletRequest request, @Context HttpServletResponse response) {
        User user = (User) securityContext.getUserPrincipal();
        userService.removeLoggedUser(user);

        // Invalidate token & server side session
        Optional<Cookie> tokenCookie = Arrays.stream(request.getCookies()).filter(cookie -> cookie.getName().equals("X-CONNEXO-TOKEN")).findFirst();
        if(tokenCookie.isPresent()){
            SecurityToken.getInstance().removeCookie(request,response);
            SecurityToken.getInstance().invalidateSession(request);
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
