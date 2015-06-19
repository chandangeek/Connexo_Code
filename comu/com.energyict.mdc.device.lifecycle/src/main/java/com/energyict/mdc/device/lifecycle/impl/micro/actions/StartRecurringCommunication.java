package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroAction;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 19/06/2015
 * Time: 14:11
 */
public class StartRecurringCommunication implements ServerMicroAction {

    @Override
    public void execute(Device device, List<ExecutableActionProperty> properties) {
        device.getConnectionTasks().forEach(connectionTask -> connectionTask.activate());
        device.getComTaskExecutions().forEach(ComTaskExecution::scheduleNow);
    }

}
