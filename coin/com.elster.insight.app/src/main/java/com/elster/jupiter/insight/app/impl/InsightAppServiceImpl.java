package com.elster.jupiter.insight.app.impl;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
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
import com.elster.jupiter.http.whiteboard.FileResolver;
import com.elster.jupiter.http.whiteboard.HttpResource;
import com.elster.jupiter.insight.app.InsightAppService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.SimpleTranslationKey;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.HasName;

@Component(
        name = "com.elster.jupiter.insight.app",
        service = {InsightAppService.class, InstallService.class, TranslationKeyProvider.class},
        property = "name=" + InsightAppService.COMPONENTNAME,
        immediate = true)
public class InsightAppServiceImpl implements InsightAppService, InstallService, TranslationKeyProvider {
    private static final Logger LOGGER = Logger.getLogger(InsightAppServiceImpl.class.getName());
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
//        HttpResource resource = new HttpResource(HTTP_RESOURCE_ALIAS, "/home/kurtk/Projects/git/insight/com.elster.jupiter.insight.app/src/main/web/js/insight", new FileResolver(),
//                new DefaultStartPage(APP_NAME));
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
        return Arrays.asList(UserService.COMPONENTNAME, MeteringService.COMPONENTNAME);
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    private void assignPrivilegesToDefaultRoles() {
        List<Privilege> availablePrivileges = getApplicationPrivileges();
        userService.grantGroupWithPrivilege(UserService.DEFAULT_ADMIN_ROLE, MeteringService.COMPONENTNAME, availablePrivileges.stream().map(HasName::getName).toArray(String[]::new));
        userService.grantGroupWithPrivilege(UserService.BATCH_EXECUTOR_ROLE, MeteringService.COMPONENTNAME, availablePrivileges.stream().map(HasName::getName).toArray(String[]::new));
    }

    private boolean isAllowed(User user) {
        return true;
        //List<Privilege> appPrivileges = getApplicationPrivileges();
        //return user.getPrivileges().stream().anyMatch(appPrivileges::contains);
    }

    private List<Privilege> getApplicationPrivileges() {
        return userService.getResources(MeteringService.COMPONENTNAME).stream().flatMap(resource -> resource.getPrivileges().stream()).collect(Collectors.toList());
    }

    @Override
    public List<TranslationKey> getKeys() {
        try {
            return SimpleTranslationKey.loadFromInputStream(this.getClass().getClassLoader().getResourceAsStream("i18n.properties"));
        } catch (IOException e) {
            LOGGER.severe("Failed to load translations for the '" + COMPONENTNAME + "' component bundle.");
        }
        return null;
    }

    @Override
    public String getComponentName() {
        //TODO: Change to COMPONENT_NAME after pulling the extjs stuff into a new bundle, etc.
        return "INS";
    }

    @Override
    public Layer getLayer() {
        return Layer.REST;
    }
}
