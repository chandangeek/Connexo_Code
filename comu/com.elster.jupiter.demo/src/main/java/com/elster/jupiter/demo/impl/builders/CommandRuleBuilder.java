/*
 *  Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.device.command.CommandRule;
import com.energyict.mdc.device.command.CommandRuleService;

import javax.inject.Inject;
import java.util.Optional;

public class CommandRuleBuilder extends NamedBuilder<CommandRule,CommandRuleBuilder> {

    private final CommandRuleService commandRuleService;
    private final ThreadPrincipalService threadPrincipalService;
    private final UserService userService;
    private int dayLimit;
    private int weekLimit;
    private int monthLimit;
    private String command;

    @Inject
    public CommandRuleBuilder(CommandRuleService commandRuleService, ThreadPrincipalService threadPrincipalService, UserService userService) {
        super(CommandRuleBuilder.class);
        this.commandRuleService = commandRuleService;
        this.threadPrincipalService = threadPrincipalService;
        this.userService = userService;
    }

    public CommandRuleBuilder withDayLimit(int dayLimit){
        this.dayLimit = dayLimit;
        return this;
    }

    public CommandRuleBuilder withWeekLimit(int weekLimit){
        this.weekLimit = weekLimit;
        return this;
    }

    public CommandRuleBuilder withMonthLimit(int monthLimit){
        this.monthLimit = monthLimit;
        return this;
    }

    public CommandRuleBuilder forCommand(String command){
        this.command = command;
        return this;
    }

    @Override
    public Optional<CommandRule> find() {
        return commandRuleService.findAllCommandRules().stream().filter(cr -> cr.getName().equals(getName())).findFirst();
    }

    @Override
    public CommandRule create() {
        CommandRule rule = commandRuleService.createRule(getName())
                .command(this.command)
                .dayLimit(this.dayLimit)
                .weekLimit(this.weekLimit)
                .monthLimit(this.monthLimit)
                .add();
        rule.activate();
        threadPrincipalService.set(userService.findUser("Casandra").get());
        rule.approve();
        threadPrincipalService.set(userService.findUser("Govanni").get());
        rule.approve();
        threadPrincipalService.set(userService.findUser("root").get());
        return rule;
    }
}
