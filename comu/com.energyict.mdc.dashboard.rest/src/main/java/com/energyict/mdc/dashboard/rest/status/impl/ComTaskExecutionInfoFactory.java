package com.energyict.mdc.dashboard.rest.status.impl;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.inject.Provider;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.rest.IdWithNameInfo;
import com.energyict.mdc.device.configuration.rest.DeviceConfigurationIdInfo;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.rest.BaseComTaskExecutionInfoFactory;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.tasks.ComTask;

public class ComTaskExecutionInfoFactory extends BaseComTaskExecutionInfoFactory<ComTaskExecutionInfo>{

    private final Provider<ConnectionTaskInfoFactory> connectionTaskInfoFactory;

    @Inject
    public ComTaskExecutionInfoFactory(Thesaurus thesaurus, Provider<ConnectionTaskInfoFactory> connectionTaskInfoFactoryProvider) {
        super(thesaurus);
        this.connectionTaskInfoFactory = connectionTaskInfoFactoryProvider;
    }
    
    @Override
    protected Supplier<ComTaskExecutionInfo> getInfoSupplier() {
        return ComTaskExecutionInfo::new;
    }
    
    @Override
    protected void initExtraFields(ComTaskExecutionInfo info, ComTaskExecution comTaskExecution, Optional<ComTaskExecutionSession> comTaskExecutionSession) {
        info.comTasks = new ArrayList<>(comTaskExecution.getComTasks().size());
        for (ComTask comTask : comTaskExecution.getComTasks()) {
            info.comTasks.add(new IdWithNameInfo(comTask));
        }
        Device device = comTaskExecution.getDevice();
        info.device = new IdWithNameInfo(device.getmRID(), device.getName());
        info.deviceConfiguration = new DeviceConfigurationIdInfo(device.getDeviceConfiguration());
        info.deviceType = new IdWithNameInfo(device.getDeviceType());
        if(comTaskExecutionSession.isPresent()){
            info.sessionId = comTaskExecutionSession.get().getId();
        }
        info.alwaysExecuteOnInbound = comTaskExecution.isIgnoreNextExecutionSpecsForInbound();
        ConnectionTask<?, ?> connectionTask = comTaskExecution.getConnectionTask();
        if (connectionTask != null) {
            Optional<ComSession> comSessionOptional = comTaskExecutionSession.map(ComTaskExecutionSession::getComSession);
            info.connectionTask = connectionTaskInfoFactory.get().from(connectionTask, comSessionOptional);
        }
    }

}
