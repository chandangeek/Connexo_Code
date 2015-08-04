package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.impl.commands.store.ComTaskExecutionRootDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.MeterDataStoreCommand;
import com.energyict.mdc.protocol.api.device.data.DataCollectionConfiguration;
import com.energyict.mdc.tasks.ComTask;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link com.energyict.mdc.protocol.api.device.data.CollectedData} interface
 * that contains all the CollectedData that relates to the same {@link ComTaskExecution}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-09-17 (12:09)
 */
public class ComTaskExecutionCollectedData extends CompositeCollectedData<ServerCollectedData> {

    private final boolean exposeStoringException;
    private ComTaskExecution comTaskExecution;
    private ComServer.LogLevel communicationLogLevel;

    public ComTaskExecutionCollectedData(ComTaskExecution comTaskExecution, List<ServerCollectedData> relatedCollectedData) {
        this(comTaskExecution, relatedCollectedData, ComServer.LogLevel.INFO, false);
    }

    public ComTaskExecutionCollectedData(ComTaskExecution comTaskExecution, List<ServerCollectedData> relatedCollectedData, ComServer.LogLevel communicationLogLevel, boolean exposeStoringException) {
        super();
        this.comTaskExecution = comTaskExecution;
        this.communicationLogLevel = communicationLogLevel;
        this.exposeStoringException = exposeStoringException;
        relatedCollectedData.forEach(this::add);
    }

    @Override
    public void postProcess (ConnectionTask connectionTask) {
        for (ServerCollectedData collectedData : this.getElements()) {
            collectedData.postProcess(connectionTask);
        }
    }

    @Override
    public boolean isConfiguredIn(DataCollectionConfiguration configuration) {
        for (ComTask comTask : this.comTaskExecution.getComTasks()) {
            if (comTask.equals(configuration)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public DeviceCommand toDeviceCommand(MeterDataStoreCommand meterDataStoreCommand, DeviceCommand.ServiceProvider serviceProvider) {
        List<DeviceCommand> nestedCommands =
                this.getElements()
                        .stream()
                        .map(collectedData -> collectedData.toDeviceCommand(meterDataStoreCommand, serviceProvider))
                        .collect(Collectors.toList());
        return new ComTaskExecutionRootDeviceCommand(this.comTaskExecution, this.communicationLogLevel, nestedCommands, exposeStoringException, serviceProvider);
    }

}