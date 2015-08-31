package com.energyict.mdc.engine.impl.commands.store.core;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.collect.BasicCheckCommand;
import com.energyict.mdc.engine.impl.commands.collect.ClockCommand;
import com.energyict.mdc.engine.impl.commands.collect.ComCommand;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandType;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.collect.CompositeComCommand;
import com.energyict.mdc.engine.impl.commands.collect.CreateComTaskExecutionSessionCommand;
import com.energyict.mdc.engine.impl.commands.collect.CreateMeterEventsFromStatusFlagsCommand;
import com.energyict.mdc.engine.impl.commands.collect.FirmwareManagementCommand;
import com.energyict.mdc.engine.impl.commands.collect.ForceClockCommand;
import com.energyict.mdc.engine.impl.commands.collect.LegacyLoadProfileLogBooksCommand;
import com.energyict.mdc.engine.impl.commands.collect.LoadProfileCommand;
import com.energyict.mdc.engine.impl.commands.collect.LogBooksCommand;
import com.energyict.mdc.engine.impl.commands.collect.MarkIntervalsAsBadTimeCommand;
import com.energyict.mdc.engine.impl.commands.collect.MessagesCommand;
import com.energyict.mdc.engine.impl.commands.collect.ReadLegacyLoadProfileLogBooksDataCommand;
import com.energyict.mdc.engine.impl.commands.collect.ReadLoadProfileDataCommand;
import com.energyict.mdc.engine.impl.commands.collect.ReadLogBooksCommand;
import com.energyict.mdc.engine.impl.commands.collect.ReadRegistersCommand;
import com.energyict.mdc.engine.impl.commands.collect.RegisterCommand;
import com.energyict.mdc.engine.impl.commands.collect.SetClockCommand;
import com.energyict.mdc.engine.impl.commands.collect.StatusInformationCommand;
import com.energyict.mdc.engine.impl.commands.collect.SynchronizeClockCommand;
import com.energyict.mdc.engine.impl.commands.collect.TimeDifferenceCommand;
import com.energyict.mdc.engine.impl.commands.collect.TopologyCommand;
import com.energyict.mdc.engine.impl.commands.collect.VerifyLoadProfilesCommand;
import com.energyict.mdc.engine.impl.commands.collect.VerifySerialNumberCommand;
import com.energyict.mdc.engine.impl.commands.collect.VerifyTimeDifferenceCommand;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.BasicCheckCommandImpl;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.ClockCommandImpl;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.CreateComTaskExecutionSessionCommandImpl;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.CreateMeterEventsFromStatusFlagsCommandImpl;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.FirmwareManagementCommandImpl;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.ForceClockCommandImpl;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.LegacyLoadProfileLogBooksCommandImpl;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.LoadProfileCommandImpl;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.LogBooksCommandImpl;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.MarkIntervalsAsBadTimeCommandImpl;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.MessagesCommandImpl;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.ReadLegacyLoadProfileLogBooksDataCommandImpl;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.ReadLoadProfileDataCommandImpl;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.ReadLogBooksCommandImpl;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.ReadRegistersCommandImpl;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.RegisterCommandImpl;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.SetClockCommandImpl;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.StatusInformationCommandImpl;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.SynchronizeClockCommandImpl;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.TimeDifferenceCommandImpl;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.TopologyCommandImpl;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.VerifyLoadProfilesCommandImpl;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.VerifySerialNumberCommandImpl;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.VerifyTimeDifferenceCommandImpl;
import com.energyict.mdc.engine.impl.core.CreateComTaskExecutionSessionTask;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.core.JobExecution;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.data.CollectedData;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.tasks.BasicCheckTask;
import com.energyict.mdc.tasks.ClockTask;
import com.energyict.mdc.tasks.FirmwareManagementTask;
import com.energyict.mdc.tasks.LoadProfilesTask;
import com.energyict.mdc.tasks.LogBooksTask;
import com.energyict.mdc.tasks.MessagesTask;
import com.energyict.mdc.tasks.RegistersTask;
import com.energyict.mdc.tasks.TopologyTask;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implementation for the {@link CommandRoot} interface.
 *
 * @author gna
 * @since 10/05/12 - 14:29
 */
