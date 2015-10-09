package com.energyict.mdc.multisense.api.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;

/**
 * Created by bvn on 10/6/15.
 */
public enum ComTaskExecutionType {
    SharedSchedule {
        @Override
        public ComTaskExecution createComTaskExecution(ComTaskExecutionInfoFactory factory, ComTaskExecutionInfo comTaskExecutionInfo, Device device) {
            return factory.createSharedScheduledComtaskExecution(comTaskExecutionInfo, device);
        }
    },
    ManualSchedule {
        @Override
        public ComTaskExecution createComTaskExecution(ComTaskExecutionInfoFactory factory, ComTaskExecutionInfo comTaskExecutionInfo, Device device) {
            return factory.createManuallyScheduledComTaskExecution(comTaskExecutionInfo, device);
        }
    },
    AdHoc {
        @Override
        public ComTaskExecution createComTaskExecution(ComTaskExecutionInfoFactory factory, ComTaskExecutionInfo comTaskExecutionInfo, Device device) {
            return factory.createAdHocComtaskExecution(comTaskExecutionInfo, device);
        }
    };

    public abstract ComTaskExecution createComTaskExecution(ComTaskExecutionInfoFactory factory, ComTaskExecutionInfo comTaskExecutionInfo, Device device);
}
