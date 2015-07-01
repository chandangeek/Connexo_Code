package com.energyict.mdc.app.impl;

import com.elster.jupiter.http.whiteboard.App;
import com.elster.jupiter.http.whiteboard.BundleResolver;
import com.elster.jupiter.http.whiteboard.DefaultStartPage;
import com.elster.jupiter.http.whiteboard.HttpResource;
import com.elster.jupiter.license.License;
import com.elster.jupiter.users.ApplicationPrivilegesProvider;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.app.MdcAppService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Component(
        name = "com.energyict.mdc.app",
        service = {MdcAppService.class, ApplicationPrivilegesProvider.class},
        immediate = true)
@SuppressWarnings("unused")
public class MdcAppServiceImpl implements MdcAppService , ApplicationPrivilegesProvider{

    private final Logger logger = Logger.getLogger(MdcAppServiceImpl.class.getName());

    public static final String HTTP_RESOURCE_ALIAS = "/multisense";
    public static final String HTTP_RESOURCE_LOCAL_NAME = "/js/mdc";

    public static final String APP_KEY = "MDC";
    public static final String APP_NAME = "MultiSense";
    public static final String APP_ICON = "connexo";

    private volatile ServiceRegistration<App> registration;
    private volatile License license;
    private volatile UserService userService;

    public MdcAppServiceImpl() {
    }

    @Inject
    public MdcAppServiceImpl(UserService userService, BundleContext context) {
        setUserService(userService);
        activate(context);
    }

    @Activate
    public final void activate(BundleContext context) {
        HttpResource resource = new HttpResource(HTTP_RESOURCE_ALIAS, HTTP_RESOURCE_LOCAL_NAME, new BundleResolver(context), new DefaultStartPage(APP_NAME));
        App app = new App(APP_KEY, APP_NAME, APP_ICON, HTTP_RESOURCE_ALIAS, resource, this::isAllowed);

        registration = context.registerService(App.class, app, null);
    }

    @Deactivate
    public void stop(BundleContext context) throws Exception {
        registration.unregister();
    }

    @Reference(target = "(com.elster.jupiter.license.application.key=" + APP_KEY + ")")
    public void setLicense(License license) {
        this.license = license;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    private boolean isAllowed(User user) {
        List<? super Privilege> appPrivileges = getDBApplicationPrivileges();
        return user.getPrivileges(APP_KEY).stream().anyMatch(appPrivileges::contains);
    }

    private List<? super Privilege> getDBApplicationPrivileges() {
        return userService.getPrivileges(APPLICATION_KEY);
    }

    @Override
    public List<String> getApplicationPrivileges() {
        return MdcAppPrivileges.getApplicationPrivileges();
    }

    @Override
    public String getApplicationName() {
        return APP_KEY;
    }

}