public class CommandRootImpl extends CompositeComCommandImpl implements CommandRoot {

    /**
     * The {@link OfflineDevice} which owns this CommandRoot.
     */
    private final OfflineDevice offlineDevice;
    private final ServiceProvider serviceProvider;
    private final ExecutionContext executionContext;
    private final boolean exposeStoringException;
    private JobExecution.PreparedComTaskExecution preparedComTaskExecution;
    private Map<ComTaskExecution, ComTaskExecutionComCommand> comCommandsPerComTaskExecution = new HashMap<>();

    public CommandRootImpl(OfflineDevice offlineDevice, ExecutionContext executionContext, CommandRoot.ServiceProvider serviceProvider) {
        this(offlineDevice, executionContext, serviceProvider, false);
    }

    public CommandRootImpl(OfflineDevice offlineDevice, ExecutionContext executionContext, CommandRoot.ServiceProvider serviceProvider, boolean exposeStoringException) {
        super(null);
        this.offlineDevice = offlineDevice;
        this.executionContext = executionContext;
        this.serviceProvider = serviceProvider;
        this.exposeStoringException = exposeStoringException;
    }

    @Override
    public String getDescriptionTitle() {
        return "All commands are executed";
    }

    @Override
    public ExecutionContext getExecutionContext () {
        return this.executionContext;
    }

    @Override
    public ComCommand getComCommand(final ComCommandTypes commandType) {
        return getComCommandFromList(commandType, getCommands());
    }

    @Override
    public void addUniqueCommand(ComCommand command, ComTaskExecution comTaskExecution) {
        ComTaskExecutionComCommand comTaskExecutionComCommand = this.getComTaskRoot(comTaskExecution);
        comTaskExecutionComCommand.addUniqueCommand(command, comTaskExecution);
        super.addUniqueCommand(command, comTaskExecution);
    }

    @Override
    public void addCommand(CreateComTaskExecutionSessionCommand command, ComTaskExecution comTaskExecution) {
        ComTaskExecutionComCommand comTaskExecutionComCommand = this.getComTaskRoot(comTaskExecution);
        comTaskExecutionComCommand.addCommand(command, comTaskExecution);
        super.addCommand(command, comTaskExecution);
    }

    @Override
    public List<Issue> getIssues () {
        Set<Issue> uniqueIssues = new HashSet<>(super.getIssues());
        for (ComCommand child : this) {
            uniqueIssues.addAll(child.getIssues());
        }
        return new ArrayList<>(uniqueIssues);
    }

    /**
     * Finds the {@link ComCommand} with the given {@link ComCommandTypes} from the given List.
     *
     * @param commandType the type of ComCommand to search for
     * @param comCommands the list of ComCommands to search in
     * @return the requested ComCommand
     */
    private ComCommand getComCommandFromList(final ComCommandTypes commandType, final Map<ComCommandType, ComCommand> comCommands) {
        if (comCommands.get(commandType) != null) {
            return comCommands.get(commandType);
        }
        for (ComCommand command : comCommands.values()) {
            if (command instanceof CompositeComCommand) {
                final ComCommand comCommandFromList = getComCommandFromList(commandType, ((CompositeComCommand) command).getCommands());
                if (comCommandFromList != null) {
                    return comCommandFromList;
                }
            }
        }
        return null;
    }

    @Override
    public ComTaskExecutionComCommand getComTaskRoot (ComTaskExecution comTaskExecution) {
        ComTaskExecutionComCommand comTaskExecutionComCommand = this.comCommandsPerComTaskExecution.get(comTaskExecution);
        if (comTaskExecutionComCommand == null) {
            comTaskExecutionComCommand = new ComTaskExecutionComCommandImpl(this, comTaskExecution);
            this.comCommandsPerComTaskExecution.put(comTaskExecution, comTaskExecutionComCommand);
        }
        return comTaskExecutionComCommand;
    }

