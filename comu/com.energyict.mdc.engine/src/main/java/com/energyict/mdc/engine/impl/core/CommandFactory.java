package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.store.access.DaisyChainedLogOffCommand;
import com.energyict.mdc.engine.impl.commands.store.access.DaisyChainedLogOnCommand;
import com.energyict.mdc.engine.impl.commands.store.access.LogOffCommand;
import com.energyict.mdc.engine.impl.commands.store.access.LogOnCommand;
import com.energyict.mdc.engine.impl.commands.store.common.AddPropertiesCommand;
import com.energyict.mdc.engine.impl.commands.store.common.DeviceProtocolInitializeCommand;
import com.energyict.mdc.engine.impl.commands.store.common.DeviceProtocolSetCacheCommand;
import com.energyict.mdc.engine.impl.commands.store.common.DeviceProtocolTerminateCommand;
import com.energyict.mdc.engine.impl.commands.store.common.DeviceProtocolUpdateCacheCommand;
import com.energyict.mdc.engine.impl.commands.store.core.DeviceProtocolCommandCreator;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.core.LegacyMeterProtocolCommandCreator;
import com.energyict.mdc.engine.impl.commands.store.core.LegacySmartMeterProtocolCommandCreator;
import com.energyict.mdc.engine.impl.commands.store.legacy.HandHeldUnitEnablerCommand;
import com.energyict.mdc.engine.impl.commands.store.legacy.InitializeLoggerCommand;
import com.energyict.mdc.engine.impl.core.inbound.ComChannelPlaceHolder;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.pluggable.MeterProtocolAdapter;
import com.energyict.mdc.protocol.pluggable.SmartMeterProtocolAdapter;
import com.energyict.mdc.tasks.ProtocolTask;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;

import java.util.List;

/**
 * Provides proper functionality to create ComCommands based on relevant information
 * <p>
 * Copyrights EnergyICT
 * Date: 27/06/12
 * Time: 16:25
 */
public final class CommandFactory {

    /**
     * Hide utility class constructor.
     */
    private CommandFactory() {
        super();
    }

    /**
     * Create commands based on the {@link ProtocolTask}s
     * in the {@link com.energyict.mdc.tasks.ComTask}.
     *
     * @param groupedDeviceCommand the root to add the created commands to
     * @param protocolTasks        the used ProtocolTasks for modeling the commands
     */
    public static void createCommandsFromTask(final GroupedDeviceCommand groupedDeviceCommand, ComTaskExecution comTaskExecution, final List<ProtocolTask> protocolTasks) {
        for (ProtocolTask protocolTask : protocolTasks) {
            ComCommandTypes comCommandType = ComCommandTypes.forProtocolTask(protocolTask.getClass());
            comCommandType.createCommandsFromTask(groupedDeviceCommand, protocolTask, comTaskExecution);
        }
    }

    /**
     * Create commands - specif for usage with legacy protocols ({@link MeterProtocol}) - based on the {@link ProtocolTask ProtoclTasks}
     * in the {@link com.energyict.mdc.tasks.ComTask}.
     *
     * @param groupedDeviceCommand the root to add the created commands to
     * @param protocolTasks        the used ProtocolTasks for modeling the commands
     */
    public static void createLegacyCommandsFromTask(final GroupedDeviceCommand groupedDeviceCommand, ComTaskExecution comTaskExecution, final List<ProtocolTask> protocolTasks) {
        for (ProtocolTask protocolTask : protocolTasks) {
            createLegacyCommandsFromProtocolTask(groupedDeviceCommand, comTaskExecution, protocolTasks, protocolTask);
        }
    }

    private static void createLegacyCommandsFromProtocolTask(GroupedDeviceCommand groupedDeviceCommand, ComTaskExecution comTaskExecution, List<ProtocolTask> protocolTasks, ProtocolTask protocolTask) {
        ComCommandTypes comCommandType = ComCommandTypes.forProtocolTask(protocolTask.getClass());
        comCommandType.createLegacyCommandsFromProtocolTask(groupedDeviceCommand, protocolTasks, protocolTask, comTaskExecution);
    }

    /**
     * Create a simple AddPropertiesCommand
     *
     * @param groupedDeviceCommand              the root to add the created commands to
     * @param deviceProperties                  the properties which are defined on the Device level (Device)
     * @param protocolDialectProperties         the properties defined on the ComTask level
     * @param deviceProtocolSecurityPropertySet the securityProperty set for this command
     */
    public static void createAddProperties(GroupedDeviceCommand groupedDeviceCommand, ComTaskExecution comTaskExecution, TypedProperties deviceProperties, TypedProperties protocolDialectProperties, DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        groupedDeviceCommand.addCommand(new AddPropertiesCommand(groupedDeviceCommand, deviceProperties, protocolDialectProperties, deviceProtocolSecurityPropertySet), comTaskExecution);
    }

    /**
     * Create a simple DeviceProtocolInitializeCommand
     *
     * @param groupedDeviceCommand  The root to add the created commands to
     * @param offlineDevice         The offlineDevice used for modeling the command
     * @param comChannelPlaceHolder The place holder for the ComChannel where all read/write actions are going to be performed
     */
    public static void createDeviceProtocolInitialization(GroupedDeviceCommand groupedDeviceCommand, ComTaskExecution comTaskExecution, OfflineDevice offlineDevice, ComChannelPlaceHolder comChannelPlaceHolder) {
        groupedDeviceCommand.addCommand(new DeviceProtocolInitializeCommand(groupedDeviceCommand, comChannelPlaceHolder), comTaskExecution);
    }

