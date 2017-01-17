package com.energyict.mdc.engine.impl.commands.store.core;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.core.ComTaskExecutionConnectionSteps;
import com.energyict.mdc.engine.impl.core.CommandCreator;
import com.energyict.mdc.engine.impl.core.CommandFactory;
import com.energyict.mdc.engine.impl.core.inbound.ComChannelPlaceHolder;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.tasks.ProtocolTask;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;

import java.util.List;

/**
 * A {@link CommandCreator} which will be used for creating commands for a {@link DeviceProtocol}
 * <p>
 * Copyrights EnergyICT
 * Date: 7/08/12
 * Time: 16:41
 */
public class LegacySmartMeterProtocolCommandCreator implements CommandCreator {

    @Override
    public void createCommands(
            GroupedDeviceCommand groupedDeviceCommand,
            TypedProperties protocolDialectProperties,
            ComChannelPlaceHolder comChannel,
            List<ProtocolTask> protocolTasks,
            DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet,
            ComTaskExecutionConnectionSteps comTaskExecutionConnectionStep,
            ComTaskExecution comTaskExecution, IssueService issueService) {

        if (comTaskExecutionConnectionStep.isLogOnRequired() || comTaskExecutionConnectionStep.isDaisyChainedLogOnRequired()) {
            CommandFactory.createSetDeviceCacheCommand(groupedDeviceCommand, comTaskExecution, groupedDeviceCommand.getOfflineDevice());
            CommandFactory.createLegacyInitLoggerCommand(groupedDeviceCommand, comTaskExecution);
            CommandFactory.createAddProperties(groupedDeviceCommand, comTaskExecution, groupedDeviceCommand.getOfflineDevice().getAllProperties(), protocolDialectProperties, deviceProtocolSecurityPropertySet);
            CommandFactory.createDeviceProtocolInitialization(groupedDeviceCommand, comTaskExecution, groupedDeviceCommand.getOfflineDevice(), comChannel);
            CommandFactory.createHandHeldUnitEnabler(groupedDeviceCommand, comTaskExecution, comChannel);
            if (comTaskExecutionConnectionStep.isLogOnRequired()) {
                CommandFactory.createLogOnCommand(groupedDeviceCommand, comTaskExecution);
            } else {
                CommandFactory.createDaisyChainedLogOnCommand(groupedDeviceCommand, comTaskExecution);
            }
        }

        CommandFactory.createCommandsFromTask(groupedDeviceCommand, comTaskExecution, protocolTasks);

        if (comTaskExecutionConnectionStep.isLogOffRequired() || comTaskExecutionConnectionStep.isDaisyChainedLogOffRequired()) {
            if (comTaskExecutionConnectionStep.isLogOffRequired()) {
                CommandFactory.createLogOffCommand(groupedDeviceCommand, comTaskExecution);
            } else {
                CommandFactory.createDaisyChainedLogOffCommand(groupedDeviceCommand, comTaskExecution);
            }
            CommandFactory.createDeviceProtocolTerminate(groupedDeviceCommand, comTaskExecution);
            CommandFactory.createUpdateDeviceCacheCommand(groupedDeviceCommand, comTaskExecution, groupedDeviceCommand.getOfflineDevice());
        }
    }
}