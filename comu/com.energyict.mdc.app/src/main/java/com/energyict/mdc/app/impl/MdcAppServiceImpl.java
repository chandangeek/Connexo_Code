package com.energyict.mdc.app.impl;

import com.elster.jupiter.http.whiteboard.App;
import com.elster.jupiter.http.whiteboard.BundleResolver;
import com.elster.jupiter.http.whiteboard.DefaultStartPage;
import com.elster.jupiter.http.whiteboard.HttpResource;
import com.elster.jupiter.license.License;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.security.Privileges;
import com.energyict.mdc.app.MdcAppService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Component(
        name = "com.energyict.mdc.app",
        service = {MdcAppService.class, InstallService.class},
        property = "name=" + MdcAppService.COMPONENTNAME,
        immediate = true
)
public class MdcAppServiceImpl implements MdcAppService, InstallService {

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

    @Override
    public void install() {
        createDefaultRoles();
        assignPrivilegesToDefaultRoles();
    }

    @Reference(target = "(com.elster.jupiter.license.application.key=" + APP_KEY + ")")
    public void setLicense(License license) {
        this.license = license;
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList(UserService.COMPONENTNAME, "ISU", "DTC", "DDC", "MDC", "SCH", "VAL");
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    private void createDefaultRoles() {
        try {
            userService.createGroup(Roles.METER_EXPERT.value(), Roles.METER_EXPERT.description());
            userService.createGroup(Roles.METER_OPERATOR.value(), Roles.METER_OPERATOR.description());
        } catch (Exception e) {
            this.logger.severe(e.getMessage());
        }
    }

    private void assignPrivilegesToDefaultRoles() {
        List<String> availablePrivileges = getAvailablePrivileges();
        userService.grantGroupWithPrivilege(Roles.METER_EXPERT.value(), availablePrivileges.toArray(new String[availablePrivileges.size()]));
        //TODO: workaround: attached Meter expert to user admin !!! to remove this line when the user can be created/added to system
        userService.getUser(1).ifPresent(u -> u.join(userService.getGroups().stream().filter(e -> e.getName().equals(Roles.METER_EXPERT.value())).findFirst().get()));
    }


    private boolean isAllowed(User user) {
        List<? super Privileges> appPrivileges = getApplicationPrivileges();
        return user.getPrivileges().stream().anyMatch(appPrivileges::contains);
    }

    private List<? super Privileges> getApplicationPrivileges() {
        return userService.getResources(APPLICATION_KEY).stream().flatMap(resource -> resource.getPrivileges().stream()).collect(Collectors.toList());
    }

    @Override
    public List<String> getAvailablePrivileges() {
        List<String> privileges = new ArrayList<String>();
        userService.getResources(MdcAppService.APPLICATION_KEY).forEach(e -> e.getPrivileges().forEach(p -> privileges.add(p.getName())));
        return privileges;
    }

}
