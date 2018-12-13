/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.command.impl;

import com.energyict.mdc.device.command.CommandRule;
import com.energyict.mdc.device.command.impl.exceptions.BulkExceededCommandRule;
import com.energyict.mdc.device.command.impl.exceptions.ExceededCommandRule;

import com.google.common.collect.Range;

import java.time.Instant;

class ExceededCommandRuleBuilder {

    private final CommandRule commandRule;
    private final Instant releaseDate;
    private Instant oldReleaseDate;
    private long availableDevices;


    ExceededCommandRuleBuilder(CommandRule commandRule, Instant releaseDate) {
        this.commandRule = commandRule;
        this.releaseDate = releaseDate;
    }

    ExceededCommandRuleBuilder oldReleaseDate(Instant date) {
        this.oldReleaseDate = date;
        return this;
    }

    ExceededCommandRuleBuilder availableDevices(long size) {
        this.availableDevices = size;
        return this;
    }

    ExceededCommandRule build() {
        ExceededCommandRule exceededCommandRule;
        if (this.availableDevices > 0){
            exceededCommandRule = new BulkExceededCommandRule(commandRule, availableDevices);
        } else {
            exceededCommandRule = new ExceededCommandRule(commandRule);
        }

        this.setExceededStatus(exceededCommandRule);
        return exceededCommandRule;
    }

    /**
     * Iterate through all command rule counters in the DB and check if the command
     * is exceeding the rule's limits and the release date matches the counter period.
     * @param exceededCommandRule exceeded object to be updated
     */
    private void setExceededStatus(ExceededCommandRule exceededCommandRule) {
        commandRule.getCounters()
                .stream()
                .map(CommandRuleCounter.class::cast)
                .filter(commandRuleCounter -> Range.closedOpen(commandRuleCounter.getFrom(), commandRuleCounter.getTo()).contains(releaseDate))
                .forEach(commandRuleCounter -> exceededCommandRule.setStatus(commandRuleCounter.getCounterType(), this.getCurrentUsedCommands(commandRuleCounter, oldReleaseDate)));
    }

    private long getCurrentUsedCommands(CommandRuleCounter commandRuleCounter, Instant oldReleaseDate) {
        long currentCount = commandRuleCounter.getCount();
        if (oldReleaseDate != null
                && Range.closedOpen(commandRuleCounter.getFrom(), commandRuleCounter.getTo()).contains(oldReleaseDate)) {
            currentCount--;
        }
        return currentCount;
    }
}
