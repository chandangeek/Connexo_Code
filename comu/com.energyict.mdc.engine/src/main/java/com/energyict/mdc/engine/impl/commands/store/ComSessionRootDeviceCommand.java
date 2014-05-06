package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.common.BusinessException;
import com.energyict.comserver.core.ComServerDAO;
import com.energyict.mdc.common.Transaction;
import com.energyict.mdc.ManagerFactory;
import com.energyict.mdc.engine.model.ComServer;

import java.sql.SQLException;
import java.util.List;

/**
 * Provides an implementation for the {@link CompositeDeviceCommand} interface
 * that will contains all the {@link DeviceCommand}s that are part of the same
 * {@link com.energyict.mdc.journal.ComSession}.
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
        this.executeTransaction(new Transaction<Void>() {
            @Override
            public Void doExecute () {
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
        this.executeTransaction(new Transaction<Void>() {
            @Override
            public Void doExecute () {
                executeAllDuringShutdown(comServerDAO);
                return null;
            }
        });
    }

    private void executeTransaction (final Transaction<Void> transaction) {
        try {
            ManagerFactory.getCurrent().getMdwInterface().execute(transaction);
        }
        catch (BusinessException | SQLException e) {
            /* All of the commands have already wrapped BusinessException in ApplicationException
             * so it is safe to ignore this one here.
             * However, since we are compiling with AspectJ's Xlint option set to error level
             * to trap advice that does not apply,
             * it will not be happy until we actually code something here. */
            throw new ApplicationException(e);
        }
    }

    @Override
    public void logExecutionWith (ExecutionLogger logger) {
        // Logging is responsibility of nested commands
    }

}