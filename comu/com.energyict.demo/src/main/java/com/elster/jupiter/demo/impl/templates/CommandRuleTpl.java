/*
 *  Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.templates;

import com.elster.jupiter.demo.impl.builders.CommandRuleBuilder;
import com.energyict.mdc.device.command.CommandRule;

public enum CommandRuleTpl implements Template <CommandRule,CommandRuleBuilder> {
    COMMAND_LIMITATION_DISCONNECT("Limit disconnect","CONTACTOR_OPEN",2,15,100);

    private final String name;
    private final int dayLimit;
    private final int weekLimit;
    private final int monthLimit;
    private final String command;

    CommandRuleTpl(String name, String command,int dayLimit, int weekLimit, int monthLimit) {
        this.name = name;
        this.command = command;
        this.dayLimit = dayLimit;
        this.weekLimit = weekLimit;
        this.monthLimit = monthLimit;

    }

    @Override
    public Class<CommandRuleBuilder> getBuilderClass() {
        return CommandRuleBuilder.class;
    }

    @Override
    public CommandRuleBuilder get(CommandRuleBuilder builder) {
        return builder.withName(this.name()).forCommand(this.command).withDayLimit(this.dayLimit).withWeekLimit(this.weekLimit).withMonthLimit(this.monthLimit);
    }
}