    @Override
    public LoadProfileCommand getLoadProfileCommand(final LoadProfilesTask loadProfilesTask, final CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution) {
        if(checkCommandTypeExistence(ComCommandTypes.LOAD_PROFILE_COMMAND, getCommands())){
            final LoadProfileCommand comCommand = (LoadProfileCommand) getComCommand(ComCommandTypes.LOAD_PROFILE_COMMAND);
            comCommand.updateLoadProfileReaders(loadProfilesTask, comTaskExecution.getDevice().getmRID());
            return comCommand;
        } else {
            return createLoadProfileCommand(loadProfilesTask, possibleCommandOwner, comTaskExecution);
        }
    }

    private LoadProfileCommand createLoadProfileCommand(LoadProfilesTask loadProfilesTask, CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution) {
        LoadProfileCommand loadProfileCommand = new LoadProfileCommandImpl(loadProfilesTask, this.offlineDevice, this, comTaskExecution);
        possibleCommandOwner.addUniqueCommand(loadProfileCommand, comTaskExecution);
        return loadProfileCommand;
    }

    @Override
    public RegisterCommand getRegisterCommand(final RegistersTask registersTask, final CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution) {
        if(checkCommandTypeExistence(ComCommandTypes.REGISTERS_COMMAND, getCommands())){
            return (RegisterCommand) getComCommand(ComCommandTypes.REGISTERS_COMMAND);
        } else {
            return createRegisterCommand(registersTask, possibleCommandOwner, comTaskExecution);
        }
    }

    private RegisterCommand createRegisterCommand(RegistersTask registersTask, CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution) {
        RegisterCommand registerCommand = new RegisterCommandImpl(registersTask, this.offlineDevice, this, comTaskExecution);
        possibleCommandOwner.addUniqueCommand(registerCommand, comTaskExecution);
        return registerCommand;
    }

    @Override
    public LogBooksCommand getLogBooksCommand(final LogBooksTask logBooksTask, final CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution) {
        if(checkCommandTypeExistence(ComCommandTypes.LOGBOOKS_COMMAND, getCommands())){
            return (LogBooksCommand) getComCommand(ComCommandTypes.LOGBOOKS_COMMAND);
        } else {
            return createLogBooksCommand(logBooksTask, possibleCommandOwner, comTaskExecution);
        }
    }

    private LogBooksCommand createLogBooksCommand(LogBooksTask logBooksTask, CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution) {
        LogBooksCommand logBooksCommand = new LogBooksCommandImpl(logBooksTask, this.offlineDevice, this, comTaskExecution);
        possibleCommandOwner.addUniqueCommand(logBooksCommand, comTaskExecution);
        return logBooksCommand;
    }

    @Override
    public LegacyLoadProfileLogBooksCommand getLegacyLoadProfileLogBooksCommand(LoadProfilesTask loadProfilesTask, LogBooksTask logBooksTask, CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution) {
        if (checkCommandTypeExistence(ComCommandTypes.LEGACY_LOAD_PROFILE_LOGBOOKS_COMMAND, getCommands())) {
            return (LegacyLoadProfileLogBooksCommand) getComCommand(ComCommandTypes.LEGACY_LOAD_PROFILE_LOGBOOKS_COMMAND);
        } else {
            return createLegacyLoadProfileLogBooksCommand(loadProfilesTask, logBooksTask, possibleCommandOwner, comTaskExecution);
        }
    }

    private LegacyLoadProfileLogBooksCommand createLegacyLoadProfileLogBooksCommand(LoadProfilesTask loadProfilesTask, LogBooksTask logBooksTask, CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution) {
        LegacyLoadProfileLogBooksCommand legacyCommand = new LegacyLoadProfileLogBooksCommandImpl(loadProfilesTask, logBooksTask, this.offlineDevice, this, comTaskExecution);
        possibleCommandOwner.addUniqueCommand(legacyCommand, comTaskExecution);
        return legacyCommand;
    }

    @Override
    public BasicCheckCommand getBasicCheckCommand(BasicCheckTask basicCheckTask, CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution) {
        if(checkCommandTypeExistence(ComCommandTypes.BASIC_CHECK_COMMAND, getCommands())){
            return (BasicCheckCommand) getComCommand(ComCommandTypes.BASIC_CHECK_COMMAND);
        } else {
            return createBasicCheckCommand(basicCheckTask, possibleCommandOwner, comTaskExecution);
        }
    }

