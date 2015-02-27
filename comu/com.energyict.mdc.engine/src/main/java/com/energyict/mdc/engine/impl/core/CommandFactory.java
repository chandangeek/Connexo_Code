package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandType;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.access.DaisyChainedLogOffCommand;
import com.energyict.mdc.engine.impl.commands.store.access.DaisyChainedLogOnCommand;
import com.energyict.mdc.engine.impl.commands.store.access.LogOffCommand;
import com.energyict.mdc.engine.impl.commands.store.access.LogOnCommand;
import com.energyict.mdc.engine.impl.commands.store.common.AddPropertiesCommand;
import com.energyict.mdc.engine.impl.commands.store.common.DeviceProtocolInitializeCommand;
import com.energyict.mdc.engine.impl.commands.store.common.DeviceProtocolSetCacheCommand;
import com.energyict.mdc.engine.impl.commands.store.common.DeviceProtocolTerminateCommand;
import com.energyict.mdc.engine.impl.commands.store.common.DeviceProtocolUpdateCacheCommand;
import com.energyict.mdc.engine.impl.commands.store.core.CommandRootImpl;
import com.energyict.mdc.engine.impl.commands.store.core.DeviceProtocolCommandCreator;
import com.energyict.mdc.engine.impl.commands.store.core.LegacyMeterProtocolCommandCreator;
import com.energyict.mdc.engine.impl.commands.store.core.LegacySmartMeterProtocolCommandCreator;
import com.energyict.mdc.engine.impl.commands.store.legacy.HandHeldUnitEnablerCommand;
import com.energyict.mdc.engine.impl.commands.store.legacy.InitializeLoggerCommand;
import com.energyict.mdc.engine.impl.core.inbound.ComChannelPlaceHolder;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.pluggable.MeterProtocolAdapter;
import com.energyict.mdc.protocol.pluggable.SmartMeterProtocolAdapter;
import com.energyict.mdc.tasks.ProtocolTask;

import java.util.List;

/**
 * Provides proper functionality to create ComCommands based on relevant information
 * <p/>
 * Copyrights EnergyICT
 * Date: 27/06/12
 * Time: 16:25
 */
public final class CommandFactory {

    /**
     * Create commands based on the {@link ProtocolTask}s
     * in the {@link com.energyict.mdc.tasks.ComTask}.
     *
     * @param commandRoot      the root to add the created commands to
     * @param protocolTasks the used ProtocolTasks for modeling the commands
     */
    public static void createCommandsFromTask(final CommandRoot commandRoot, ComTaskExecution comTaskExecution, final List<? extends ProtocolTask> protocolTasks) {
        for (ProtocolTask protocolTask : protocolTasks) {
            ComCommandTypes.forProtocolTask(protocolTask).createCommandsFromTask(commandRoot, protocolTask, comTaskExecution);
        }
    }

    /**
     * Create commands - specif for usage with legacy protocols ({@link MeterProtocol}) - based on the {@link ProtocolTask ProtoclTasks}
     * in the {@link com.energyict.mdc.tasks.ComTask}.
     *
     * @param commandRoot      the root to add the created commands to
     * @param protocolTasks the used ProtocolTasks for modeling the commands
     */
    public static void createLegacyCommandsFromTask(final CommandRoot commandRoot, ComTaskExecution comTaskExecution, final List<? extends ProtocolTask> protocolTasks) {
        CommandRootImpl root = (CommandRootImpl) commandRoot;
        for (ProtocolTask protocolTask : protocolTasks) {
            createLegacyCommandsFromProtocolTask(root, comTaskExecution, protocolTasks, protocolTask);
        }
    }

    private static void createLegacyCommandsFromProtocolTask (CommandRoot root, ComTaskExecution comTaskExecution, List<? extends ProtocolTask> protocolTasks, ProtocolTask protocolTask) {
        ComCommandType comCommandType = ComCommandTypes.forProtocolTask(protocolTask);
        comCommandType.createLegacyCommandsFromProtocolTask(root, protocolTasks, protocolTask, comTaskExecution);
    }

    /**
     * Create a simple AddPropertiesCommand
     *
     * @param root                      the root to add the created commands to
     * @param deviceProperties          the properties which are defined on the Device level (Device)
     * @param protocolDialectProperties the properties defined on the ComTask level
     * @param deviceProtocolSecurityPropertySet
     *                                  the securityProperty set for this command
     */
    public static void createAddProperties(CommandRoot root, ComTaskExecution comTaskExecution, TypedProperties deviceProperties, TypedProperties protocolDialectProperties, DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        root.addUniqueCommand(new AddPropertiesCommand(root, deviceProperties, protocolDialectProperties, deviceProtocolSecurityPropertySet), comTaskExecution);
    }

