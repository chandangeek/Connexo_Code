/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;

/**
 * Created by bvn on 10/6/15.
 */
public enum ComTaskExecutionType {
    //TODO: FIX THIS
    SharedSchedule {
        @Override
        public ComTaskExecution createComTaskExecution(ComTaskExecutionInfoFactory factory, ComTaskExecutionInfo comTaskExecutionInfo, Device device) {
            return factory.createSharedScheduledComtaskExecution(comTaskExecutionInfo, device);
        }

        @Override
        public ComTaskExecution updateComTaskExecution(ComTaskExecutionInfoFactory factory, ComTaskExecutionInfo comTaskExecutionInfo, ComTaskExecution comTaskExecution) {
            return factory.updateSharedScheduledComtaskExecution(comTaskExecutionInfo, comTaskExecution);
        }
    },
    ManualSchedule {
        @Override
        public ComTaskExecution createComTaskExecution(ComTaskExecutionInfoFactory factory, ComTaskExecutionInfo comTaskExecutionInfo, Device device) {
            return factory.createManuallyScheduledComTaskExecution(comTaskExecutionInfo, device);
        }

        @Override
        public ComTaskExecution updateComTaskExecution(ComTaskExecutionInfoFactory factory, ComTaskExecutionInfo comTaskExecutionInfo, ComTaskExecution comTaskExecution) {
            return factory.updateManuallyScheduledComTaskExecution(comTaskExecutionInfo, comTaskExecution);
        }
    },
    AdHoc {
        @Override
        public ComTaskExecution createComTaskExecution(ComTaskExecutionInfoFactory factory, ComTaskExecutionInfo comTaskExecutionInfo, Device device) {
            return factory.createAdHocComtaskExecution(comTaskExecutionInfo, device);
        }

        @Override
        public ComTaskExecution updateComTaskExecution(ComTaskExecutionInfoFactory factory, ComTaskExecutionInfo comTaskExecutionInfo, ComTaskExecution comTaskExecution) {
            return factory.updateAdHocComTaskExecution(comTaskExecutionInfo, comTaskExecution);
        }
    };

    public abstract ComTaskExecution createComTaskExecution(ComTaskExecutionInfoFactory factory, ComTaskExecutionInfo comTaskExecutionInfo, Device device);

    public abstract ComTaskExecution updateComTaskExecution(ComTaskExecutionInfoFactory factory, ComTaskExecutionInfo comTaskExecutionInfo, ComTaskExecution comTaskExecution);
}
