/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.comserver.logging.PropertyDescriptionBuilder;
import com.energyict.mdc.engine.impl.commands.collect.AlreadyExecutedComCommand;
import com.energyict.mdc.engine.impl.commands.collect.ComCommand;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandType;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.protocol.api.DeviceProtocol;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sva
 * @since 9/06/2015 - 16:11
 */
public class AlreadyExecutedComCommandImpl extends NoopCommandImpl implements AlreadyExecutedComCommand {

    private List<ComCommandType> comCommandTypes = new ArrayList<>();
    private List<ComCommand> linkedComCommands = new ArrayList<>();

    public AlreadyExecutedComCommandImpl(GroupedDeviceCommand groupedDeviceCommand, ComCommandType comCommandType) {
        super(groupedDeviceCommand);
        this.comCommandTypes.add(comCommandType);
    }

    @Override
    public void linkToComCommandDoingActualExecution(ComCommandTypes comCommandType, ComCommand comCommand) {
        if (!this.comCommandTypes.contains(comCommandType)) {
            this.comCommandTypes.add(comCommandType);
        }

        this.linkedComCommands.add(comCommand);
    }

    @Override
    public void doExecute(DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        for (ComCommand linkedComCommand : linkedComCommands) {
            // Copy over all the issues of the linked ComCommands
            for (Issue issue : linkedComCommand.getIssues()) {
                this.addIssue(issue, linkedComCommand.getCompletionCode());
            }
        }
        super.doExecute(deviceProtocol, executionContext);
    }

    @Override
    public List<Issue> getIssues() {
        return super.getIssues();
    }

    @Override
    protected LogLevel defaultJournalingLogLevel() {
        return LogLevel.INFO;
    }

    @Override
    protected void toJournalMessageDescription(DescriptionBuilder builder, LogLevel serverLogLevel) {
        PropertyDescriptionBuilder descriptionBuilder = builder.addListProperty("Types");
        for (ComCommandType comCommandType : this.comCommandTypes) {
            descriptionBuilder.append(((ComCommandTypes) comCommandType).name()).next();
        }
    }

    @Override
    public ComCommandTypes getCommandType() {
        return ComCommandTypes.ALREADY_EXECUTED;
    }
}
