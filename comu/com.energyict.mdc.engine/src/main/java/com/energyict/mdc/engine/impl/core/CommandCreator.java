package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.collect.ComCommand;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.core.inbound.ComChannelPlaceHolder;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.tasks.ProtocolTask;

import java.util.List;

/**
 * Responsible for creating ComCommands
 * <p>
 * Copyrights EnergyICT
 * Date: 7/08/12
 * Time: 16:26
 */
public interface CommandCreator {

    /**
     * Create proper comCommands for the given {@link CommandRoot root}, in the correct order of execution
     *
     * @param groupedDeviceCommand              The owner of the newly created {@link ComCommand comCommands}
     * @param protocolDialectProperties         The used ProtocolDialectProperties
     * @param comChannel                        The communication channel which will be used to communicate with the device
     * @param protocolTasks                     the list of task to execute
     * @param deviceProtocolSecurityPropertySet The securityProperties which should be used for the communication session
     * @param comTaskExecutionConnectionStep    the connectionExecutionSteps required to perform the tasks
     * @param comTaskExecution                  The ComTaskExecution which requires the ComCommands to be executed
     * @param issueService
     */
    public void createCommands(
            GroupedDeviceCommand groupedDeviceCommand,
            TypedProperties protocolDialectProperties,
            ComChannelPlaceHolder comChannel,
            List<ProtocolTask> protocolTasks,
            DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet,
            ComTaskExecutionConnectionSteps comTaskExecutionConnectionStep,
            ComTaskExecution comTaskExecution,
            IssueService issueService);

}