package com.energyict.mdc.engine.impl.commands.collect;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.core.CreateComTaskExecutionSessionTask;
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
    ROOT,
    COM_TASK_ROOT,

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
        public void createLegacyCommandsFromProtocolTask(CommandRoot root, List<? extends ProtocolTask> protocolTasks, ProtocolTask protocolTask, ComTaskExecution comTaskExecution) {
            root.getClockCommand((ClockTask) protocolTask, root, comTaskExecution);
        }

        @Override
        public void createCommandsFromTask(CommandRoot root, ProtocolTask protocolTask, ComTaskExecution comTaskExecution) {
            root.getClockCommand((ClockTask) protocolTask, root, comTaskExecution);
        }
    },
    SET_CLOCK_COMMAND,
    FORCE_CLOCK_COMMAND,
    SYNCHRONIZE_CLOCK_COMMAND,

    TIME_DIFFERENCE_COMMAND,
    VERIFY_TIME_DIFFERENCE_COMMAND,

    TOPOLOGY_COMMAND(TopologyTask.class) {
        @Override
        public void createLegacyCommandsFromProtocolTask(CommandRoot root, List<? extends ProtocolTask> protocolTasks, ProtocolTask protocolTask, ComTaskExecution comTaskExecution) {
            root.getTopologyCommand((TopologyTask) protocolTask, root, comTaskExecution);
        }

        @Override
        public void createCommandsFromTask(CommandRoot root, ProtocolTask protocolTask, ComTaskExecution comTaskExecution) {
            root.getTopologyCommand((TopologyTask) protocolTask, root, comTaskExecution);
        }
    },

    LOAD_PROFILE_COMMAND(LoadProfilesTask.class) {
        @Override
        public void createCommandsFromTask(CommandRoot root, ProtocolTask protocolTask, ComTaskExecution comTaskExecution) {
            root.getLoadProfileCommand((LoadProfilesTask) protocolTask, root, comTaskExecution);
        }

        @Override
        public void createLegacyCommandsFromProtocolTask(CommandRoot root, List<? extends ProtocolTask> protocolTasks, ProtocolTask protocolTask, ComTaskExecution comTaskExecution) {
            LogBooksTask logBooksTask = checkGetLogBooksTask(protocolTasks);
            root.getLegacyLoadProfileLogBooksCommand((LoadProfilesTask) protocolTask, logBooksTask, root, comTaskExecution);
        }

        /**
         * Search and return the {@link LogBooksTask} from the set of {@link ProtocolTask ProtocolTasks}.
         * If the set doesn't contain a {@link LogBooksTask}, then null will be returned.
         *
         * @param protocolTasks The List of ProtocolTasks
         * @return  the {@link LogBooksTask}
         *          null, if no {@link LogBooksTask} was found
         */
        private LogBooksTask checkGetLogBooksTask(List<? extends ProtocolTask> protocolTasks) {
            for (ProtocolTask protocolTask : protocolTasks) {
                if (ComCommandTypes.forProtocolTask(protocolTask).equals(ComCommandTypes.LOGBOOKS_COMMAND)) {
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
        public void createLegacyCommandsFromProtocolTask(CommandRoot root, List<? extends ProtocolTask> protocolTasks, ProtocolTask protocolTask, ComTaskExecution comTaskExecution) {
            root.getBasicCheckCommand((BasicCheckTask) protocolTask, root, comTaskExecution);
        }

        @Override
        public void createCommandsFromTask(CommandRoot root, ProtocolTask protocolTask, ComTaskExecution comTaskExecution) {
            root.getBasicCheckCommand((BasicCheckTask) protocolTask, root, comTaskExecution);
        }
    },
    VERIFY_SERIAL_NUMBER_COMMAND,

    REGISTERS_COMMAND(RegistersTask.class) {
        @Override
        public void createLegacyCommandsFromProtocolTask(CommandRoot root, List<? extends ProtocolTask> protocolTasks, ProtocolTask protocolTask, ComTaskExecution comTaskExecution) {
            root.getRegisterCommand((RegistersTask) protocolTask, root, comTaskExecution);
        }

        @Override
        public void createCommandsFromTask(CommandRoot root, ProtocolTask protocolTask, ComTaskExecution comTaskExecution) {
            root.getRegisterCommand((RegistersTask) protocolTask, root, comTaskExecution);
        }
    },
    READ_REGISTERS_COMMAND,

    STATUS_INFORMATION_COMMAND(StatusInformationTask.class) {
        @Override
        public void createLegacyCommandsFromProtocolTask(CommandRoot root, List<? extends ProtocolTask> protocolTasks, ProtocolTask protocolTask, ComTaskExecution comTaskExecution) {
            root.getStatusInformationCommand(root, comTaskExecution);
        }

        @Override
        public void createCommandsFromTask(CommandRoot root, ProtocolTask protocolTask, ComTaskExecution comTaskExecution) {
            root.getStatusInformationCommand(root, comTaskExecution);
        }
    },

    MESSAGES_COMMAND(MessagesTask.class) {
        @Override
        public void createLegacyCommandsFromProtocolTask(CommandRoot root, List<? extends ProtocolTask> protocolTasks, ProtocolTask protocolTask, ComTaskExecution comTaskExecution) {
            root.getMessagesCommand((MessagesTask) protocolTask, root, comTaskExecution);
        }

        @Override
        public void createCommandsFromTask(CommandRoot root, ProtocolTask protocolTask, ComTaskExecution comTaskExecution) {
            root.getMessagesCommand((MessagesTask) protocolTask, root, comTaskExecution);
        }
    },

    LOGBOOKS_COMMAND(LogBooksTask.class) {
        @Override
        public void createCommandsFromTask(CommandRoot root, ProtocolTask protocolTask, ComTaskExecution comTaskExecution) {
            root.getLogBooksCommand((LogBooksTask) protocolTask, root, comTaskExecution);
        }

        @Override
        public void createLegacyCommandsFromProtocolTask(CommandRoot root, List<? extends ProtocolTask> protocolTasks, ProtocolTask protocolTask, ComTaskExecution comTaskExecution) {
            LoadProfilesTask loadProfilesTask = checkGetLoadProfilesTask(protocolTasks);
            // Only need action when there is no load profiles task
            if (loadProfilesTask == null) {
                root.getLegacyLoadProfileLogBooksCommand(null, (LogBooksTask) protocolTask, root, comTaskExecution);
            }
        }

        /**
         * Search and return the {@link LoadProfilesTask} from the set of {@link ProtocolTask ProtocolTasks}.
         * If the set doesn't contain a {@link LoadProfilesTask}, then null will be returned.
         *
         * @param protocolTasks The List of ProtoclTasks
         * @return  the {@link LoadProfilesTask}
         *          null, if no {@link LoadProfilesTask} was found
         */
        private LoadProfilesTask checkGetLoadProfilesTask(List<? extends ProtocolTask> protocolTasks) {
            for (ProtocolTask protocolTask : protocolTasks) {
                if (ComCommandTypes.forProtocolTask(protocolTask).equals(ComCommandTypes.LOAD_PROFILE_COMMAND)) {
                    return (LoadProfilesTask) protocolTask;
                }
            }
            return null;
        }

    },
    READ_LOGBOOKS_COMMAND,

    LEGACY_LOAD_PROFILE_LOGBOOKS_COMMAND,
    READ_LEGACY_LOAD_PROFILE_LOGBOOKS_COMMAND,

    CREATE_COM_TASK_SESSION_COMMAND(CreateComTaskExecutionSessionTask.class){
        @Override
        public void createLegacyCommandsFromProtocolTask(CommandRoot root, List<? extends ProtocolTask> protocolTasks, ProtocolTask protocolTask, ComTaskExecution comTaskExecution) {
            root.createComTaskSessionTask((CreateComTaskExecutionSessionTask) protocolTask, root, comTaskExecution);
        }

        @Override
        public void createCommandsFromTask(CommandRoot root, ProtocolTask protocolTask, ComTaskExecution comTaskExecution) {
            root.createComTaskSessionTask((CreateComTaskExecutionSessionTask) protocolTask, root, comTaskExecution);
        }
    },
    FIRMWARE_COMMAND(FirmwareManagementTask.class){
        @Override
        public void createLegacyCommandsFromProtocolTask(CommandRoot root, List<? extends ProtocolTask> protocolTasks, ProtocolTask protocolTask, ComTaskExecution comTaskExecution) {
            root.getFirmwareCommand((FirmwareManagementTask) protocolTask, root, comTaskExecution);
        }

        @Override
        public void createCommandsFromTask(CommandRoot root, ProtocolTask protocolTask, ComTaskExecution comTaskExecution) {
            root.getFirmwareCommand((FirmwareManagementTask) protocolTask, root, comTaskExecution);
        }
    };

    /**
     * The protocolTask that can model a {@link ComCommand} from this type.
     */
    private Class<? extends ProtocolTask> protocolTaskClass;

    private ComCommandTypes(Class<? extends ProtocolTask> protocolTaskClass) {
        this.protocolTaskClass = protocolTaskClass;
    }

    private ComCommandTypes() {
    }

    /**
     * Get the CommandType for the given {@link ProtocolTask}.
     *
     * @param protocolTask The ProtocolTask
     * @return the corresponding ComCommandType
     */
    public static ComCommandType forProtocolTask(ProtocolTask protocolTask) {
        if (protocolTask instanceof CreateComTaskExecutionSessionTask) {
            CreateComTaskExecutionSessionTask createComTaskExecutionSessionTask = (CreateComTaskExecutionSessionTask) protocolTask;
            return new CreateComTaskExecutionSessionCommandType(createComTaskExecutionSessionTask.getComTask(), createComTaskExecutionSessionTask.getComTaskExecution());
        }
        else {
            for (ComCommandTypes comCommandTypes : values()) {
                if (comCommandTypes.appliesTo(protocolTask)) {
                    return comCommandTypes;
                }
            }
            return UNKNOWN;
        }
    }

    public boolean appliesTo(ProtocolTask protocolTask) {
        Class<? extends ProtocolTask> protocolTaskClass = protocolTask.getClass();
        return this.protocolTaskClass != null && this.protocolTaskClass.isAssignableFrom(protocolTaskClass);
    }

    @Override
    public void createLegacyCommandsFromProtocolTask(CommandRoot root, List<? extends ProtocolTask> protocolTasks, ProtocolTask protocolTask, ComTaskExecution comTaskExecution) {
        /* Default behavior is to create nothing
         * enum values that need to create something will override this method.
         * Consider logging the fact that this is being ignored. */
    }

    @Override
    public void createCommandsFromTask(CommandRoot root, ProtocolTask protocolTask, ComTaskExecution comTaskExecution) {
        /* Default behavior is to create nothing
         * enum values that need to create something will override this method.
         * Consider logging the fact that this is being ignored. */
    }

}
