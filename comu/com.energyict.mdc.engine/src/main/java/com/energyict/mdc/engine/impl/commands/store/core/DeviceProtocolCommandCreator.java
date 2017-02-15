/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store.core;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.core.ComTaskExecutionConnectionSteps;
import com.energyict.mdc.engine.impl.core.CommandCreator;
import com.energyict.mdc.engine.impl.core.CommandFactory;
import com.energyict.mdc.engine.impl.core.inbound.ComChannelPlaceHolder;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.tasks.ProtocolTask;

import java.util.List;

public class DeviceProtocolCommandCreator implements CommandCreator {

    @Override
    public void createCommands(
            GroupedDeviceCommand groupedDeviceCommand,
            TypedProperties protocolDialectProperties,
            ComChannelPlaceHolder comChannel,
            List<ProtocolTask> protocolTasks,
            DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet,
            ComTaskExecutionConnectionSteps comTaskExecutionConnectionStep,
            ComTaskExecution comTaskExecution,
            IssueService issueService) {

        if (comTaskExecutionConnectionStep.isLogOnRequired()) {
            doSetupNewDevice(groupedDeviceCommand, protocolDialectProperties, comChannel, groupedDeviceCommand.getOfflineDevice(), deviceProtocolSecurityPropertySet, comTaskExecution);
            CommandFactory.createLogOnCommand(groupedDeviceCommand, comTaskExecution);
        } else if (comTaskExecutionConnectionStep.isDaisyChainedLogOnRequired()) {
            doSetupNewDevice(groupedDeviceCommand, protocolDialectProperties, comChannel, groupedDeviceCommand.getOfflineDevice(), deviceProtocolSecurityPropertySet, comTaskExecution);
            CommandFactory.createDaisyChainedLogOnCommand(groupedDeviceCommand, comTaskExecution);
        }

        CommandFactory.createCommandsFromTask(groupedDeviceCommand, comTaskExecution, protocolTasks);

        if (comTaskExecutionConnectionStep.isDaisyChainedLogOffRequired()) {
            CommandFactory.createDaisyChainedLogOffCommand(groupedDeviceCommand, comTaskExecution);
            doTearDown(groupedDeviceCommand, groupedDeviceCommand.getOfflineDevice(), comTaskExecution);
        } else if (comTaskExecutionConnectionStep.isLogOffRequired()) {
            CommandFactory.createLogOffCommand(groupedDeviceCommand, comTaskExecution);
            doTearDown(groupedDeviceCommand, groupedDeviceCommand.getOfflineDevice(), comTaskExecution);
        }
    }

    private void doSetupNewDevice(GroupedDeviceCommand groupedDeviceCommand, TypedProperties protocolDialectProperties, ComChannelPlaceHolder comChannel, OfflineDevice offlineDevice, DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet, ComTaskExecution comTaskExecution) {
        CommandFactory.createAddProperties(groupedDeviceCommand, comTaskExecution, offlineDevice.getAllProperties(), protocolDialectProperties, deviceProtocolSecurityPropertySet);
        CommandFactory.createSetDeviceCacheCommand(groupedDeviceCommand, comTaskExecution, offlineDevice);
        CommandFactory.createDeviceProtocolInitialization(groupedDeviceCommand,comTaskExecution, offlineDevice, comChannel);
    }

    private void doTearDown(GroupedDeviceCommand groupedDeviceCommand, OfflineDevice offlineDevice, ComTaskExecution comTaskExecution) {
        CommandFactory.createDeviceProtocolTerminate(groupedDeviceCommand, comTaskExecution);
        CommandFactory.createUpdateDeviceCacheCommand(groupedDeviceCommand, comTaskExecution, offlineDevice);
    }
}