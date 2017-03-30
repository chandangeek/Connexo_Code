/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.collect;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.tasks.BasicCheckTask;
import com.energyict.mdc.tasks.ClockTask;
import com.energyict.mdc.tasks.FirmwareManagementTask;
import com.energyict.mdc.tasks.LoadProfilesTask;
import com.energyict.mdc.tasks.LogBooksTask;
import com.energyict.mdc.tasks.MessagesTask;
import com.energyict.mdc.tasks.ProtocolTask;
import com.energyict.mdc.tasks.RegistersTask;
import com.energyict.mdc.tasks.StatusInformationTask;
import com.energyict.mdc.tasks.TopologyTask;

import java.util.List;

/**
 * Defines all the types of the {@link ComCommand ComCommands}.<br>
 * Optionally a type can hold a {@link ProtocolTask} which can model several other types.
 *
 * @author gna
 * @since 10/05/12 - 14:50
 */
public enum ComCommandTypes implements ComCommandType {

    UNKNOWN,
    ALREADY_EXECUTED,
    ROOT,
    COM_TASK_ROOT,
    GROUPED_DEVICE,

    DEVICE_PROTOCOL_INITIALIZE,
    DEVICE_PROTOCOL_TERMINATE,
    ADD_PROPERTIES_COMMAND,
    INIT_LOGGER_COMMAND,
    DEVICE_PROTOCOL_SET_CACHE_COMMAND,
    DEVICE_PROTOCOL_UPDATE_CACHE_COMMAND,
    LOGON,
    DAISY_CHAINED_LOGON,
    LOGOFF,
    DAISY_CHAINED_LOGOFF,

    HAND_HELD_UNIT_ENABLER,

    CLOCK_COMMAND(ClockTask.class) {
        @Override
        public void createLegacyCommandsFromProtocolTask(GroupedDeviceCommand groupedDeviceCommand, List<ProtocolTask> protocolTasks, ProtocolTask protocolTask, ComTaskExecution comTaskExecution) {
            groupedDeviceCommand.getClockCommand((ClockTask) protocolTask, groupedDeviceCommand, comTaskExecution);
        }

        @Override
        public void createCommandsFromTask(GroupedDeviceCommand groupedDeviceCommand, ProtocolTask protocolTask, ComTaskExecution comTaskExecution) {
            groupedDeviceCommand.getClockCommand((ClockTask) protocolTask, groupedDeviceCommand, comTaskExecution);
        }
    },
    SET_CLOCK_COMMAND,
    FORCE_CLOCK_COMMAND,
    SYNCHRONIZE_CLOCK_COMMAND,

    TIME_DIFFERENCE_COMMAND,
    VERIFY_TIME_DIFFERENCE_COMMAND,

    TOPOLOGY_COMMAND(TopologyTask.class) {
        @Override
        public void createLegacyCommandsFromProtocolTask(GroupedDeviceCommand groupedDeviceCommand, List<ProtocolTask> protocolTasks, ProtocolTask protocolTask, ComTaskExecution comTaskExecution) {
            groupedDeviceCommand.getTopologyCommand((TopologyTask) protocolTask, groupedDeviceCommand, comTaskExecution);
        }

        @Override
        public void createCommandsFromTask(GroupedDeviceCommand groupedDeviceCommand, ProtocolTask protocolTask, ComTaskExecution comTaskExecution) {
            groupedDeviceCommand.getTopologyCommand((TopologyTask) protocolTask, groupedDeviceCommand, comTaskExecution);
        }
    },

