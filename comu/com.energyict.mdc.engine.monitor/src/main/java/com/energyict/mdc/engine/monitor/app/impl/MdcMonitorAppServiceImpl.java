package com.energyict.mdc.engine.monitor.app.impl;

import com.elster.jupiter.http.whiteboard.*;
import com.elster.jupiter.nls.*;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.*;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.monitor.app.MdcMonitorAppService;
import com.energyict.mdc.engine.monitor.app.security.MdcMonitorAppPrivileges;
import com.energyict.mdc.engine.monitor.app.security.PrivilegeTranslationKeyPair;
import com.energyict.mdc.engine.status.StatusService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.*;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component(
        name = "com.energyict.mdc.engine.monitor.app",
        service = {MdcMonitorAppService.class, TranslationKeyProvider.class, PrivilegesProvider.class},
        property = "name=" + MdcMonitorAppService.COMPONENT_NAME,
        immediate = true)
@SuppressWarnings("unused")
public class MdcMonitorAppServiceImpl implements MdcMonitorAppService , TranslationKeyProvider, PrivilegesProvider{

    private final Logger logger = Logger.getLogger(MdcMonitorAppServiceImpl.class.getName());

    public static final String HTTP_RESOURCE_ALIAS = "/comservermonitor";
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

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    private boolean isAllowed(User user) {
        List<? super Privilege> appPrivileges = userService.getPrivileges(COMPONENT_NAME);
        return user.getPrivileges(COMPONENT_NAME).stream().anyMatch(appPrivileges::contains);
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        List<ResourceDefinition> resources = new ArrayList<>();
        resources.add(userService.createModuleResourceWithPrivileges(COMPONENT_NAME,
                                        PrivilegeTranslationKeyPair.RESOURCE_COMMUNICATION_SERVER_MONITOR.getKey(),
                                        PrivilegeTranslationKeyPair.RESOURCE_COMMUNICATION_SERVER_MONITOR_DESCRIPTION.getKey(),
                Collections.singletonList(MdcMonitorAppPrivileges.MONITOR_COMMUNICATION_SERVER)));
        return resources;
    }

    @Override
    public String getModuleName() {
        return COMPONENT_NAME;
    }

    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Stream.of(
                Arrays.stream(PrivilegeTranslationKeyPair.values()))
                .flatMap(Function.identity())
                .collect(Collectors.toList());
    }
}