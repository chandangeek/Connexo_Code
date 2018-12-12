/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.command.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.energyict.mdc.device.command.CommandInRule;
import com.energyict.mdc.device.command.CommandRule;
import com.energyict.mdc.device.command.CommandRulePendingUpdate;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;

import com.google.inject.Inject;

import java.util.Objects;
import java.util.Optional;

public class CommandInRuleImpl implements CommandInRule {

    enum Fields {

        COMMAND("command"),
        COMMANDRULE("commandRule"),
        COMMANDRULEPENDINGUPDATE("commandRulePendingUpdate"),
        COMMANDID("commandId");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }

    }

    private long id;

    private DeviceMessageSpec command;
    private Reference<CommandRule> commandRule = Reference.empty();
    private Reference<CommandRulePendingUpdate> commandRulePendingUpdate = Reference.empty();
    private long commandId;
    private final DeviceMessageSpecificationService deviceMessageSpecificationService;
    private final DataModel dataModel;

    @Inject
    public CommandInRuleImpl(DataModel dataModel, DeviceMessageSpecificationService deviceMessageSpecificationService) {
        this.dataModel = dataModel;
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
    }

    @Override
    public long getId() {
        return id;
    }

    CommandInRuleImpl initialize(DeviceMessageSpec command, CommandRule commandRule) {
        this.command = command;
        this.commandId = command.getId().dbValue();
        this.commandRule.set(commandRule);
        return this;
    }

    CommandInRuleImpl initialize(DeviceMessageSpec command, CommandRulePendingUpdate commandRuleTemplate) {
        this.command = command;
        this.commandId = command.getId().dbValue();
        this.commandRulePendingUpdate.set(commandRuleTemplate);
        return this;
    }

    @Override
    public DeviceMessageSpec getCommand() {
        if (this.command == null) {
            this.command = this.deviceMessageSpecificationService.findMessageSpecById(this.commandId).orElse(null);
        }
        return this.command;
    }

    @Override
    public CommandRule getCommandRule() {
        return commandRule.get();
    }

    public Optional<CommandRulePendingUpdate> getCommandRulePendingUpdate() {
        return this.commandRulePendingUpdate.getOptional();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CommandInRuleImpl that = (CommandInRuleImpl) o;
        return commandId == that.commandId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(commandId);
    }
}
