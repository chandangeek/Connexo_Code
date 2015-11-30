package com.elster.jupiter.system.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.system.RuntimeComponent;
import com.elster.jupiter.system.Subsystem;
import com.elster.jupiter.system.SubsystemService;
import com.elster.jupiter.system.security.Privileges;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Reference;

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
        service = {SubsystemService.class, PrivilegesProvider.class, TranslationKeyProvider.class, InstallService.class},
        property = "name=" + SubsystemService.COMPONENTNAME, immediate = true)
public class SubsystemServiceImpl implements SubsystemService, PrivilegesProvider, TranslationKeyProvider, InstallService {

    private static final Logger LOGGER = Logger.getLogger(SubsystemServiceImpl.class.getName());

    private volatile UserService userService;
    private BundleContext bundleContext;

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
        Map<String, Bundle> bundles = Stream.of(bundleContext.getBundles()).collect(Collectors.toMap(Bundle::getSymbolicName, Function.identity(), (bundle, bundle2) -> bundle));
        for (Subsystem subsystem : subsystems) {
            List<RuntimeComponent> runtimeComponents = subsystem.getComponents().stream()
                    .map(component -> {
                        String symbolicName = component.getSymbolicName();
                        String version = component.getVersion();
                        Bundle bundle = bundles.get(symbolicName);
                        if (bundle != null && bundle.getHeaders().get("Bundle-Version").equals(version)) {
                            return new RuntimeComponentImpl(bundle, component, subsystem);
                        } else {
                            LOGGER.log(Level.FINE, "Bundle with symbolic name '" + symbolicName + "' and version '" + version + "' not found for " + subsystem.getId());
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            resultList.addAll(runtimeComponents);
        }
        return resultList;
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

    @Activate
    public void activate(BundleContext context) {
        this.bundleContext = context;
    }

    @Override
    public void install() {
        // NO-OP
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Collections.emptyList();
    }
}
