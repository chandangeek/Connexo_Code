/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.commands;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.builders.NTASimToolBuilder;

import javax.inject.Inject;

public class CreateNtaConfigCommand extends CommandWithTransaction {
    @Inject
    public CreateNtaConfigCommand() {
    }

    public void run(){
        Builders.from(NTASimToolBuilder.class).get();
    }
}
