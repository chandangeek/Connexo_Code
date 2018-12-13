/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.command.impl.exceptions;


import com.energyict.mdc.device.command.CommandRule;
import com.energyict.mdc.device.command.impl.CommandRuleCounter;


/**
 * This class is used to verify if the command rule limitation is exceeded
 * when we're adding a command to a single device
 */
public class ExceededCommandRule {
    private String name;
    boolean dayLimitExceeded;
    boolean weekLimitExceeded;
    boolean monthLimitExceeded;

    final long dayLimit;
    final long weekLimit;
    final long monthLimit;

    public ExceededCommandRule(CommandRule commandRule) {
        this.name = commandRule.getName();
        this.dayLimit = commandRule.getDayLimit();
        this.weekLimit = commandRule.getWeekLimit();
        this.monthLimit = commandRule.getMonthLimit();
    }

    public String getName() {
        return name;
    }

    public boolean isLimitExceeded() {
        return isDayLimitExceeded() || isWeekLimitExceeded() || isMonthLimitExceeded();
    }

    public void setStatus(CommandRuleCounter.CounterType type, long countUsedCommands) {
        switch (type) {
            case DAY:
                this.setDayStatus(countUsedCommands);
                break;
            case WEEK:
                this.setWeekStatus(countUsedCommands);
                break;
            case MONTH:
                this.setMonthStatus(countUsedCommands);
            default:
                break;
        }
    }

    boolean isDayLimitExceeded() {
        return this.dayLimitExceeded;
    }
    boolean isWeekLimitExceeded() {
        return this.weekLimitExceeded;
    }
    boolean isMonthLimitExceeded() {
        return this.monthLimitExceeded;
    }


    void setDayStatus(long countUsedCommands){
        this.dayLimitExceeded = checkLimitExceeded(countUsedCommands, this.dayLimit);
    }
    void setWeekStatus(long countUsedCommands){
        this.weekLimitExceeded = checkLimitExceeded(countUsedCommands, this.weekLimit);
    }
    void setMonthStatus(long countUsedCommands){
        this.monthLimitExceeded = checkLimitExceeded(countUsedCommands, this.monthLimit);
    }

    boolean checkLimitExceeded(long countUsedCommands, long checkLimit) {
        return (checkLimit != 0) && (countUsedCommands >= checkLimit);
    }

}
