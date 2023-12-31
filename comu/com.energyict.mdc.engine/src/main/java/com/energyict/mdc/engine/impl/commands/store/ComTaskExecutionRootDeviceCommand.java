/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilderImpl;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.core.ComServerDAO;

import java.util.Iterator;
import java.util.List;

/**
 * Provides an implementation for the {@link CompositeDeviceCommand} interface
 * that will contains all the {@link DeviceCommand}s that are related the same {@link ComTaskExecution}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-09-17 (11:54)
 */
public class ComTaskExecutionRootDeviceCommand extends CompositeDeviceCommandImpl {

    private ComTaskExecution comTaskExecution;
    private ExecutionLogger executionLogger;

    public ComTaskExecutionRootDeviceCommand(ComTaskExecution comTaskExecution, ComServer.LogLevel communicationLogLevel, List<DeviceCommand> commands) {
        super(communicationLogLevel, commands);
        this.comTaskExecution = comTaskExecution;
    }

    @Override
    public void execute(final ComServerDAO comServerDAO) {
        try {
            executeAll(comServerDAO);
            this.markExecutionFailedWhenProblemsWereLogged(comServerDAO);
        } catch (Exception t) {
            handleUnexpectedError(t, comServerDAO);
        }
    }

    @Override
    public void executeDuringShutdown(final ComServerDAO comServerDAO) {
        try {
            executeAllDuringShutdown(comServerDAO);
            this.markExecutionFailedWhenProblemsWereLogged(comServerDAO);
        } catch (Exception t) {
            handleUnexpectedError(t, comServerDAO);
        }
    }

    private void markExecutionFailedWhenProblemsWereLogged(ComServerDAO comServerDAO) {
        if (this.executionLogger.hasProblems()) {
            comServerDAO.executionFailed(this.comTaskExecution);
        }
    }

    @Override
    public void logExecutionWith(ExecutionLogger logger) {
        this.executionLogger = logger;
        broadCastExecutionLoggerIfAny();
    }

    private void handleUnexpectedError(Throwable t, final ComServerDAO comServerDAO) {
        this.executionLogger.logUnexpected(t, this.comTaskExecution);
        comServerDAO.executionFailed(this.comTaskExecution);
    }

    private void broadCastExecutionLoggerIfAny() {
        if (this.executionLogger != null) {
            for (DeviceCommand deviceCommand : super.getChildren()) {
                deviceCommand.logExecutionWith(this.executionLogger);
            }
        }
    }

    @Override
    public String toJournalMessageDescription(ComServer.LogLevel serverLogLevel) {
        DescriptionBuilder descriptionBuilder = new DescriptionBuilderImpl(this);
        if (this.comTaskExecution != null) {
            descriptionBuilder.addProperty("deviceID").append(comTaskExecution.getDevice().getId());
            if (this.comTaskExecution.usesSharedSchedule()) {
                ComTaskExecution scheduledComTaskExecution = this.comTaskExecution;
                descriptionBuilder.addProperty("comSchedule").append(scheduledComTaskExecution.getComSchedule().get().getName());
            }
            else if (this.comTaskExecution.isScheduledManually()) {
                // Must be ComTaskExecution
                ComTaskExecution manuallyScheduledComTaskExecution = this.comTaskExecution;
                descriptionBuilder.addProperty("comTask").append(manuallyScheduledComTaskExecution.getComTask().getName());
            }
            else if (this.comTaskExecution.isFirmware()) {
                ComTaskExecution firmwareComTaskExecution = this.comTaskExecution;
                descriptionBuilder.addProperty("comTask").append(firmwareComTaskExecution.getComTask().getName());
            }
        }

        if (isJournalingLevelEnabled(serverLogLevel, ComServer.LogLevel.DEBUG)) {
            StringBuilder builder = descriptionBuilder.addProperty("commands");
            Iterator<DeviceCommand> commandIterator = this.getChildren().iterator();
            while (commandIterator.hasNext()) {
                DeviceCommand command = commandIterator.next();
                String messageDescription = command.toJournalMessageDescription(serverLogLevel);
                builder.append(messageDescription);
                if (commandIterator.hasNext()) {
                    builder.append(", ");
                }
            }
        }
        return descriptionBuilder.toString();
    }

    @Override
    public String getDescriptionTitle() {
        return "ComTask device command";
    }

}