    /**
     * Create a simple DeviceProtocolInitializeCommand
     *
     * @param root The root to add the created commands to
     * @param offlineDevice The offlineDevice used for modeling the command
     * @param comChannelPlaceHolder The place holder for the ComChannel where all read/write actions are going to be performed
     */
    public static void createDeviceProtocolInitialization(CommandRoot root, ComTaskExecution comTaskExecution, OfflineDevice offlineDevice, ComChannelPlaceHolder comChannelPlaceHolder) {
        root.addUniqueCommand(new DeviceProtocolInitializeCommand(root, offlineDevice, comChannelPlaceHolder), comTaskExecution);
    }

    /**
     * Create a simple DeviceProtocolTerminateCommand
     *
     * @param root the root to add the created commands to
     */
    public static void createDeviceProtocolTerminate(CommandRoot root, ComTaskExecution comTaskExecution) {
        root.addUniqueCommand(new DeviceProtocolTerminateCommand(root), comTaskExecution);
    }

    /**
     * Create a simple LogOnCommand.
     *
     * @param root the root to add the created commands to
     */
    public static void createLogOnCommand(CommandRoot root, ComTaskExecution comTaskExecution) {
        root.addUniqueCommand(new LogOnCommand(root), comTaskExecution);
    }

    /**
     * Create a simple {@link DaisyChainedLogOnCommand}
     *
     * @param root the root to add the created commands to
     */
    public static void createDaisyChainedLogOnCommand(CommandRoot root, ComTaskExecution comTaskExecution) {
        root.addUniqueCommand(new DaisyChainedLogOnCommand(root), comTaskExecution);
    }

    /**
     * Create a simple LogOffCommand.
     *
     * @param root the root to add the created commands to
     */
    public static void createLogOffCommand(CommandRoot root, ComTaskExecution comTaskExecution) {
        root.addUniqueCommand(new LogOffCommand(root), comTaskExecution);
    }

    /**
     * Create a simple {@link DaisyChainedLogOffCommand}
     *
     * @param root the root to add the created commands to
     */
    public static void createDaisyChainedLogOffCommand(CommandRoot root, ComTaskExecution comTaskExecution) {
        root.addUniqueCommand(new DaisyChainedLogOffCommand(root), comTaskExecution);
    }

    /**
     * Create a simple {@link InitializeLoggerCommand}
     * <br/>
     * Note: this command should only be used in adapter classes
     *
     * @param root the root to add the created commands to
     */
    public static void createLegacyInitLoggerCommand(CommandRoot root, ComTaskExecution comTaskExecution) {
        root.addUniqueCommand(new InitializeLoggerCommand(root), comTaskExecution);
    }

    /**
     * Returns the correct {@link CommandCreator} for the specific pluggable protocol 'type'
     *
     * @param pluggableClass the pluggableClass representing the protocol
     * @return the proper CommandCreator
     */
    public static CommandCreator commandCreatorForPluggableClass(DeviceProtocolPluggableClass pluggableClass) {
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
     * @param root The root to add the created commands to
     * @param comChannelPlaceHolder The communication channel which will be used to talk to the device
     */
    public static void createHandHeldUnitEnabler(CommandRoot root, ComTaskExecution comTaskExecution, ComChannelPlaceHolder comChannelPlaceHolder) {
        root.addUniqueCommand(new HandHeldUnitEnablerCommand(root, comChannelPlaceHolder), comTaskExecution);
    }

    /**
     * Create a simple {@link DeviceProtocolSetCacheCommand}
     *
     * @param root       the root to add the created commands to
     * @param offlineDevice the offlineDevice used for modeling the command
     */
    public static void createSetDeviceCacheCommand(CommandRoot root, ComTaskExecution comTaskExecution, OfflineDevice offlineDevice) {
        root.addUniqueCommand(new DeviceProtocolSetCacheCommand(offlineDevice, root), comTaskExecution);
    }

    /**
     * Create a simple {@link DeviceProtocolUpdateCacheCommand}
     *
     * @param root       the root to add the created commands to
     * @param offlineDevice the offlineDevice used for modeling the command
     */
    public static void createUpdateDeviceCacheCommand(CommandRoot root, ComTaskExecution comTaskExecution, OfflineDevice offlineDevice) {
        root.addUniqueCommand(new DeviceProtocolUpdateCacheCommand(offlineDevice, root), comTaskExecution);
    }

    /**
     * Hide utility class constructor.
     */
    private CommandFactory() {
        super();
    }

}