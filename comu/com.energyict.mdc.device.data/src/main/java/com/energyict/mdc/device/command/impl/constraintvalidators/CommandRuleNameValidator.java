package com.energyict.mdc.device.command.impl.constraintvalidators;

import com.energyict.mdc.device.command.CommandRule;
import com.energyict.mdc.device.command.CommandRuleService;
import com.energyict.mdc.device.command.CommandRuleTemplate;
import com.energyict.mdc.device.command.ServerCommandRule;
import com.energyict.mdc.device.command.impl.CommandRuleImpl;

import com.google.inject.Inject;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

public class CommandRuleNameValidator implements ConstraintValidator<UniqueName, ServerCommandRule> {

    private CommandRuleService commandRuleService;

    @Inject
    public CommandRuleNameValidator(CommandRuleService commandRuleService) {
        this.commandRuleService = commandRuleService;
    }

    @Override
    public void initialize(UniqueName uniqueName) {

    }

    @Override
    public boolean isValid(ServerCommandRule commandRule, ConstraintValidatorContext constraintValidatorContext) {
        Optional<CommandRule> other = this.commandRuleService.findCommandRuleByName(commandRule.getName());
        Optional<CommandRuleTemplate> otherTemplate = this.commandRuleService.findCommandTemplateRuleByName(commandRule.getName());
        if (other.isPresent() && other.get().getId() != commandRule.getId()) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate(constraintValidatorContext.getDefaultConstraintMessageTemplate())
                    .addPropertyNode(CommandRuleImpl.Fields.NAME.fieldName())
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
