package com.elster.jupiter.system;

public class RuntimeComponent {
    private long id;
    private String name;
    private ComponentStatus status;
    private Component component;

    public RuntimeComponent(long id, String name, ComponentStatus status, Component component) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.component = component;
    }

    public ComponentStatus getStatus() {
        return status;
    }

    public Component getComponent() {
        return component;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
