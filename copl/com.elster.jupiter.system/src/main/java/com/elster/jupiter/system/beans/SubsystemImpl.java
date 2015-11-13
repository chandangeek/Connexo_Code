package com.elster.jupiter.system.beans;

import com.elster.jupiter.system.Component;
import com.elster.jupiter.system.Subsystem;

import java.util.List;

public class SubsystemImpl implements Subsystem {

    private String id;
    private String name;
    private String version;
    private List<Component> components;

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

    @Override
    public void addComponents(List<Component> components) {
        this.components = components;
    }

}
