package com.elster.jupiter.system.app.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.appserver.extjs.AppServerUIInstaller;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.data.lifecycle.LifeCycleService;
import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.http.whiteboard.*;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.SimpleTranslationKey;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.system.SubsystemService;
import com.elster.jupiter.system.app.SysAppService;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.users.ApplicationPrivilegesProvider;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
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

@Component(
        name = "com.elster.jupiter.system.app",
        service = {SysAppService.class, InstallService.class, TranslationKeyProvider.class, ApplicationPrivilegesProvider.class},
        property = "name=" + SysAppService.COMPONENTNAME,
        immediate = true
)
public class SysAppServiceImpl implements SysAppService, InstallService, TranslationKeyProvider, ApplicationPrivilegesProvider {

    public static final String HTTP_RESOURCE_ALIAS = "/admin";
    public static final String HTTP_RESOURCE_LOCAL_NAME = "/js/system";

    public static final String APPLICATION_ICON = "connexo";

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
        HttpResource resource = new HttpResource(HTTP_RESOURCE_ALIAS, HTTP_RESOURCE_LOCAL_NAME, new BundleResolver(context), new DefaultStartPage(APPLICATION_NAME));
        App app = new App(APPLICATION_KEY, APPLICATION_NAME, APPLICATION_ICON, HTTP_RESOURCE_ALIAS, resource, user -> isAllowed(user));

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
        return Arrays.asList(UserService.COMPONENTNAME,
                AppService.COMPONENT_NAME,
                LicenseService.COMPONENTNAME,
                TimeService.COMPONENT_NAME,
                "BPM",
                AppServerUIInstaller.COMPONENT_NAME,
                LifeCycleService.COMPONENTNAME,
                "YFN",
                FileImportService.COMPONENT_NAME,
                CustomPropertySetService.COMPONENT_NAME,
                MeteringService.COMPONENTNAME,
                SubsystemService.COMPONENTNAME);
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
        return APPLICATION_KEY;
    }

    @Override
    public String getComponentName() {
        return APPLICATION_KEY;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        List<TranslationKey> translationKeys = new ArrayList<>();
        translationKeys.add(new SimpleTranslationKey(APPLICATION_KEY, APPLICATION_NAME));
        return translationKeys;
    }
}