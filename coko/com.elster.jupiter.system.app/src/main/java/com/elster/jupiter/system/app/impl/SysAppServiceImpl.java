package com.elster.jupiter.system.app.impl;

import com.elster.jupiter.http.whiteboard.App;
import com.elster.jupiter.http.whiteboard.BundleResolver;
import com.elster.jupiter.http.whiteboard.DefaultStartPage;
import com.elster.jupiter.http.whiteboard.HttpResource;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.system.app.SysAppService;
import com.elster.jupiter.users.ApplicationPrivilegesProvider;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.HasName;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

@Component(
        name = "com.elster.jupiter.system.app",
        service = {SysAppService.class, InstallService.class, ApplicationPrivilegesProvider.class},
        property = "name=" + SysAppService.COMPONENTNAME,
        immediate = true
)
public class SysAppServiceImpl implements SysAppService, InstallService, ApplicationPrivilegesProvider {

    public static final String HTTP_RESOURCE_ALIAS = "/admin";
    public static final String HTTP_RESOURCE_LOCAL_NAME = "/js/system";

    public static final String APP_KEY = "SYS";
    public static final String APP_NAME = "Admin";
    public static final String APP_ICON = "connexo";

    private volatile ServiceRegistration<App> registration;
    private volatile UserService userService;

    public SysAppServiceImpl() {
    }

    @Inject
    public SysAppServiceImpl(UserService userService, BundleContext context) {
        setUserService(userService);
        activate(context);
    }

    @Activate
    public final void activate(BundleContext context) {
        HttpResource resource = new HttpResource(HTTP_RESOURCE_ALIAS, HTTP_RESOURCE_LOCAL_NAME, new BundleResolver(context), new DefaultStartPage(APP_NAME));
        App app = new App(APP_KEY, APP_NAME, APP_ICON, HTTP_RESOURCE_ALIAS, resource, user -> isAllowed(user));

        registration = context.registerService(App.class, app, null);
    }

    @Deactivate
    public void stop(BundleContext context) throws Exception {
        registration.unregister();
    }

    @Override
    public void install() {
        assignPrivilegesToDefaultRoles();
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList(UserService.COMPONENTNAME, "APS", "LIC", "TME", "BPM", "APR", "LFC", "YFN", "FIM");
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    private void assignPrivilegesToDefaultRoles() {
        String[] adminPrivileges = getAdminPrivileges();
        userService.grantGroupWithPrivilege(UserService.DEFAULT_ADMIN_ROLE, APPLICATION_KEY, adminPrivileges);
        userService.grantGroupWithPrivilege(UserService.BATCH_EXECUTOR_ROLE, APPLICATION_KEY, adminPrivileges);
    }

    private boolean isAllowed(User user) {
        return !user.getPrivileges(APPLICATION_KEY).isEmpty();
    }

    private String[] getAdminPrivileges() {
        return SysAppPrivileges.getApplicationPrivileges().stream().toArray(String[]::new);
    }


    @Override
    public List<String> getApplicationPrivileges() {
        return SysAppPrivileges.getApplicationPrivileges();
    }

    @Override
    public String getApplicationName() {
        return APP_KEY;
    }

}
