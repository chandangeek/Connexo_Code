package com.energyict.mdc.engine.monitor.app.impl;

import com.elster.jupiter.http.whiteboard.*;
import com.elster.jupiter.nls.*;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.*;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.monitor.app.MdcMonitorAppService;
import com.energyict.mdc.engine.status.StatusService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.*;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Component(
        name = "com.energyict.mdc.engine.monitor.app",
        service = {MdcMonitorAppService.class, TranslationKeyProvider.class, ApplicationPrivilegesProvider.class},
        immediate = true)
@SuppressWarnings("unused")
public class MdcMonitorAppServiceImpl implements MdcMonitorAppService , TranslationKeyProvider, ApplicationPrivilegesProvider{

    private final Logger logger = Logger.getLogger(MdcMonitorAppServiceImpl.class.getName());

    public static final String HTTP_RESOURCE_ALIAS = "/CSMonitor";
    public static final String HTTP_RESOURCE_LOCAL_NAME = "/js/mon";

    public static final String APPLICATION_ICON = "connexo";

    private volatile ServiceRegistration<HttpResource> registration;
    private volatile StatusService statusService;
    private volatile EngineConfigurationService engineConfigurationService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile UserService userService;

    public MdcMonitorAppServiceImpl() {
    }

    @Inject
    public MdcMonitorAppServiceImpl(StatusService statusService,
                                    EngineConfigurationService engineConfigurationService,
                                    ThreadPrincipalService threadPrincipalService,
                                    UserService userService, BundleContext context) {
        setStatusService(statusService);
        setEngineConfigurationService(engineConfigurationService);
        setThreadPrincipalService(threadPrincipalService);
        setUserService(userService);
        activate(context);
    }

    @Activate
    public final void activate(BundleContext context) {
        HttpResource resource = new HttpResource(HTTP_RESOURCE_ALIAS, HTTP_RESOURCE_LOCAL_NAME, new BundleResolver(context));
        // EXAMPLE: Below is how to enable local development mode.
        //HttpResource resource = new HttpResource(HTTP_RESOURCE_ALIAS, "C:\\git\\jupiter\\copl\\com.elster.jupiter.appserver.extjs\\src\\main\\web\\js\\appserver", new FileResolver());
        registration = context.registerService(HttpResource.class, resource, null);
    }

    @Deactivate
    public void stop(BundleContext context) throws Exception {
        registration.unregister();
    }

    @Reference
    public void setStatusService(StatusService statusService) {
        this.statusService = statusService;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    public void setEngineConfigurationService(EngineConfigurationService engineConfigurationService) {
        this.engineConfigurationService = engineConfigurationService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    private boolean isAllowed(User user) {
        List<? super Privilege> appPrivileges = getDBApplicationPrivileges();
        return user.getPrivileges(APPLICATION_KEY).stream().anyMatch(appPrivileges::contains);
    }

    private List<? super Privilege> getDBApplicationPrivileges() {
        return userService.getPrivileges(APPLICATION_KEY);
    }

    @Override
    public List<String> getApplicationPrivileges() {
        return MdcMonitorAppPrivileges.getApplicationPrivileges();
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