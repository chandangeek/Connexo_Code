/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.system.beans;

import com.elster.jupiter.system.Component;
import com.elster.jupiter.system.Subsystem;

import java.util.ArrayList;
import java.util.List;

public class SubsystemImpl implements Subsystem {

    private final String id;
    private final String name;
    private final String version;
    private final List<Component> components = new ArrayList<>();

    public SubsystemImpl(String id, String name, String version) {
        this.id = id;
        this.name = name;
        this.version = version;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getVersion() {
        return this.version;
    }

    @Override
    public List<Component> getComponents() {
        return this.components;
    }

    public void addComponents(List<Component> components) {
        this.components.addAll(components);
    }
}
