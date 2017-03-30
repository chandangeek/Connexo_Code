/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.commands;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.templates.CreationRuleTpl;

import javax.inject.Inject;

public class CreateAlarmCreationRuleCommand {

    @Inject
    public CreateAlarmCreationRuleCommand() {
    }

    public void run() {
        Builders.from(CreationRuleTpl.DEVICE_ALARM).get();
    }
}
