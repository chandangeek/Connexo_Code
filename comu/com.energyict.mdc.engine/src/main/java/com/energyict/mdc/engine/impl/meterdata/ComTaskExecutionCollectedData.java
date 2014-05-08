package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.impl.commands.store.ComTaskExecutionRootDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.device.data.DataCollectionConfiguration;
import com.energyict.mdc.engine.model.ComServer;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides an implemenation for the {@link com.energyict.mdc.protocol.api.device.data.CollectedData} interface
 * that contains all the CollectedData that relates to the same {@link ComTaskExecution}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-09-17 (12:09)
 */
public class ComTaskExecutionCollectedData extends CompositeCollectedData<ServerCollectedData> {

    private ComTaskExecution comTaskExecution;
    private ComServer.LogLevel communicationLogLevel;

    public ComTaskExecutionCollectedData (ComTaskExecution comTaskExecution, List<ServerCollectedData> relatedCollectedData) {
        this(comTaskExecution, relatedCollectedData, ComServer.LogLevel.INFO);
    }

    public ComTaskExecutionCollectedData (ComTaskExecution comTaskExecution, List<ServerCollectedData> relatedCollectedData, ComServer.LogLevel communicationLogLevel) {
        super();
        this.comTaskExecution = comTaskExecution;
        this.communicationLogLevel = communicationLogLevel;
        for (ServerCollectedData collectedData : relatedCollectedData) {
            this.add(collectedData);
        }
    }

    @Override
    public void postProcess (ConnectionTask connectionTask) {
        for (ServerCollectedData collectedData : this.getElements()) {
            collectedData.postProcess(connectionTask);
        }
    }

    @Override
    public boolean isConfiguredIn (DataCollectionConfiguration configuration) {
        return this.comTaskExecution.getComTask().equals(configuration);
    }

    @Override
    public DeviceCommand toDeviceCommand(IssueService issueService) {
        List<DeviceCommand> nestedCommands = new ArrayList<>(this.getElements().size());
        for (ServerCollectedData collectedData : this.getElements()) {
            nestedCommands.add(collectedData.toDeviceCommand(issueService));
        }
        return new ComTaskExecutionRootDeviceCommand(this.comTaskExecution, this.communicationLogLevel, nestedCommands);
    }

}