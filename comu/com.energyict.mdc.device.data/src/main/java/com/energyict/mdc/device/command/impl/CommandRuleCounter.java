/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.command.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.energyict.mdc.device.command.CommandRule;
import com.energyict.mdc.device.command.ICommandRuleCounter;

import com.google.inject.Inject;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class CommandRuleCounter implements ICommandRuleCounter {

    public enum Fields {

        FROM("from"),
        TO("to"),
        COUNT("count"),
        COMMANDRULE("commandRule");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }

    }

    private final DataModel dataModel;
    private long id;
    private Instant from;
    private Instant to;
    private long count;
    private Reference<CommandRule> commandRule = Reference.empty();

    @Inject
    public CommandRuleCounter(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    public CommandRuleCounter initialize(Instant from, Instant to, Long count) {
        this.from = from;
        this.to = to;
        this.count = count;
        return this;
    }

    public CommandRuleCounter initialize(Instant from, Instant to, Long count, CommandRule commandRule) {
        this.commandRule.set(commandRule);
        return initialize(from, to, count);
    }

    public void increaseCount() {
        this.count++;
        save();
    }

    public void decreaseCount() {
        this.count--;
        save();
    }

    @Override
    public Instant getTo() {
        return to;
    }

    @Override
    public Instant getFrom() {
        return from;
    }

    public CommandRule getCommandRule() {
        return commandRule.get();
    }

    @Override
    public long getCount() {
        return this.count;
    }

    private void save() {
        if (this.id > 0) {
            doUpdate();
        } else {
            doSave();
        }
    }

    private void doSave() {
        Save.CREATE.save(dataModel, this);
    }

    private void doUpdate() {
        Save.UPDATE.save(dataModel, this);
    }

    @Override
    public CounterType getCounterType() {
        long differenceInMillis = getTo().toEpochMilli() - getFrom().toEpochMilli();
        if (differenceInMillis <= TimeUnit.DAYS.toMillis(1)) {
            return CounterType.DAY;
        } else if (differenceInMillis <= TimeUnit.DAYS.toMillis(7)) {
            return CounterType.WEEK;
        } else {
            return CounterType.MONTH;
        }
    }


}
