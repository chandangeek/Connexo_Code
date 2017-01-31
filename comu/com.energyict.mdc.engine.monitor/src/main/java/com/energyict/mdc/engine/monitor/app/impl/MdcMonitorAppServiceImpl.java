/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.monitor.app.impl;

import com.elster.jupiter.http.whiteboard.BundleResolver;
import com.elster.jupiter.http.whiteboard.HttpResource;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.monitor.app.MdcMonitorAppService;
import com.energyict.mdc.engine.monitor.app.security.MdcMonitorAppPrivileges;
import com.energyict.mdc.engine.monitor.app.security.PrivilegeTranslationKeyPair;
import com.energyict.mdc.engine.status.StatusService;

import com.google.inject.AbstractModule;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component(
        name = "com.energyict.mdc.engine.monitor.app",
        service = {MdcMonitorAppService.class, TranslationKeyProvider.class},
        property = "name=" + MdcMonitorAppService.COMPONENT_NAME,
        immediate = true)
@SuppressWarnings("unused")
public class MdcMonitorAppServiceImpl implements MdcMonitorAppService , TranslationKeyProvider {

    private final Logger logger = Logger.getLogger(MdcMonitorAppServiceImpl.class.getName());

    public static final String HTTP_RESOURCE_ALIAS = "/comservermonitor";
    public static final String HTTP_RESOURCE_LOCAL_NAME = "/js/mon";

    public static final String APPLICATION_ICON = "connexo";

    private volatile ServiceRegistration<HttpResource> registration;
    private volatile StatusService statusService;
    private volatile EngineConfigurationService engineConfigurationService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile UserService userService;
    private volatile UpgradeService upgradeService;

    public MdcMonitorAppServiceImpl() {
    }

    @Inject
    public MdcMonitorAppServiceImpl(StatusService statusService,
                                    EngineConfigurationService engineConfigurationService,
                                    ThreadPrincipalService threadPrincipalService,
                                    UserService userService, BundleContext context,
                                    UpgradeService upgradeService) {
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
        DataModel dataModel = upgradeService.newNonOrmDataModel();
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(UserService.class).toInstance(userService);
            }
        });

        upgradeService.register(InstallIdentifier.identifier("MultiSense", COMPONENT_NAME), dataModel, Installer.class, Collections.emptyMap());
    }

    static class Installer implements FullInstaller, PrivilegesProvider {
        private final UserService userService;

        @Inject
        Installer(UserService userService) {
            this.userService = userService;
        }

        @Override
        public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
            userService.addModulePrivileges(this);
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

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    private boolean isAllowed(User user) {
        List<? super Privilege> appPrivileges = userService.getPrivileges(COMPONENT_NAME);
        return user.getPrivileges(COMPONENT_NAME).stream().anyMatch(appPrivileges::contains);
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