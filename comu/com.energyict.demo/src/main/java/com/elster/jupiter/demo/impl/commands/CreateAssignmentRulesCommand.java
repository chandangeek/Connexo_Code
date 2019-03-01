/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.commands;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.templates.AssignmentRuleTpl;

import javax.inject.Inject;

public class CreateAssignmentRulesCommand {

    @Inject
    public CreateAssignmentRulesCommand() {
    }

    public void run(){
        for (AssignmentRuleTpl ruleTpl : AssignmentRuleTpl.values()) {
            Builders.from(ruleTpl).get();
        }
    }
}
