package com.energyict.mdc.engine.impl.commands.store.core;

import com.energyict.comserver.core.ComTaskExecutionConnectionSteps;
import com.energyict.comserver.core.CommandCreator;
import com.energyict.comserver.core.CommandFactory;
import com.energyict.mdc.commands.CommandRoot;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.ComChannelPlaceHolder;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.tasks.ProtocolTask;

import java.util.List;

/**
 * A {@link com.energyict.comserver.core.CommandCreator} which will be used for creating commands for a {@link DeviceProtocol}
 * <p/>
 * Copyrights EnergyICT
 * Date: 7/08/12
 * Time: 16:41
 */
public class LegacyMeterProtocolCommandCreator implements CommandCreator {

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

        if (comTaskExecutionConnectionStep.isLogOnRequired() || comTaskExecutionConnectionStep.isDaisyChainedLogOnRequired()) {
            CommandFactory.createSetDeviceCacheCommand(root, comTaskExecution, offlineDevice);
            CommandFactory.createLegacyInitLoggerCommand(root, comTaskExecution);
            CommandFactory.createAddProperties(root,comTaskExecution, offlineDevice.getAllProperties(), protocolDialectProperties, deviceProtocolSecurityPropertySet);
            CommandFactory.createDeviceProtocolInitialization(root, comTaskExecution, offlineDevice, comChannel);
            CommandFactory.createHandHeldUnitEnabler(root, comTaskExecution, comChannel);
            CommandFactory.createLogOnCommand(root, comTaskExecution);
        }

        CommandFactory.createLegacyCommandsFromTask(root, comTaskExecution, protocolTasks);    // Create a set of legacy commands

        if (comTaskExecutionConnectionStep.isLogOffRequired() || comTaskExecutionConnectionStep.isDaisyChainedLogOffRequired()) {
            CommandFactory.createLogOffCommand(root, comTaskExecution);
            CommandFactory.createDeviceProtocolTerminate(root, comTaskExecution);
            CommandFactory.createUpdateDeviceCacheCommand(root, comTaskExecution, offlineDevice);
        }
    }
}