    public BasicCheckCommand createBasicCheckCommand(BasicCheckTask basicCheckTask, CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution) {
        BasicCheckCommand basicCheckCommand = new BasicCheckCommandImpl(basicCheckTask, this, comTaskExecution);
        possibleCommandOwner.addUniqueCommand(basicCheckCommand, comTaskExecution);
        return basicCheckCommand;
    }

    @Override
    public StatusInformationCommand getStatusInformationCommand(final CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution) {
        if(checkCommandTypeExistence(ComCommandTypes.STATUS_INFORMATION_COMMAND, getCommands())){
            return (StatusInformationCommand) getComCommand(ComCommandTypes.STATUS_INFORMATION_COMMAND);
        } else {
            return createStatusInformationCommand(possibleCommandOwner, comTaskExecution);
        }
    }

    public StatusInformationCommand createStatusInformationCommand(CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution) {
        StatusInformationCommand statusInformationCommand = new StatusInformationCommandImpl(this.offlineDevice, this, comTaskExecution);
        possibleCommandOwner.addUniqueCommand(statusInformationCommand, comTaskExecution);
        return statusInformationCommand;
    }

    @Override
    public ReadRegistersCommand getReadRegistersCommand(final CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution) {
        if(checkCommandTypeExistence(ComCommandTypes.READ_REGISTERS_COMMAND, getCommands())){
            return (ReadRegistersCommand) getComCommand(ComCommandTypes.READ_REGISTERS_COMMAND);
        } else {
            return createReadRegistersCommand(possibleCommandOwner, comTaskExecution);
        }
    }

    public ReadRegistersCommand createReadRegistersCommand(CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution) {
        ReadRegistersCommandImpl readRegistersCommand = new ReadRegistersCommandImpl(possibleCommandOwner,  this);
        possibleCommandOwner.addUniqueCommand(readRegistersCommand, comTaskExecution);
        return readRegistersCommand;
    }

    @Override
    public ReadLogBooksCommand getReadLogBooksCommand(LogBooksCommand logBooksCommand, ComTaskExecution comTaskExecution) {
        if(checkCommandTypeExistence(ComCommandTypes.READ_LOGBOOKS_COMMAND, getCommands())){
            return (ReadLogBooksCommand) getComCommand(ComCommandTypes.READ_LOGBOOKS_COMMAND);
        } else {
            return createReadLogBooksCommand(logBooksCommand, comTaskExecution);
        }
    }

    public ReadLogBooksCommand createReadLogBooksCommand(LogBooksCommand logBooksCommand, ComTaskExecution comTaskExecution) {
        ReadLogBooksCommandImpl readLogBooksCommand = new ReadLogBooksCommandImpl(logBooksCommand, this);
        logBooksCommand.addUniqueCommand(readLogBooksCommand, comTaskExecution);
        return readLogBooksCommand;
    }

    @Override
    public ClockCommand getClockCommand(final ClockTask clockTask, final CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution) {
        if(checkCommandTypeExistence(ComCommandTypes.CLOCK_COMMAND, getCommands())){
            return (ClockCommand) getComCommand(ComCommandTypes.CLOCK_COMMAND);
        } else {
            return createClockCommand(clockTask, possibleCommandOwner, comTaskExecution);
        }
    }

    public ClockCommand createClockCommand(ClockTask clockTask, CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution) {
        ClockCommand clockCommand = new ClockCommandImpl(clockTask, this, comTaskExecution);
        possibleCommandOwner.addUniqueCommand(clockCommand, comTaskExecution);
        return clockCommand;
    }

    @Override
    public MessagesCommand getMessagesCommand(MessagesTask messagesTask, CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution) {
        if(checkCommandTypeExistence(ComCommandTypes.MESSAGES_COMMAND, getCommands())){
            return (MessagesCommand) getComCommand(ComCommandTypes.MESSAGES_COMMAND);
        } else {
            return createMessagesCommand(messagesTask, possibleCommandOwner, comTaskExecution);
        }
    }

