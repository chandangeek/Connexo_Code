/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.system.utils;

import com.elster.jupiter.system.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SubsystemModel {

    private Set<Component> thirdParties = new HashSet<>();
    private Set<Component> dependencies = new HashSet<>();
    private Set<Component> versionedDependencies = new HashSet<>();

    public void addThirdParties(List<Component> components) {
        thirdParties.addAll(components);
    }

    public void addDependency(Component component) {
        dependencies.add(component);
    }

    public void addDependencies(List<Component> component) {
        dependencies.addAll(component);
    }

    public void addVersionedDependencies(List<Component> component) {
        versionedDependencies.addAll(component);
    }

    public List<Component> mergeDependencies() {
        Map<String, Component> versionedDeps = versionedDependencies.stream()
                .collect(Collectors.toMap(Component::getSymbolicName, Function.identity()));
        List<Component> components = dependencies.stream()
                .map(c -> versionedDeps.getOrDefault(c.getSymbolicName(), c))
                .filter(c -> c.getVersion() != null)
                .collect(Collectors.toList());
        components.addAll(thirdParties);
        return components;
    }
}