    LOAD_PROFILE_COMMAND(LoadProfilesTask.class) {
        @Override
        public void createCommandsFromTask(GroupedDeviceCommand groupedDeviceCommand, ProtocolTask protocolTask, ComTaskExecution comTaskExecution) {
            groupedDeviceCommand.getLoadProfileCommand((LoadProfilesTask) protocolTask, groupedDeviceCommand, comTaskExecution);
        }

        @Override
        public void createLegacyCommandsFromProtocolTask(GroupedDeviceCommand groupedDeviceCommand, List<ProtocolTask> protocolTasks, ProtocolTask protocolTask, ComTaskExecution comTaskExecution) {
            LogBooksTask logBooksTask = checkGetLogBooksTask(protocolTasks);
            groupedDeviceCommand.getLegacyLoadProfileLogBooksCommand((LoadProfilesTask) protocolTask, logBooksTask, groupedDeviceCommand, comTaskExecution);
        }

        /**
         * Search and return the {@link LogBooksTask} from the set of {@link ProtocolTask ProtocolTasks}.
         * If the set doesn't contain a {@link LogBooksTask}, then null will be returned.
         *
         * @param protocolTasks The List of ProtocolTasks
         * @return the {@link LogBooksTask}
         *          null, if no {@link LogBooksTask} was found
         */
        private LogBooksTask checkGetLogBooksTask(List<ProtocolTask> protocolTasks) {
            for (ProtocolTask protocolTask : protocolTasks) {
                if (ComCommandTypes.forProtocolTask(protocolTask.getClass()).equals(ComCommandTypes.LOGBOOKS_COMMAND)) {
                    return (LogBooksTask) protocolTask;
                }
            }
            return null;
        }

    },
    VERIFY_LOAD_PROFILE_COMMAND,
    READ_LOAD_PROFILE_COMMAND,
    MARK_LOAD_PROFILES_AS_BAD_TIME,
    CREATE_METER_EVENTS_IN_LOAD_PROFILE_FROM_STATUS_FLAGS,

    BASIC_CHECK_COMMAND(BasicCheckTask.class) {
        @Override
        public void createLegacyCommandsFromProtocolTask(GroupedDeviceCommand groupedDeviceCommand, List<ProtocolTask> protocolTasks, ProtocolTask protocolTask, ComTaskExecution comTaskExecution) {
            groupedDeviceCommand.getBasicCheckCommand((BasicCheckTask) protocolTask, groupedDeviceCommand, comTaskExecution);
        }

        @Override
        public void createCommandsFromTask(GroupedDeviceCommand groupedDeviceCommand, ProtocolTask protocolTask, ComTaskExecution comTaskExecution) {
            groupedDeviceCommand.getBasicCheckCommand((BasicCheckTask) protocolTask, groupedDeviceCommand, comTaskExecution);
        }
    },
    VERIFY_SERIAL_NUMBER_COMMAND,

    REGISTERS_COMMAND(RegistersTask.class) {
        @Override
        public void createLegacyCommandsFromProtocolTask(GroupedDeviceCommand groupedDeviceCommand, List<ProtocolTask> protocolTasks, ProtocolTask protocolTask, ComTaskExecution comTaskExecution) {
            groupedDeviceCommand.getRegisterCommand((RegistersTask) protocolTask, groupedDeviceCommand, comTaskExecution);
        }

        @Override
        public void createCommandsFromTask(GroupedDeviceCommand groupedDeviceCommand, ProtocolTask protocolTask, ComTaskExecution comTaskExecution) {
            groupedDeviceCommand.getRegisterCommand((RegistersTask) protocolTask, groupedDeviceCommand, comTaskExecution);
        }
    },
    READ_REGISTERS_COMMAND,

    STATUS_INFORMATION_COMMAND(StatusInformationTask.class) {
        @Override
        public void createLegacyCommandsFromProtocolTask(GroupedDeviceCommand groupedDeviceCommand, List<ProtocolTask> protocolTasks, ProtocolTask protocolTask, ComTaskExecution comTaskExecution) {
            groupedDeviceCommand.getStatusInformationCommand(groupedDeviceCommand, comTaskExecution);
        }

        @Override
        public void createCommandsFromTask(GroupedDeviceCommand groupedDeviceCommand, ProtocolTask protocolTask, ComTaskExecution comTaskExecution) {
            groupedDeviceCommand.getStatusInformationCommand(groupedDeviceCommand, comTaskExecution);
        }
    },

    MESSAGES_COMMAND(MessagesTask.class) {
        @Override
        public void createLegacyCommandsFromProtocolTask(GroupedDeviceCommand groupedDeviceCommand, List<ProtocolTask> protocolTasks, ProtocolTask protocolTask, ComTaskExecution comTaskExecution) {
            groupedDeviceCommand.getMessagesCommand((MessagesTask) protocolTask, groupedDeviceCommand, comTaskExecution);
        }

        @Override
        public void createCommandsFromTask(GroupedDeviceCommand groupedDeviceCommand, ProtocolTask protocolTask, ComTaskExecution comTaskExecution) {
            groupedDeviceCommand.getMessagesCommand((MessagesTask) protocolTask, groupedDeviceCommand, comTaskExecution);
        }
    },