    public MessagesCommand createMessagesCommand(MessagesTask messagesTask, CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution) {
        MessagesCommand messagesCommand = new MessagesCommandImpl(messagesTask, this.offlineDevice, this, comTaskExecution);
        possibleCommandOwner.addUniqueCommand(messagesCommand, comTaskExecution);
        return messagesCommand;
    }

    @Override
    public TimeDifferenceCommand getTimeDifferenceCommand(final CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution) {
        if (checkCommandTypeExistence(ComCommandTypes.TIME_DIFFERENCE_COMMAND, getCommands())) {
            return (TimeDifferenceCommand) getComCommand(ComCommandTypes.TIME_DIFFERENCE_COMMAND);
        } else {
            return createTimeDifferenceCommand(possibleCommandOwner, comTaskExecution);
        }
    }

    public TimeDifferenceCommand createTimeDifferenceCommand(CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution) {
        TimeDifferenceCommand timeDifferenceCommand = new TimeDifferenceCommandImpl(this);
        possibleCommandOwner.addUniqueCommand(timeDifferenceCommand, comTaskExecution);
        return timeDifferenceCommand;
    }

    @Override
    public VerifyTimeDifferenceCommand getVerifyTimeDifferenceCommand(BasicCheckCommand basicCheckCommand, CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution) {
        if (checkCommandTypeExistence(ComCommandTypes.VERIFY_TIME_DIFFERENCE_COMMAND, getCommands())) {
            return (VerifyTimeDifferenceCommand) getComCommand(ComCommandTypes.VERIFY_TIME_DIFFERENCE_COMMAND);
        } else {
            return createVerifyTimeDifferenceCommand(basicCheckCommand, possibleCommandOwner, comTaskExecution);
        }
    }

    public VerifyTimeDifferenceCommand createVerifyTimeDifferenceCommand(BasicCheckCommand basicCheckCommand, CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution) {
        VerifyTimeDifferenceCommand verifyTimeDifferenceCommand = new VerifyTimeDifferenceCommandImpl(basicCheckCommand, this);
        possibleCommandOwner.addUniqueCommand(verifyTimeDifferenceCommand, comTaskExecution);
        return verifyTimeDifferenceCommand;
    }

    @Override
    public VerifyLoadProfilesCommand getVerifyLoadProfileCommand(final LoadProfileCommand loadProfileCommand, ComTaskExecution comTaskExecution) {
        if (checkCommandTypeExistence(ComCommandTypes.VERIFY_LOAD_PROFILE_COMMAND, getCommands())) {
            return (VerifyLoadProfilesCommand) getComCommand(ComCommandTypes.VERIFY_LOAD_PROFILE_COMMAND);
        } else {
            return createVerifyLoadProfilesCommand(loadProfileCommand, comTaskExecution);
        }
    }

    public VerifyLoadProfilesCommand createVerifyLoadProfilesCommand(LoadProfileCommand loadProfileCommand, ComTaskExecution comTaskExecution) {
        VerifyLoadProfilesCommand verifyLoadProfilesCommand = new VerifyLoadProfilesCommandImpl(loadProfileCommand, this);
        loadProfileCommand.addUniqueCommand(verifyLoadProfilesCommand, comTaskExecution);
        return verifyLoadProfilesCommand;
    }

    @Override
    public ReadLoadProfileDataCommand getReadLoadProfileDataCommand(final LoadProfileCommand loadProfileCommand, ComTaskExecution comTaskExecution) {
        if (checkCommandTypeExistence(ComCommandTypes.READ_LOAD_PROFILE_COMMAND, getCommands())) {
            return (ReadLoadProfileDataCommand) getComCommand(ComCommandTypes.READ_LOAD_PROFILE_COMMAND);
        } else {
            return createReadLoadProfileDataCommand(loadProfileCommand, comTaskExecution);
        }
    }

