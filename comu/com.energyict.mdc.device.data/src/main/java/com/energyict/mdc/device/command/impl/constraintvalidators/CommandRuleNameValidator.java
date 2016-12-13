package com.energyict.mdc.device.command.impl.constraintvalidators;

import com.energyict.mdc.device.command.CommandRule;
import com.energyict.mdc.device.command.CommandRuleService;
import com.energyict.mdc.device.command.CommandRulePendingUpdate;
import com.energyict.mdc.device.command.ServerCommandRule;
import com.energyict.mdc.device.command.impl.CommandRuleImpl;
import com.energyict.mdc.device.command.impl.CommandRulePendingUpdateImpl;

import com.google.inject.Inject;
import org.omg.CORBA.COMM_FAILURE;

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
        if (nameCollisionExists(commandRule)) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate(constraintValidatorContext.getDefaultConstraintMessageTemplate())
                    .addPropertyNode(CommandRuleImpl.Fields.NAME.fieldName())
                    .addConstraintViolation();
            return false;
        }
        return true;
    }

    private boolean nameCollisionExists(ServerCommandRule commandRule) {
        Optional<CommandRule> other = this.commandRuleService.findCommandRuleByName(commandRule.getName());
        Optional<CommandRulePendingUpdate> otherTemplate = this.commandRuleService.findCommandTemplateRuleByName(commandRule.getName());

        boolean collisionWithOtherCommandRule = commandRule instanceof CommandRule && other.isPresent() && other.get().getId() != commandRule.getId();
        boolean collisionAsTemplateWithOtherRule = commandRule instanceof CommandRulePendingUpdate && other.isPresent() && ((CommandRulePendingUpdateImpl)commandRule).getCommandRule().getId() != other.get().getId();
        boolean collisionAsTemplateWithOtherTemplate = commandRule instanceof CommandRulePendingUpdate && otherTemplate.isPresent() && otherTemplate.get().getId() != commandRule.getId();

        return collisionWithOtherCommandRule || collisionAsTemplateWithOtherRule || collisionAsTemplateWithOtherTemplate;
    }
}
