package com.elster.jupiter.system.app.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.data.lifecycle.LifeCycleService;
import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.http.whiteboard.App;
import com.elster.jupiter.http.whiteboard.BundleResolver;
import com.elster.jupiter.http.whiteboard.DefaultStartPage;
import com.elster.jupiter.http.whiteboard.HttpResource;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.SimpleTranslationKey;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.system.SubsystemService;
import com.elster.jupiter.system.app.SysAppService;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.ApplicationPrivilegesProvider;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static com.elster.jupiter.orm.Version.version;

@Component(
        name = "com.elster.jupiter.system.app",
        service = {SysAppService.class, TranslationKeyProvider.class, ApplicationPrivilegesProvider.class},
        property = "name=" + SysAppService.COMPONENTNAME,
        immediate = true
)
public class SysAppServiceImpl implements SysAppService, TranslationKeyProvider, ApplicationPrivilegesProvider {

    public static final String HTTP_RESOURCE_ALIAS = "/admin";
    public static final String HTTP_RESOURCE_LOCAL_NAME = "/js/system";

    public static final String APPLICATION_ICON = "connexo";

    private volatile ServiceRegistration<App> registration;
    private volatile UserService userService;
    private volatile AppService appService;
    private volatile LicenseService licenseService;
    private volatile TimeService timeService;
    private volatile BpmService bpmService;
    private volatile LifeCycleService lifeCycleService;
    private volatile FileImportService fileImportService;
    private volatile CustomPropertySetService customPropertySetService;
    private volatile MeteringService meteringService;
    private volatile SubsystemService subsystemService;
    private volatile ServiceCallService serviceCallService;
    private volatile CalendarService calendarService;
    private volatile UpgradeService upgradeService;

    public SysAppServiceImpl() {
    }

    @Inject
    public SysAppServiceImpl(UserService userService,
                             AppService appService,
                             LicenseService licenseService,
                             TimeService timeService,
                             BpmService bpmService,
                             LifeCycleService lifeCycleService,
                             FileImportService fileImportService,
                             CustomPropertySetService customPropertySetService,
                             MeteringService meteringService,
                             SubsystemService subsystemService,
                             ServiceCallService serviceCallService,
                             OrmService ormService,
                             UpgradeService upgradeService,
                             BundleContext context) {
        setUserService(userService);
        setAppService(appService);
        setLicenseService(licenseService);
        setTimeService(timeService);
        setBpmService(bpmService);
        setLifeCycleService(lifeCycleService);
        setFileImportService(fileImportService);
        setCustomPropertySetService(customPropertySetService);
        setMeteringService(meteringService);
        setSubsystemService(subsystemService);
        setServiceCallService(serviceCallService);
        setUpgradeService(upgradeService);
        activate(context);
    }

    @Activate
    public final void activate(BundleContext context) {
        try {
            HttpResource resource = new HttpResource(HTTP_RESOURCE_ALIAS, HTTP_RESOURCE_LOCAL_NAME, new BundleResolver(context), new DefaultStartPage(APPLICATION_NAME));
            App app = new App(APPLICATION_KEY, APPLICATION_NAME, APPLICATION_ICON, HTTP_RESOURCE_ALIAS, resource, user -> isAllowed(user));

            registration = context.registerService(App.class, app, null);

            DataModel dataModel = upgradeService.newNonOrmDataModel();
            dataModel.register(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(UserService.class).toInstance(userService);
                }
            });
            upgradeService.register(InstallIdentifier.identifier("Pulse", "SSA"), dataModel, Installer.class,
                    ImmutableMap.of(
                            version(10, 2), Installer.class,
                            version(10, 3), Installer.class
                    ));
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Deactivate
    public void stop(BundleContext context) throws Exception {
        registration.unregister();
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setAppService(AppService appService) {
        this.appService = appService;
    }

    @Reference
    public void setLicenseService(LicenseService licenseService) {
        this.licenseService = licenseService;
    }

    @Reference
    public void setTimeService(TimeService timeService) {
        this.timeService = timeService;
    }

    @Reference
    public void setBpmService(BpmService bpmService) {
        this.bpmService = bpmService;
    }

    @Reference
    public void setLifeCycleService(LifeCycleService lifeCycleService) {
        this.lifeCycleService = lifeCycleService;
    }

    @Reference
    public void setFileImportService(FileImportService fileImportService) {
        this.fileImportService = fileImportService;
    }

    @Reference
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setSubsystemService(SubsystemService subsystemService) {
        this.subsystemService = subsystemService;
    }

    @Reference
    public void setServiceCallService(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Reference
    public void setCalendarService(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    private boolean isAllowed(User user) {
        return !user.getPrivileges(APPLICATION_KEY).isEmpty();
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