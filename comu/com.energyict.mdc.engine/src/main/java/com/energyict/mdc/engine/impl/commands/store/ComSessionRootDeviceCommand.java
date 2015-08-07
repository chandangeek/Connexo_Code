package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.util.time.StopWatch;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilderImpl;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.ExecutionContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Provides an implementation for the {@link CompositeDeviceCommand} interface
 * that will contains all the {@link DeviceCommand}s that are part of the same
 * ComSession.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-09-17 (11:52)
 */
public class ComSessionRootDeviceCommand extends CompositeDeviceCommandImpl {

    private List<DeviceCommand> finalCommands = new ArrayList<>();
    private CreateComSessionDeviceCommand createComSessionDeviceCommand;
    private PublishConnectionTaskEventDeviceCommand publishConnectionTaskEventDeviceCommand;

    public ComSessionRootDeviceCommand() {
        this(ComServer.LogLevel.INFO);
    }

    public ComSessionRootDeviceCommand(ComServer.LogLevel communicationLogLevel) {
        super(communicationLogLevel);
    }

    @Override
    public void add (CreateComSessionDeviceCommand command) {
        this.createComSessionDeviceCommand = command;
        if (this.publishConnectionTaskEventDeviceCommand != null) {
            this.publishConnectionTaskEventDeviceCommand.setCreateComSessionDeviceCommand(command);
        }
    }

    public CreateComSessionDeviceCommand getCreateComSessionDeviceCommand() {
        return createComSessionDeviceCommand;
    }

    @Override
    public void add (PublishConnectionTaskEventDeviceCommand command) {
        this.publishConnectionTaskEventDeviceCommand = command;
        if (this.createComSessionDeviceCommand != null) {
            this.publishConnectionTaskEventDeviceCommand.setCreateComSessionDeviceCommand(this.createComSessionDeviceCommand);
        }
    }

    @Override
    public void add(RescheduleExecutionDeviceCommand command) {
        this.finalCommands.add(command);
    }

    @Override
    public void add(UnlockScheduledJobDeviceCommand command) {
        this.finalCommands.add(command);
    }

    @Override
    public List<DeviceCommand> getChildren () {
        List<DeviceCommand> deviceCommands = super.getChildren();
        deviceCommands.addAll(this.finalCommands);
        if (this.createComSessionDeviceCommand != null) {
            deviceCommands.add(this.createComSessionDeviceCommand);
        }
        if (this.publishConnectionTaskEventDeviceCommand != null) {
            deviceCommands.add(this.publishConnectionTaskEventDeviceCommand);
        }
        return deviceCommands;
    }

    @Override
    public void execute (final ComServerDAO comServerDAO) {
        this.startStopWatch();
        this.broadCastFailureLoggerIfAny();
        executeAll(comServerDAO);
    }

    private void startStopWatch() {
        if (this.createComSessionDeviceCommand != null) {
            this.createComSessionDeviceCommand.setStopWatch(new StopWatch());
        }
    }

    private void broadCastFailureLoggerIfAny () {
        if (this.createComSessionDeviceCommand != null) {
            for (DeviceCommand deviceCommand : super.getChildren()) {
                deviceCommand.logExecutionWith(this.createComSessionDeviceCommand);
            }
        }
    }

    @Override
    public void executeDuringShutdown (final ComServerDAO comServerDAO) {
        this.startStopWatch();
        this.broadCastFailureLoggerIfAny();
        comServerDAO.executeTransaction(() -> {
            executeAllDuringShutdown(comServerDAO);
            return null;
        });
    }

    @Override
    public void logExecutionWith (ExecutionLogger logger) {
        // Logging is responsibility of nested commands
    }

    @Override
    public String toJournalMessageDescription(ComServer.LogLevel serverLogLevel) {
        DescriptionBuilder builder = new DescriptionBuilderImpl(this);
        if (this.createComSessionDeviceCommand != null) {
            builder.addProperty("connectionTaskID").append(this.createComSessionDeviceCommand.getConnectionTask().getId());
        }
        else {
            builder.addProperty("connectionTaskID").append("");
        }

        if (this.isJournalingLevelEnabled(serverLogLevel, ComServer.LogLevel.INFO)) {
            StringBuilder stringBuilder = builder.addProperty("commands");

            List<DeviceCommand> deviceCommands = super.getChildren();
            if (this.createComSessionDeviceCommand != null) {
                deviceCommands.add(this.createComSessionDeviceCommand);
            }

            Iterator<DeviceCommand> commandIterator = deviceCommands.iterator();
            while (commandIterator.hasNext()) {
                DeviceCommand command = commandIterator.next();
                String messageDescription = command.toJournalMessageDescription(serverLogLevel);
                stringBuilder.append(messageDescription);
                if (commandIterator.hasNext()) {
                    stringBuilder.append(", ");
                }
            }
        }
        return builder.toString();
    }

    @Override
    public String getDescriptionTitle() {
        return "ComSession device command";
    }

}