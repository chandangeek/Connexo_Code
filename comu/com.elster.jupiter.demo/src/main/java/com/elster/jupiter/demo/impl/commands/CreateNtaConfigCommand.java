package com.elster.jupiter.demo.impl.commands;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.builders.NTASimToolBuilder;

import javax.inject.Inject;

public class CreateNtaConfigCommand {
    @Inject
    public CreateNtaConfigCommand() {
    }

    public void run(){
        Builders.from(NTASimToolBuilder.class).get();
    }
}
