/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.commands;

import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.demo.impl.builders.AppServerBuilder;

import javax.inject.Inject;
import javax.inject.Provider;

public class CreateApplicationServerCommand extends CommandWithTransaction {

    private final Provider<AppServerBuilder> appServerBuilderProvider;
    private String name;

    @Inject
    public CreateApplicationServerCommand(Provider<AppServerBuilder> appServerBuilderProvider) {
        this.appServerBuilderProvider = appServerBuilderProvider;
    }

    public void setName(String  name){
        this.name = name;
    }

    public void run(){
        if (this.name == null){
            throw new UnableToCreate("Please specify name for application server");
        }
        this.appServerBuilderProvider.get().withName(this.name).get();
    }
}
