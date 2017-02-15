/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.engine.impl.MessageSeeds;
import com.energyict.mdc.engine.impl.commands.collect.ComCommand;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandType;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.LogBooksCommand;
import com.energyict.mdc.engine.impl.commands.collect.ReadLogBooksCommand;
import com.energyict.mdc.engine.impl.commands.store.core.CompositeComCommandImpl;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.protocol.api.LogBookReader;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.tasks.LogBooksTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple command to read out the requested {@link com.energyict.mdc.protocol.api.device.BaseLogBook logBooks} from the device
 *
 * @author sva
 * @since 07/12/12 - 11:36
 */
public class LogBooksCommandImpl extends CompositeComCommandImpl implements LogBooksCommand {

    /**
     * The used {@link OfflineDevice} which contains relevant information for this {@link ComCommand}
     */
    private final OfflineDevice device;
    private final ReadLogBooksCommand readLogBooksCommand;
    private List<LogBookReader> logBookReaders = new ArrayList<>();

    public LogBooksCommandImpl(final GroupedDeviceCommand groupedDeviceCommand, final LogBooksTask logBooksTask, ComTaskExecution comTaskExecution) {
        super(groupedDeviceCommand);
        if (groupedDeviceCommand == null) {
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "constructor", "commandRoot", MessageSeeds.METHOD_ARGUMENT_CAN_NOT_BE_NULL);
        }
        if (groupedDeviceCommand.getOfflineDevice() == null) {
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "constructor", "offlineDevice", MessageSeeds.METHOD_ARGUMENT_CAN_NOT_BE_NULL);
        }
        if (logBooksTask == null) {
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "constructor", "logbookstask", MessageSeeds.METHOD_ARGUMENT_CAN_NOT_BE_NULL);
        }
        this.device = groupedDeviceCommand.getOfflineDevice();
        this.logBookReaders.addAll(LogBookCommandHelper.createLogBookReaders(getCommandRoot().getServiceProvider(), logBooksTask, this.device, comTaskExecution));
        this.readLogBooksCommand = getGroupedDeviceCommand().getReadLogBooksCommand(this, comTaskExecution);
    }

    @Override
    public String getDescriptionTitle() {
        return "Executed logbook protocol task";
    }

    @Override
    protected LogLevel defaultJournalingLogLevel() {
        return LogLevel.DEBUG;
    }

    @Override
    public void updateAccordingTo(LogBooksTask logBooksTask, GroupedDeviceCommand groupedDeviceCommand, ComTaskExecution comTaskExecution) {
        List<LogBookReader> newLogBookReaders = LogBookCommandHelper.createLogBookReaders(getCommandRoot().getServiceProvider(), logBooksTask, this.device, comTaskExecution);
        for (LogBookReader newLogBookReader : newLogBookReaders) {
            if (LogBookCommandHelper.canWeAddIt(this.logBookReaders, newLogBookReader)) {
                this.logBookReaders.add(newLogBookReader);
            }
        }
    }

    public List<LogBookReader> getLogBookReaders() {
        return logBookReaders;
    }

    @Override
    public ComCommandType getCommandType() {
        return ComCommandTypes.LOGBOOKS_COMMAND;
    }
}