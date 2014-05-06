package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;

import java.util.ArrayList;
import java.util.List;

/**
 * Refers to a Device with his to-Be-Executed ComTask references
 * and the connectionSteps required for the ComTaskExecution,
 * including the required securityProperties.
 * <p/>
 * Copyrights EnergyICT
 * Date: 7/06/13
 * Time: 16:45
 */
public class DeviceOrganizedComTaskExecution {

    private final BaseDevice device;
    private final List<ComTaskWithSecurityAndConnectionSteps> comTasksWithStepsAndSecurity = new ArrayList<>();

    public DeviceOrganizedComTaskExecution(BaseDevice device) {
        this.device = device;
    }

    public void addComTaskWithConnectionSteps(final ComTaskExecution comTaskExecution, final ComTaskExecutionConnectionSteps connectionSteps, final DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        this.comTasksWithStepsAndSecurity.add(new ComTaskWithSecurityAndConnectionSteps(comTaskExecution, connectionSteps, deviceProtocolSecurityPropertySet));
    }

    public List<ComTaskWithSecurityAndConnectionSteps> getComTasksWithStepsAndSecurity() {
        return comTasksWithStepsAndSecurity;
    }

    public BaseDevice getDevice() {
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