    public ReadLoadProfileDataCommand createReadLoadProfileDataCommand(LoadProfileCommand loadProfileCommand, ComTaskExecution comTaskExecution) {
        ReadLoadProfileDataCommand readLoadProfileDataCommand = new ReadLoadProfileDataCommandImpl(loadProfileCommand, this);
        loadProfileCommand.addUniqueCommand(readLoadProfileDataCommand, comTaskExecution);
        return readLoadProfileDataCommand;
    }

    @Override
       public ReadLegacyLoadProfileLogBooksDataCommand getReadLegacyLoadProfileLogBooksDataCommand(LegacyLoadProfileLogBooksCommand legacyLoadProfileLogBooksCommand, ComTaskExecution comTaskExecution) {
        if (checkCommandTypeExistence(ComCommandTypes.READ_LEGACY_LOAD_PROFILE_LOGBOOKS_COMMAND, getCommands())) {
            return (ReadLegacyLoadProfileLogBooksDataCommand) getComCommand(ComCommandTypes.READ_LEGACY_LOAD_PROFILE_LOGBOOKS_COMMAND);
        } else {
            return createReadLegacyLoadProfileLogBooksDataCommand(legacyLoadProfileLogBooksCommand, comTaskExecution);
        }
    }

    public ReadLegacyLoadProfileLogBooksDataCommand createReadLegacyLoadProfileLogBooksDataCommand(LegacyLoadProfileLogBooksCommand legacyLoadProfileLogBooksCommand, ComTaskExecution comTaskExecution) {
        ReadLegacyLoadProfileLogBooksDataCommand readLegacyLoadProfileLogBooksDataCommand = new ReadLegacyLoadProfileLogBooksDataCommandImpl(legacyLoadProfileLogBooksCommand, this);
        legacyLoadProfileLogBooksCommand.addUniqueCommand(readLegacyLoadProfileLogBooksDataCommand, comTaskExecution);
        return readLegacyLoadProfileLogBooksDataCommand;
    }

    @Override
    public MarkIntervalsAsBadTimeCommand getMarkIntervalsAsBadTimeCommand(final LoadProfileCommand loadProfileCommand, ComTaskExecution comTaskExecution) {
        if (checkCommandTypeExistence(ComCommandTypes.MARK_LOAD_PROFILES_AS_BAD_TIME, getCommands())) {
            return (MarkIntervalsAsBadTimeCommand) getComCommand(ComCommandTypes.MARK_LOAD_PROFILES_AS_BAD_TIME);
        } else {
            return createMarkIntervalsAsBadTimeCommand(loadProfileCommand, comTaskExecution);
        }
    }

    public MarkIntervalsAsBadTimeCommand createMarkIntervalsAsBadTimeCommand(LoadProfileCommand loadProfileCommand, ComTaskExecution comTaskExecution) {
        MarkIntervalsAsBadTimeCommand markIntervalsAsBadTimeCommand = new MarkIntervalsAsBadTimeCommandImpl(loadProfileCommand, this);
        loadProfileCommand.addUniqueCommand(markIntervalsAsBadTimeCommand, comTaskExecution);
        return markIntervalsAsBadTimeCommand;
    }

    @Override
    public CreateMeterEventsFromStatusFlagsCommand getCreateMeterEventsFromStatusFlagsCommand(final LoadProfileCommand loadProfileCommand, ComTaskExecution comTaskExecution) {
        if (checkCommandTypeExistence(ComCommandTypes.CREATE_METER_EVENTS_IN_LOAD_PROFILE_FROM_STATUS_FLAGS, getCommands())) {
            return (CreateMeterEventsFromStatusFlagsCommand) getComCommand(ComCommandTypes.CREATE_METER_EVENTS_IN_LOAD_PROFILE_FROM_STATUS_FLAGS);
        } else {
            return createMeterEventsFromStatusFlagsCommand(loadProfileCommand, comTaskExecution);
        }
    }

    public CreateMeterEventsFromStatusFlagsCommand createMeterEventsFromStatusFlagsCommand(LoadProfileCommand loadProfileCommand, ComTaskExecution comTaskExecution) {
        CreateMeterEventsFromStatusFlagsCommand createMeterEventsFromStatusFlagsCommand = new CreateMeterEventsFromStatusFlagsCommandImpl(loadProfileCommand, this);
        loadProfileCommand.addUniqueCommand(createMeterEventsFromStatusFlagsCommand, comTaskExecution);
        return createMeterEventsFromStatusFlagsCommand;
    }

