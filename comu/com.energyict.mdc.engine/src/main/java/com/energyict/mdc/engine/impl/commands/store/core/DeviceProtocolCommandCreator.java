package com.energyict.mdc.engine.impl.commands.store.core;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.core.ComTaskExecutionConnectionSteps;
import com.energyict.mdc.engine.impl.core.CommandCreator;
import com.energyict.mdc.engine.impl.core.CommandFactory;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.tasks.ProtocolTask;

import java.util.List;

/**
 * A {@link CommandCreator} which will be used for creating commands for a {@link DeviceProtocol}
 * <p/>
 * Copyrights EnergyICT
 * Date: 7/08/12
 * Time: 16:41
 */
public class DeviceProtocolCommandCreator implements CommandCreator {

    @Override
    public void createCommands(
            CommandRoot root,
            TypedProperties protocolDialectProperties,
            ComChannelPlaceHolder comChannel,
            OfflineDevice offlineDevice,
            List<? extends ProtocolTask> protocolTasks,
            DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet,
            ComTaskExecutionConnectionSteps comTaskExecutionConnectionStep,
            ComTaskExecution comTaskExecution, IssueService issueService) {

        if (comTaskExecutionConnectionStep.isLogOnRequired()) {
            doSetupNewDevice(root, protocolDialectProperties, comChannel, offlineDevice, deviceProtocolSecurityPropertySet, comTaskExecution);
            CommandFactory.createLogOnCommand(root, comTaskExecution);
        } else if (comTaskExecutionConnectionStep.isDaisyChainedLogOnRequired()) {
            doSetupNewDevice(root, protocolDialectProperties, comChannel, offlineDevice, deviceProtocolSecurityPropertySet, comTaskExecution);
            CommandFactory.createDaisyChainedLogOnCommand(root, comTaskExecution);
        }

        CommandFactory.createCommandsFromTask(root, comTaskExecution, protocolTasks);

        if (comTaskExecutionConnectionStep.isDaisyChainedLogOffRequired()) {
            CommandFactory.createDaisyChainedLogOffCommand(root, comTaskExecution);
            doTearDown(root, offlineDevice, comTaskExecution);
        } else if (comTaskExecutionConnectionStep.isLogOffRequired()) {
            CommandFactory.createLogOffCommand(root, comTaskExecution);
            doTearDown(root, offlineDevice, comTaskExecution);
        }
    }

    private void doSetupNewDevice(CommandRoot root, TypedProperties protocolDialectProperties, ComChannelPlaceHolder comChannel, OfflineDevice offlineDevice, DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet, ComTaskExecution comTaskExecution) {
        CommandFactory.createSetDeviceCacheCommand(root, comTaskExecution, offlineDevice);
        CommandFactory.createAddProperties(root, comTaskExecution, offlineDevice.getAllProperties(), protocolDialectProperties, deviceProtocolSecurityPropertySet);
        CommandFactory.createDeviceProtocolInitialization(root,comTaskExecution, offlineDevice, comChannel);
    }

    private void doTearDown(CommandRoot root, OfflineDevice offlineDevice, ComTaskExecution comTaskExecution) {
        CommandFactory.createDeviceProtocolTerminate(root, comTaskExecution);
        CommandFactory.createUpdateDeviceCacheCommand(root, comTaskExecution, offlineDevice);
    }
}
