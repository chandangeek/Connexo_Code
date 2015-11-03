package com.elster.jupiter.system.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.system.Subsystem;
import com.elster.jupiter.system.SubsystemService;
import com.elster.jupiter.system.security.Privileges;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;
import org.osgi.service.component.annotations.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component(name = "com.elster.jupiter.system.impl", service = {SubsystemService.class, PrivilegesProvider.class, TranslationKeyProvider.class},
        property = {"name=" + SubsystemService.COMPONENTNAME},immediate = true)
public class SubsystemServiceImpl implements SubsystemService, PrivilegesProvider, TranslationKeyProvider {
    private List<Subsystem> subsystems = new ArrayList<>();
    private volatile UserService userService;

    public SubsystemServiceImpl() {
    }

    @Override
    public List<Subsystem> getSubsystems() {
        return this.subsystems;
    }

    @Override
    public void registerSubsystem(Subsystem subsystem) {
        this.subsystems.add(subsystem);
    }

    @Override
    public String getModuleName() {
        return SubsystemService.COMPONENTNAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        List<ResourceDefinition> resources = new ArrayList<>();
        resources.add(userService.createModuleResourceWithPrivileges(getModuleName(),
                "deployment.info", "deployment.info.description",
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
}
