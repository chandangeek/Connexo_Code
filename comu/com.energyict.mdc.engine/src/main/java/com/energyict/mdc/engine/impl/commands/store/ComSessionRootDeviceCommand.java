package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.transaction.Transaction;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.model.ComServer;

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

    private CreateComSessionDeviceCommand createComSessionDeviceCommand;

    public ComSessionRootDeviceCommand () {
        this(ComServer.LogLevel.INFO);
    }

    public ComSessionRootDeviceCommand (ComServer.LogLevel communicationLogLevel) {
        super(communicationLogLevel);
    }

    @Override
    public void add (CreateComSessionDeviceCommand command) {
        this.createComSessionDeviceCommand = command;
    }

    @Override
    public List<DeviceCommand> getChildren () {
        List<DeviceCommand> deviceCommands = super.getChildren();
        if (this.createComSessionDeviceCommand != null) {
            deviceCommands.add(this.createComSessionDeviceCommand);
        }
        return deviceCommands;
    }

    @Override
    public void execute (final ComServerDAO comServerDAO) {
        this.broadCastFailureLoggerIfAny();
        comServerDAO.executeTransaction(new Transaction<Void>() {
            @Override
            public Void perform() {
                executeAll(comServerDAO);
                return null;
            }
        });
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
        this.broadCastFailureLoggerIfAny();
        comServerDAO.executeTransaction(new Transaction<Void>() {
            @Override
            public Void perform () {
                executeAllDuringShutdown(comServerDAO);
                return null;
            }
        });
    }

    @Override
    public void logExecutionWith (ExecutionLogger logger) {
        // Logging is responsibility of nested commands
    }

}