    /**
     * Create a simple DeviceProtocolTerminateCommand
     *
     * @param groupedDeviceCommand the root to add the created commands to
     */
    public static void createDeviceProtocolTerminate(GroupedDeviceCommand groupedDeviceCommand, ComTaskExecution comTaskExecution) {
        groupedDeviceCommand.addCommand(new DeviceProtocolTerminateCommand(groupedDeviceCommand), comTaskExecution);
    }

    /**
     * Create a simple LogOnCommand.
     *
     * @param groupedDeviceCommand the root to add the created commands to
     */
    public static void createLogOnCommand(GroupedDeviceCommand groupedDeviceCommand, ComTaskExecution comTaskExecution) {
        groupedDeviceCommand.addCommand(new LogOnCommand(groupedDeviceCommand), comTaskExecution);
    }

    /**
     * Create a simple {@link DaisyChainedLogOnCommand}
     *
     * @param groupedDeviceCommand the root to add the created commands to
     */
    public static void createDaisyChainedLogOnCommand(GroupedDeviceCommand groupedDeviceCommand, ComTaskExecution comTaskExecution) {
        groupedDeviceCommand.addCommand(new DaisyChainedLogOnCommand(groupedDeviceCommand), comTaskExecution);
    }

    /**
     * Create a simple LogOffCommand.
     *
     * @param groupedDeviceCommand the root to add the created commands to
     */
    public static void createLogOffCommand(GroupedDeviceCommand groupedDeviceCommand, ComTaskExecution comTaskExecution) {
        groupedDeviceCommand.addCommand(new LogOffCommand(groupedDeviceCommand), comTaskExecution);
    }

    /**
     * Create a simple {@link DaisyChainedLogOffCommand}
     *
     * @param groupedDeviceCommand the root to add the created commands to
     */
    public static void createDaisyChainedLogOffCommand(GroupedDeviceCommand groupedDeviceCommand, ComTaskExecution comTaskExecution) {
        groupedDeviceCommand.addCommand(new DaisyChainedLogOffCommand(groupedDeviceCommand), comTaskExecution);
    }

    /**
     * Create a simple {@link InitializeLoggerCommand}
     * <br/>
     * Note: this command should only be used in adapter classes
     *
     * @param groupedDeviceCommand the root to add the created commands to
     */
    public static void createLegacyInitLoggerCommand(GroupedDeviceCommand groupedDeviceCommand, ComTaskExecution comTaskExecution) {
        groupedDeviceCommand.addCommand(new InitializeLoggerCommand(groupedDeviceCommand), comTaskExecution);
    }

    /**
     * Returns the correct {@link CommandCreator} for the specific pluggable protocol 'type'
     *
     * @param pluggableClass the pluggableClass representing the protocol
     * @return the proper CommandCreator
     */
    static CommandCreator commandCreatorForPluggableClass(DeviceProtocolPluggableClass pluggableClass) {
        DeviceProtocol deviceProtocol = pluggableClass.getDeviceProtocol();
        if (deviceProtocol instanceof MeterProtocolAdapter) {
            return new LegacyMeterProtocolCommandCreator();
        } else if (deviceProtocol instanceof SmartMeterProtocolAdapter) {
            return new LegacySmartMeterProtocolCommandCreator();
        } else {
            return new DeviceProtocolCommandCreator();
        }
    }

    /**
     * Create a simple {@link HandHeldUnitEnablerCommand}
     *
     * @param groupedDeviceCommand  The root to add the created commands to
     * @param comChannelPlaceHolder The communication channel which will be used to talk to the device
     */
    public static void createHandHeldUnitEnabler(GroupedDeviceCommand groupedDeviceCommand, ComTaskExecution comTaskExecution, ComChannelPlaceHolder comChannelPlaceHolder) {
        groupedDeviceCommand.addCommand(new HandHeldUnitEnablerCommand(groupedDeviceCommand, comChannelPlaceHolder), comTaskExecution);
    }

    /**
     * Create a simple {@link DeviceProtocolSetCacheCommand}
     *
     * @param groupedDeviceCommand the root to add the created commands to
     * @param offlineDevice        the offlineDevice used for modeling the command
     */
    public static void createSetDeviceCacheCommand(GroupedDeviceCommand groupedDeviceCommand, ComTaskExecution comTaskExecution, OfflineDevice offlineDevice) {
        groupedDeviceCommand.addCommand(new DeviceProtocolSetCacheCommand(groupedDeviceCommand), comTaskExecution);
    }

    /**
     * Create a simple {@link DeviceProtocolUpdateCacheCommand}
     *
     * @param groupedDeviceCommand the root to add the created commands to
     * @param offlineDevice        the offlineDevice used for modeling the command
     */
    public static void createUpdateDeviceCacheCommand(GroupedDeviceCommand groupedDeviceCommand, ComTaskExecution comTaskExecution, OfflineDevice offlineDevice) {
        groupedDeviceCommand.addCommand(new DeviceProtocolUpdateCacheCommand(groupedDeviceCommand), comTaskExecution);
    }

}