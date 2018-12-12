/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.command.impl.constraintvalidators;

import com.energyict.mdc.device.command.ServerCommandRule;
import com.energyict.mdc.device.command.impl.CommandRuleImpl;
import com.energyict.mdc.device.command.impl.MessageSeeds;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class LimitsValidator implements ConstraintValidator<HasValidLimits, ServerCommandRule> {

    private ConstraintValidatorContext context;

    @Override
    public void initialize(HasValidLimits hasValidLimits) {

    }

    @Override
    public boolean isValid(ServerCommandRule commandRule, ConstraintValidatorContext context) {
        this.context = context;
        boolean weekLimitSmallerThanDayLimit = commandRule.getDayLimit() > commandRule.getWeekLimit() && !(commandRule.getWeekLimit() < 1);
        boolean monthLimitSmallerThanDayLimit = commandRule.getDayLimit() > commandRule.getMonthLimit() && !(commandRule.getMonthLimit() < 1);
        boolean monthLimitSmallerThanWeekLimit = commandRule.getWeekLimit() > commandRule.getMonthLimit() && !(commandRule.getMonthLimit() < 1);

        if(weekLimitSmallerThanDayLimit && monthLimitSmallerThanDayLimit && monthLimitSmallerThanWeekLimit) {
            createDayFieldViolation(MessageSeeds.Keys.DAY_LIMIT_SMALLER_THAN_WEEK_AND_MONTH);
            createWeekFieldViolation(MessageSeeds.Keys.WEEK_LIMIT_BIGGER_THAN_DAY_SMALLER_THAN_MONTH);
            createMonthFieldViolation(MessageSeeds.Keys.MONTH_LIMIT_BIGGER_THAN_DAY_AND_WEEK);
            return false;
        } else if (weekLimitSmallerThanDayLimit && monthLimitSmallerThanDayLimit) {
            createDayFieldViolation(MessageSeeds.Keys.DAY_LIMIT_SMALLER_THAN_WEEK_AND_MONTH);
            createWeekFieldViolation(MessageSeeds.Keys.WEEK_LIMIT_BIGGER_THAN_DAY);
            createMonthFieldViolation(MessageSeeds.Keys.MONTH_LIMIT_BIGGER_THAN_DAY);
            return false;
        } else if (weekLimitSmallerThanDayLimit && monthLimitSmallerThanWeekLimit) {
            createDayFieldViolation(MessageSeeds.Keys.DAY_LIMIT_SMALLER_THAN_WEEK);
            createWeekFieldViolation(MessageSeeds.Keys.WEEK_LIMIT_BIGGER_THAN_DAY_SMALLER_THAN_MONTH);
            createMonthFieldViolation(MessageSeeds.Keys.MONTH_LIMIT_BIGGER_THAN_WEEK);
            return false;
        } else if (monthLimitSmallerThanDayLimit && monthLimitSmallerThanWeekLimit) {
            createDayFieldViolation(MessageSeeds.Keys.DAY_LIMIT_SMALLER_THAN_MONTH);
            createWeekFieldViolation(MessageSeeds.Keys.WEEK_LIMIT_SMALLER_THAN_MONTH);
            createMonthFieldViolation(MessageSeeds.Keys.MONTH_LIMIT_BIGGER_THAN_DAY_AND_WEEK);
            return false;
        } else if (weekLimitSmallerThanDayLimit) {
            createDayFieldViolation(MessageSeeds.Keys.DAY_LIMIT_SMALLER_THAN_WEEK);
            createWeekFieldViolation(MessageSeeds.Keys.WEEK_LIMIT_BIGGER_THAN_DAY);
            return false;
        } else if (monthLimitSmallerThanDayLimit) {
            createDayFieldViolation(MessageSeeds.Keys.DAY_LIMIT_SMALLER_THAN_MONTH);
            createMonthFieldViolation(MessageSeeds.Keys.MONTH_LIMIT_BIGGER_THAN_DAY);
            return false;
        } else if (monthLimitSmallerThanWeekLimit) {
            createWeekFieldViolation(MessageSeeds.Keys.WEEK_LIMIT_SMALLER_THAN_MONTH);
            createMonthFieldViolation(MessageSeeds.Keys.MONTH_LIMIT_BIGGER_THAN_WEEK);
            return false;
        }
        return true;
    }

    private void createViolation(String message, String fieldName) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate("{" + message + "}")
                .addPropertyNode(fieldName)
                .addConstraintViolation();
    }

    private void createDayFieldViolation(String message) {
        createViolation(message, CommandRuleImpl.Fields.DAYLIMIT.fieldName());
    }
    private void createWeekFieldViolation( String message) {
        createViolation(message, CommandRuleImpl.Fields.WEEKLIMIT.fieldName());
    }

    private void createMonthFieldViolation(String message) {
        createViolation(message, CommandRuleImpl.Fields.MONTHLIMIT.fieldName());
    }
}
