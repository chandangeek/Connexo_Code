package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.*;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;
import com.energyict.mdc.device.lifecycle.config.MicroAction;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroAction;

import com.energyict.mdc.tasks.ComTask;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link ServerMicroAction} interface
 * that will start the  communication with the device
 * by activating all connection schedule all communication tasks
 * to execute now.
 * @see {@link com.energyict.mdc.device.lifecycle.config.MicroAction#START_COMMUNICATION}
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-13 (14:32)
 */
public class StartCommunication extends TranslatableServerMicroAction {

    public StartCommunication(Thesaurus thesaurus) {
        super(thesaurus);
    }

    @Override
    public void execute(Device device, Instant effectiveTimestamp, List<ExecutableActionProperty> properties) {
        List<ComTaskExecution> comTaskExecutions = new ArrayList<>(device.getComTaskExecutions());
        // include the 'unused' enablements
        List<ComTaskEnablement> comTaskEnablements = device.getDeviceConfiguration().getComTaskEnablements();
        Set<Long> usedComtaskIds =
                comTaskExecutions
                        .stream()
                        .flatMap(each -> each.getComTasks().stream())
                        .map(ComTask::getId)
                        .collect(Collectors.toSet());
        boolean deviceNeedsToBeSaved = false;
        for(ComTaskEnablement comTaskEnablement : comTaskEnablements){
            if(!usedComtaskIds.contains(comTaskEnablement.getComTask().getId())){
                comTaskExecutions.add(device.newManuallyScheduledComTaskExecution(comTaskEnablement, null ).add()) ;
                deviceNeedsToBeSaved = true;
            }
        }
        if (deviceNeedsToBeSaved){
            device.save();
        }
        device.getConnectionTasks().forEach(ConnectionTask::activate);
        device.getComTaskExecutions().forEach(ComTaskExecution::scheduleNow);
    }

    @Override
    protected MicroAction getMicroAction() {
        return MicroAction.START_COMMUNICATION;
    }
}