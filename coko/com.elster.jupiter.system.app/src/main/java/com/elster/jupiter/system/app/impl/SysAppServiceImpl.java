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
import java.util.stream.Collectors;

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
        return Arrays.asList(UserService.COMPONENTNAME, "APS", "DES", "LIC", "TME", "BPM", "APR", "LFC", "YFN", "FIM");
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    private void assignPrivilegesToDefaultRoles() {
        List<Privilege> availablePrivileges = getDBApplicationPrivileges();
        userService.grantGroupWithPrivilege(UserService.DEFAULT_ADMIN_ROLE, availablePrivileges.stream().map(HasName::getName).toArray(String[]::new));
        userService.grantGroupWithPrivilege(UserService.BATCH_EXECUTOR_ROLE, availablePrivileges.stream().map(HasName::getName).toArray(String[]::new));
    }

    private boolean isAllowed(User user) {
        List<Privilege> appPrivileges = getRegisteredApplicationsPrivileges();
        return user.getPrivileges().stream().anyMatch(appPrivileges::contains);
    }

    private List<Privilege> getDBApplicationPrivileges() {
        return userService.getResources(APPLICATION_KEY).stream().flatMap(resource -> resource.getPrivileges().stream()).collect(Collectors.toList());
    }

    //@Override
    public List<Privilege> getRegisteredApplicationsPrivileges() {
        return userService.getResources()
                .stream()
                .flatMap(x->x.getPrivileges().stream())
                .filter(p-> SysAppPrivileges.getApplicationPrivileges().contains(p.getName()))
                .collect(Collectors.toList());


        //return userService.getResources(APPLICATION_KEY).stream().flatMap(resource -> resource.getPrivileges().stream()).collect(Collectors.toList());
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
