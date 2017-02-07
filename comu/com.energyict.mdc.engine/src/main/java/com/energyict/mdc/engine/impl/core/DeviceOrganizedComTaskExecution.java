/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;

import java.util.ArrayList;
import java.util.List;

public class DeviceOrganizedComTaskExecution {

    private final Device device;
    private final List<ComTaskWithSecurityAndConnectionSteps> comTasksWithStepsAndSecurity = new ArrayList<>();

    public DeviceOrganizedComTaskExecution(Device device) {
        this.device = device;
    }

    public void addComTaskWithConnectionSteps(ComTaskExecution comTaskExecution, ComTaskExecutionConnectionSteps connectionSteps, DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        this.comTasksWithStepsAndSecurity.add(new ComTaskWithSecurityAndConnectionSteps(comTaskExecution, connectionSteps, deviceProtocolSecurityPropertySet));
    }

    public List<ComTaskWithSecurityAndConnectionSteps> getComTasksWithStepsAndSecurity() {
        return comTasksWithStepsAndSecurity;
    }

    public Device getDevice() {
        return device;
    }

    public List<ComTaskExecution> getComTaskExecutions(){
        List<ComTaskExecution> comTaskExecutions = new ArrayList<>(comTasksWithStepsAndSecurity.size());
        for (ComTaskWithSecurityAndConnectionSteps comTasksWithStep : comTasksWithStepsAndSecurity) {
            comTaskExecutions.add(comTasksWithStep.getComTaskExecution());
        }
        return comTaskExecutions;
    }

    final class ComTaskWithSecurityAndConnectionSteps{
        private final ComTaskExecution comTaskExecution;
        private final ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps;
        private final DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet;

        private ComTaskWithSecurityAndConnectionSteps(ComTaskExecution comTaskExecution, ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps, DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
            this.comTaskExecution = comTaskExecution;
            this.comTaskExecutionConnectionSteps = comTaskExecutionConnectionSteps;
            this.deviceProtocolSecurityPropertySet = deviceProtocolSecurityPropertySet;
        }

        public ComTaskExecution getComTaskExecution() {
            return comTaskExecution;
        }

        public ComTaskExecutionConnectionSteps getComTaskExecutionConnectionSteps() {
            return comTaskExecutionConnectionSteps;
        }

        public DeviceProtocolSecurityPropertySet getDeviceProtocolSecurityPropertySet() {
            return deviceProtocolSecurityPropertySet;
        }
    }

}