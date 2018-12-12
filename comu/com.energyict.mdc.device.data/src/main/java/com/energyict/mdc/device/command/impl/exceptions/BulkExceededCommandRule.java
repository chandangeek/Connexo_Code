/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.command.impl.exceptions;

import com.energyict.mdc.device.command.CommandRule;

/**
 * This class is used to verify if the command rule limitation is exceeded
 * when we're adding a command to multiple devices.
 */
public class BulkExceededCommandRule extends ExceededCommandRule {

    private final long availableDevices;
    private long dayAllowedCommands;
    private long weekAllowedCommands;
    private long monthAllowedCommands;

    public BulkExceededCommandRule(CommandRule commandRule, long availableDevices) {
        super(commandRule);
        this.availableDevices = availableDevices;

        // By default, when we're adding commands on multiple devices, we assume the rule limit is exceeded.
        // This is required when we add a command for the first time and there is no counter rule to check against
        this.setDayStatus(0);
        this.setWeekStatus(0);
        this.setMonthStatus(0);
    }
    long getDayAllowedCommands(){
        return this.dayAllowedCommands;
    }
    long getWeekAllowedCommands() {
        return this.weekAllowedCommands;
    }
    long getMonthAllowedCommands() {
        return this.monthAllowedCommands;
    }

    @Override
    void setDayStatus(long countUsedCommands){
        this.dayLimitExceeded = checkLimitExceeded(countUsedCommands, this.dayLimit);
        this.dayAllowedCommands = this.dayLimit - countUsedCommands;
    }

    @Override
    void setWeekStatus(long countUsedCommands){
        this.weekLimitExceeded = checkLimitExceeded(countUsedCommands, this.weekLimit);
        this.weekAllowedCommands = this.weekLimit - countUsedCommands;
    }


    @Override
    void setMonthStatus(long countUsedCommands){
        this.monthLimitExceeded = checkLimitExceeded(countUsedCommands, this.monthLimit);
        this.monthAllowedCommands = this.monthLimit - countUsedCommands;
    }

    @Override
    protected boolean checkLimitExceeded(long countUsedCommands, long checkLimit) {
        return (checkLimit != 0) && (checkLimit - countUsedCommands < this.availableDevices);
    }
}
