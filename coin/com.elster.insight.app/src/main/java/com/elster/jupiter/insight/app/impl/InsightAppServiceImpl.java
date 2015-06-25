package com.elster.jupiter.insight.app.impl;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import com.elster.jupiter.http.whiteboard.App;
import com.elster.jupiter.http.whiteboard.BundleResolver;
import com.elster.jupiter.http.whiteboard.DefaultStartPage;
import com.elster.jupiter.http.whiteboard.HttpResource;
import com.elster.jupiter.insight.app.InsightAppService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.HasName;

@Component(
        name = "com.elster.jupiter.insight.app",
        service = {InsightAppService.class, InstallService.class},
        property = "name=" + InsightAppService.COMPONENTNAME,
        immediate = true
)
public class InsightAppServiceImpl implements InsightAppService, InstallService {

    public static final String HTTP_RESOURCE_ALIAS = "/insight";
    public static final String HTTP_RESOURCE_LOCAL_NAME = "/js/insight";

    public static final String APP_KEY = "INS";
    public static final String APP_NAME = "Insight";
    public static final String APP_ICON = "connexo";

    private volatile ServiceRegistration<App> registration;
    private volatile UserService userService;

    public InsightAppServiceImpl() {
    }

    @Inject
    public InsightAppServiceImpl(UserService userService, BundleContext context) {
        setUserService(userService);
        activate(context);
    }

    @Activate
    public final void activate(BundleContext context) {
        HttpResource resource = new HttpResource(HTTP_RESOURCE_ALIAS, HTTP_RESOURCE_LOCAL_NAME, new BundleResolver(context), new DefaultStartPage(APP_NAME));
//        HttpResource resource = new HttpResource(HTTP_RESOURCE_ALIAS, "C:\\Users\\lvz\\Documents\\Workspace\\Jupiter\\com.elster.jupiter.system.app\\src\\main\\web\\js\\system", new FileResolver(), new DefaultStartPage(APP_NAME));
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
        return Arrays.asList(UserService.COMPONENTNAME);
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    private void assignPrivilegesToDefaultRoles() {
        List<Privilege> availablePrivileges = getApplicationPrivileges();
        userService.grantGroupWithPrivilege(UserService.DEFAULT_ADMIN_ROLE, availablePrivileges.stream().map(HasName::getName).toArray(String[]::new));
        userService.grantGroupWithPrivilege(UserService.BATCH_EXECUTOR_ROLE, availablePrivileges.stream().map(HasName::getName).toArray(String[]::new));
    }

    private boolean isAllowed(User user) {
        return true;
        //List<Privilege> appPrivileges = getApplicationPrivileges();
        //return user.getPrivileges().stream().anyMatch(appPrivileges::contains);
    }

    private List<Privilege> getApplicationPrivileges() {
        return userService.getResources(APPLICATION_KEY).stream().flatMap(resource -> resource.getPrivileges().stream()).collect(Collectors.toList());
    }

}
