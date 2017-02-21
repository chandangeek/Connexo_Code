/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.command.impl.constraintvalidators;

import com.energyict.mdc.device.command.CommandInRule;
import com.energyict.mdc.device.command.ServerCommandRule;
import com.energyict.mdc.device.command.impl.CommandRuleImpl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.stream.Collectors;

public class CommandsValidator implements ConstraintValidator<HasUniqueCommands, ServerCommandRule> {
    @Override
    public void initialize(HasUniqueCommands hasUniqueCommands) {

    }

    @Override
    public boolean isValid(ServerCommandRule serverCommandRule, ConstraintValidatorContext context) {
        List<CommandInRule> distinctCommands = serverCommandRule.getCommands()
                .stream()
                .distinct()
                .collect(Collectors.toList());

        if(serverCommandRule.getCommands().size() != distinctCommands.size()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode(CommandRuleImpl.Fields.COMMANDS.fieldName())
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
