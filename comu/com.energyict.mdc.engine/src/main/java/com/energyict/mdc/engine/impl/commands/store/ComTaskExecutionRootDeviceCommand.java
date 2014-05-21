package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.model.ComServer;

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
    public void execute (final ComServerDAO comServerDAO) {
        executeAll(comServerDAO);
    }

    @Override
    public void executeDuringShutdown (final ComServerDAO comServerDAO) {
        executeAllDuringShutdown(comServerDAO);
    }

    @Override
    public void logExecutionWith (ExecutionLogger logger) {
        this.executionLogger = logger;
        broadCastExecutionLoggerIfAny();
    }

    private void broadCastExecutionLoggerIfAny () {
        if (this.executionLogger != null) {
            for (DeviceCommand deviceCommand : super.getChildren()) {
                deviceCommand.logExecutionWith(this.executionLogger);
            }
        }
    }

}