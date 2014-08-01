package com.elster.jupiter.users.impl;

import com.elster.jupiter.users.Module;

public class ModuleImpl implements Module {
    private String name;
    private String description;

    ModuleImpl(String name, String description){
        this.name = name;
        this.description = description;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }
}
