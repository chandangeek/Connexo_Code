package com.energyict.mdc.device.data.impl.constraintvalidators;

import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.device.command.CommandRule;
import com.energyict.mdc.device.command.CommandRuleService;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.device.data.impl.DeviceMessageImpl;
import com.energyict.mdc.device.data.impl.MessageSeeds;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageAttribute;

import com.google.inject.Inject;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;

public class DoesNotExceedCommandLimitationRulesValidator implements ConstraintValidator<DoesNotExceedCommandLimitationRules, DeviceMessageImpl> {

    private final CommandRuleService commandRuleService;
    private final DeviceMessageService deviceMessageService;

    @Inject
    public DoesNotExceedCommandLimitationRulesValidator(CommandRuleService commandRuleService, DeviceMessageService deviceMessageService) {
        this.commandRuleService = commandRuleService;
        this.deviceMessageService = deviceMessageService;
    }

    @Override
    public void initialize(DoesNotExceedCommandLimitationRules hasValidDeviceMessageAttributes) {

    }

    @Override
    public boolean isValid(DeviceMessageImpl deviceMessage, ConstraintValidatorContext context) {
        List<CommandRule> applicableCommandRules = commandRuleService.getCommandRulesByDeviceMessageId(deviceMessage.getDeviceMessageId());
        if(applicableCommandRules.isEmpty()) {
            return true;
        }
        long currentDayCount = deviceMessageService.getCurrentDayCountFor(deviceMessage);
        long currentWeekCount = deviceMessageService.getCurrentWeekCountFor(deviceMessage);
        long currentMonthCount = deviceMessageService.getCurrentMonthCountFor(deviceMessage);
        Optional<CommandRule> surpassedCommandRule = applicableCommandRules.stream()
                .filter(commandRule -> wouldCommandRuleBeInvalid(currentDayCount, currentWeekCount, currentMonthCount, commandRule))
                .findFirst();
        if (surpassedCommandRule.isPresent()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode(DeviceMessageImpl.Fields.RELEASEDATE.fieldName()).addConstraintViolation();
            return false;
        }
        return true;
    }

    private boolean wouldCommandRuleBeInvalid(long currentDayCount, long currentWeekCount, long currentMonthCount, CommandRule commandRule) {
        return (commandRule.getDayLimit() <= currentDayCount && commandRule.getDayLimit() != 0)
                || (commandRule.getWeekLimit() <= currentWeekCount && commandRule.getWeekLimit() != 0)
                || (commandRule.getMonthLimit() <= currentMonthCount && commandRule.getMonthLimit() != 0);
    }


}