    LOGBOOKS_COMMAND(LogBooksTask.class) {
        @Override
        public void createCommandsFromTask(GroupedDeviceCommand groupedDeviceCommand, ProtocolTask protocolTask, ComTaskExecution comTaskExecution) {
            groupedDeviceCommand.getLogBooksCommand((LogBooksTask) protocolTask, groupedDeviceCommand, comTaskExecution);
        }

        @Override
        public void createLegacyCommandsFromProtocolTask(GroupedDeviceCommand groupedDeviceCommand, List<ProtocolTask> protocolTasks, ProtocolTask protocolTask, ComTaskExecution comTaskExecution) {
            LoadProfilesTask loadProfilesTask = checkGetLoadProfilesTask(protocolTasks);
            // Only need action when there is no load profiles task
            if (loadProfilesTask == null) {
                groupedDeviceCommand.getLegacyLoadProfileLogBooksCommand(null, (LogBooksTask) protocolTask, groupedDeviceCommand, comTaskExecution);
            }
        }

        /**
         * Search and return the {@link LoadProfilesTask} from the set of {@link ProtocolTask ProtocolTasks}.
         * If the set doesn't contain a {@link LoadProfilesTask}, then null will be returned.
         *
         * @param protocolTasks The List of ProtoclTasks
         * @return the {@link LoadProfilesTask}
         *          null, if no {@link LoadProfilesTask} was found
         */
        private LoadProfilesTask checkGetLoadProfilesTask(List<ProtocolTask> protocolTasks) {
            for (ProtocolTask protocolTask : protocolTasks) {
                if (ComCommandTypes.forProtocolTask(protocolTask.getClass()).equals(ComCommandTypes.LOAD_PROFILE_COMMAND)) {
                    return (LoadProfilesTask) protocolTask;
                }
            }
            return null;
        }

    },
    READ_LOGBOOKS_COMMAND,

    LEGACY_LOAD_PROFILE_LOGBOOKS_COMMAND,
    READ_LEGACY_LOAD_PROFILE_LOGBOOKS_COMMAND,

    FIRMWARE_COMMAND(FirmwareManagementTask.class) {
        @Override
        public void createCommandsFromTask(GroupedDeviceCommand groupedDeviceCommand, ProtocolTask protocolTask, ComTaskExecution comTaskExecution) {
            groupedDeviceCommand.getFirmwareCommand(groupedDeviceCommand, (FirmwareManagementTask) protocolTask, comTaskExecution);
        }

        @Override
        public void createLegacyCommandsFromProtocolTask(GroupedDeviceCommand groupedDeviceCommand, List<ProtocolTask> protocolTasks, ProtocolTask protocolTask, ComTaskExecution comTaskExecution) {
            groupedDeviceCommand.getFirmwareCommand(groupedDeviceCommand, (FirmwareManagementTask) protocolTask, comTaskExecution);
        }
    };

    /**
     * The protocolTask that can model a {@link ComCommand} from this type.
     */
    private Class protocolTaskClass;

    private ComCommandTypes(Class<? extends ProtocolTask> protocolTaskClass) {
        this.protocolTaskClass = protocolTaskClass;
    }

    private ComCommandTypes() {
    }

    /**
     * Get the commandType based on the given {@link ProtocolTask}
     *
     * @param protocolTaskClass the class of the ProtocolTask
     * @return the corresponding ComCommandType
     */
    public static ComCommandTypes forProtocolTask(final Class<? extends ProtocolTask> protocolTaskClass) {
        for (ComCommandTypes comCommandTypes : values()) {
            if (comCommandTypes.protocolTaskClass != null && comCommandTypes.protocolTaskClass.isAssignableFrom(protocolTaskClass)) {
                return comCommandTypes;
            }
        }
        return UNKNOWN;
    }

    public void createLegacyCommandsFromProtocolTask(GroupedDeviceCommand groupedDeviceCommand, List<ProtocolTask> protocolTasks, ProtocolTask protocolTask, ComTaskExecution comTaskExecution) {
        /* Default behavior is to create nothing
         * enum values that need to create something will override this method.
         * Consider logging the fact that this is being ignored. */
    }

    public void createCommandsFromTask(GroupedDeviceCommand groupedDeviceCommand, ProtocolTask protocolTask, ComTaskExecution comTaskExecution) {
        /* Default behavior is to create nothing
         * enum values that need to create something will override this method.
         * Consider logging the fact that this is being ignored. */
    }

}
