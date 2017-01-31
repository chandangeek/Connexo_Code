/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.system.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.system.RuntimeComponent;
import com.elster.jupiter.system.Subsystem;
import com.elster.jupiter.system.SubsystemService;
import com.elster.jupiter.system.security.Privileges;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;

import com.google.inject.AbstractModule;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@org.osgi.service.component.annotations.Component(name = "com.elster.jupiter.system.impl",
        service = {SubsystemService.class, TranslationKeyProvider.class},
        property = "name=" + SubsystemService.COMPONENTNAME, immediate = true)
public class SubsystemServiceImpl implements SubsystemService, TranslationKeyProvider {

    private static final Logger LOGGER = Logger.getLogger(SubsystemServiceImpl.class.getName());

    private volatile UserService userService;
    private BundleContext bundleContext;
    private UpgradeService upgradeService;

    private List<Subsystem> subsystems = new ArrayList<>();

    @Override
    public List<Subsystem> getSubsystems() {
        return this.subsystems;
    }

    @Override
    public void registerSubsystem(Subsystem subsystem) {
        this.subsystems.add(subsystem);
    }

    @Override
    public void unregisterSubsystem(Subsystem subsystem) {
        this.subsystems.remove(subsystem);
    }

    @Override
    public List<RuntimeComponent> getRuntimeComponents() {
        List<RuntimeComponent> resultList = new ArrayList<>();
        Map<String, Bundle> bundles = Stream.of(bundleContext.getBundles())
                .collect(Collectors.toMap(Bundle::getSymbolicName, Function.identity(), (bundle, bundle2) -> bundle));
        for (Subsystem subsystem : subsystems) {
            List<RuntimeComponent> runtimeComponents = subsystem.getComponents().stream()
                    .map(component -> {
                        String symbolicName = component.getSymbolicName();
                        String version = component.getVersion();
                        Bundle bundle = bundles.get(symbolicName);
                        if (bundle != null && bundle.getHeaders().get("Bundle-Version").equals(version)) {
                            return new RuntimeComponentImpl(bundle, component, subsystem);
                        } else {
                            LOGGER.log(Level.FINE, "Bundle with symbolic name '" + symbolicName + "' and version '" + version + "' not found for " + subsystem
                                    .getId());
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            resultList.addAll(runtimeComponents);
        }
        return resultList;
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
        public String getModuleName() {
            return SubsystemService.COMPONENTNAME;
        }

        @Override
        public List<ResourceDefinition> getModuleResources() {
            List<ResourceDefinition> resources = new ArrayList<>();
            resources.add(userService.createModuleResourceWithPrivileges(getModuleName(),
                    Privileges.RESOURCE_DEPLOYMENT_INFO.getKey(), Privileges.RESOURCE_DEPLOYMENT_INFO_DESCRIPTION.getKey(),
                    Arrays.asList(Privileges.Constants.VIEW_DEPLOYMENT_INFORMATION)));
            return resources;
        }
    }

    @Override
    public String getComponentName() {
        return SubsystemService.COMPONENTNAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Stream.of(
                Arrays.stream(Privileges.values()))
                .flatMap(Function.identity())
                .collect(Collectors.toList());
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Activate
    public void activate(BundleContext context) {
        this.bundleContext = context;
        DataModel dataModel = upgradeService.newNonOrmDataModel();
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(UserService.class).toInstance(userService);
            }
        });
        upgradeService.register(InstallIdentifier.identifier("Pulse", COMPONENTNAME), dataModel, Installer.class, Collections.emptyMap());
    }

}