    @Override
    public ForceClockCommand getForceClockCommand(final ClockCommand clockCommand, ComTaskExecution comTaskExecution) {
        if(checkCommandTypeExistence(ComCommandTypes.FORCE_CLOCK_COMMAND, getCommands())){
            return (ForceClockCommand) getComCommand(ComCommandTypes.FORCE_CLOCK_COMMAND);
        } else {
            return createForceClockCommand(clockCommand, comTaskExecution);
        }
    }

    public ForceClockCommand createForceClockCommand(ClockCommand clockCommand, ComTaskExecution comTaskExecution) {
        ForceClockCommand forceClockCommand = new ForceClockCommandImpl(this);
        clockCommand.addUniqueCommand(forceClockCommand, comTaskExecution);
        return forceClockCommand;
    }

    @Override
    public SetClockCommand getSetClockCommand(final ClockCommand clockCommand, ComTaskExecution comTaskExecution) {
        if(checkCommandTypeExistence(ComCommandTypes.SET_CLOCK_COMMAND, getCommands())){
            return (SetClockCommand) getComCommand(ComCommandTypes.SET_CLOCK_COMMAND);
        } else {
            return createSetClockCommand(clockCommand, comTaskExecution);
        }
    }

    public SetClockCommand createSetClockCommand(ClockCommand clockCommand, ComTaskExecution comTaskExecution) {
        SetClockCommand setClockCommand = new SetClockCommandImpl(clockCommand, this, comTaskExecution);
        clockCommand.addUniqueCommand(setClockCommand, comTaskExecution);
        return setClockCommand;
    }

    @Override
    public SynchronizeClockCommand getSynchronizeClockCommand(final ClockCommand clockCommand, ComTaskExecution comTaskExecution) {
        if(checkCommandTypeExistence(ComCommandTypes.SYNCHRONIZE_CLOCK_COMMAND, getCommands())){
            return (SynchronizeClockCommand) getComCommand(ComCommandTypes.SYNCHRONIZE_CLOCK_COMMAND);
        } else {
            return createSynchronizeClockCommand(clockCommand, comTaskExecution);
        }
    }

    public SynchronizeClockCommand createSynchronizeClockCommand(ClockCommand clockCommand, ComTaskExecution comTaskExecution) {
        SynchronizeClockCommand synchronizeClockCommand = new SynchronizeClockCommandImpl(clockCommand, this, comTaskExecution);
        clockCommand.addUniqueCommand(synchronizeClockCommand, comTaskExecution);
        return synchronizeClockCommand;
    }

    @Override
    public TopologyCommand getTopologyCommand(final TopologyTask topologyTask, final CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution) {
        if (checkCommandTypeExistence(ComCommandTypes.TOPOLOGY_COMMAND, getCommands())) {
            return (TopologyCommand) getComCommand(ComCommandTypes.TOPOLOGY_COMMAND);
        } else {
            return createTopologyCommand(topologyTask, possibleCommandOwner, comTaskExecution);
        }
    }

    public TopologyCommand createTopologyCommand(TopologyTask topologyTask, CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution) {
        TopologyCommand topologyCommand = new TopologyCommandImpl(this, topologyTask.getTopologyAction(), this.offlineDevice, comTaskExecution);
        possibleCommandOwner.addUniqueCommand(topologyCommand, comTaskExecution);
        return topologyCommand;
    }

    @Override
    public FirmwareManagementCommand getFirmwareCommand(FirmwareManagementTask firmwareManagementTask, CommandRoot possibleCommandOwner, ComTaskExecution comTaskExecution) {
        if(checkCommandTypeExistence(ComCommandTypes.FIRMWARE_COMMAND, getCommands())){
            return ((FirmwareManagementCommand) getComCommand(ComCommandTypes.FIRMWARE_COMMAND));
        }
        return createFirmwareUpgradeCommand(firmwareManagementTask, possibleCommandOwner, comTaskExecution);
    }

