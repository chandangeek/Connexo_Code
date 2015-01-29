package com.elster.jupiter.demo.impl.commands;

import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.demo.impl.builders.AppServerBuilder;

import javax.inject.Inject;
import javax.inject.Provider;

public class CreateApplicationServerCommand {

    private final Provider<AppServerBuilder> appServerProvider;
    private String name;

    @Inject
    public CreateApplicationServerCommand(Provider<AppServerBuilder> appServerProvider) {
        this.appServerProvider = appServerProvider;
    }

    public void setName(String  name){
        this.name = name;
    }

    public void run(){
        if (this.name == null){
            throw new UnableToCreate("Please specify name for application server");
        }
        this.appServerProvider.get().withName(this.name.toUpperCase()).get();
    }
}