    private FirmwareManagementCommand createFirmwareUpgradeCommand(FirmwareManagementTask firmwareManagementTask, CommandRoot possibleCommandOwner, ComTaskExecution comTaskExecution) {
        FirmwareManagementCommandImpl firmwareUpgradeCommand = new FirmwareManagementCommandImpl(this, firmwareManagementTask, comTaskExecution, this.offlineDevice);
        possibleCommandOwner.addUniqueCommand(firmwareUpgradeCommand, comTaskExecution);
        return firmwareUpgradeCommand;
    }

    @Override
    public VerifySerialNumberCommand getVerifySerialNumberCommand(final BasicCheckCommand comCommands, ComTaskExecution comTaskExecution) {
        if(checkCommandTypeExistence(ComCommandTypes.VERIFY_SERIAL_NUMBER_COMMAND, getCommands())){
            return (VerifySerialNumberCommand) getComCommand(ComCommandTypes.VERIFY_SERIAL_NUMBER_COMMAND);
        } else {
            return createVerifySerialNumberCommand(comCommands, comTaskExecution);
        }
    }

    public VerifySerialNumberCommand createVerifySerialNumberCommand(BasicCheckCommand comCommands, ComTaskExecution comTaskExecution) {
        VerifySerialNumberCommand verifySerialNumberCommand = new VerifySerialNumberCommandImpl(this.offlineDevice, this);
        comCommands.addUniqueCommand(verifySerialNumberCommand, comTaskExecution);
        return verifySerialNumberCommand;
    }

    @Override
    public CreateComTaskExecutionSessionCommand createComTaskSessionTask(CreateComTaskExecutionSessionTask protocolTask, CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution) {
        CreateComTaskExecutionSessionCommandImpl createComTaskSessionCommand = new CreateComTaskExecutionSessionCommandImpl(protocolTask, this, comTaskExecution);
        possibleCommandOwner.addCommand(createComTaskSessionCommand, comTaskExecution);
        return createComTaskSessionCommand;
    }

    @Override
    public void executeFor(JobExecution.PreparedComTaskExecution preparedComTaskExecution, ExecutionContext executionContext) {
        this.preparedComTaskExecution = preparedComTaskExecution;
        super.execute(preparedComTaskExecution.getDeviceProtocol(), executionContext);
        this.preparedComTaskExecution = null;
    }

    @Override
    public ServiceProvider getServiceProvider() {
        return serviceProvider;
    }

    @Override
    protected void performTheComCommandIfAllowed(DeviceProtocol deviceProtocol, ExecutionContext executionContext, ComCommand comCommand) {
        if(this.preparedComTaskExecution != null){
            ComTaskExecutionComCommand comTaskExecutionComCommand = this.comCommandsPerComTaskExecution.get(this.preparedComTaskExecution.getComTaskExecution());
            if (comTaskExecutionComCommand != null && comTaskExecutionComCommand.contains(comCommand)){
                super.performTheComCommandIfAllowed(deviceProtocol, executionContext, comCommand);
            }
        } else {
            super.performTheComCommandIfAllowed(deviceProtocol, executionContext, comCommand);
        }
    }

    @Override
    public boolean hasExecuted() {
        return false; // the root can always execute, the ComTaskExecution will determine whether or not he actually has to do something
    }

    @Override
    public CommandRoot getCommandRoot() {
        return this;
    }

    @Override
    public ComCommandTypes getCommandType() {
        return ComCommandTypes.ROOT;
    }

    @Override
    public List<CollectedData> getCollectedData () {
        List<CollectedData> everything = new ArrayList<>();
        for (ComCommand command : this.comCommandsPerComTaskExecution.values()) {
            everything.addAll(command.getCollectedData());
        }
        return everything;
    }

    @Override
    public CompletionCode getCompletionCode() {
        return this.getMyCompletionCode();
    }

    protected LogLevel defaultJournalingLogLevel () {
        return LogLevel.DEBUG;
    }


    @Override
    public boolean isExposeStoringException() {
        return exposeStoringException;
    }
}