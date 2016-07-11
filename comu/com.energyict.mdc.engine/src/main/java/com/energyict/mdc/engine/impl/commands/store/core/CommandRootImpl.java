package com.energyict.mdc.engine.impl.commands.store.core;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.impl.commands.collect.BasicCheckCommand;
import com.energyict.mdc.engine.impl.commands.collect.ClockCommand;
import com.energyict.mdc.engine.impl.commands.collect.ComCommand;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandKey;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    /**
     * ID for the securitySet command group<br/>
     * Commands having the same command group use the same security settings and thus can be combined together (e.g. multiple RegisterCommands merged)
     * Commands having a different command group use different security settings and thus cannot be combined together
     */
    private long currentSecuritySetCommandsGroup;

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

    public CommandRootImpl shallowCloneFor(OfflineDevice offlineDevice, CommandRoot.ServiceProvider serviceProvider, Set<ComCommandTypes> unnecessary) {
        CommandRootImpl clonedRoot = new CommandRootImpl(offlineDevice, this.getExecutionContext(), serviceProvider);
        CommandRootImpl.copyComCommands(this, clonedRoot, unnecessary);
        return clonedRoot;
    }

    @Override
    public void nextSecurityCommandGroup() {
        currentSecuritySetCommandsGroup++;
    }

    @Override
    public long getSecuritySetCommandGroupId() {
        return currentSecuritySetCommandsGroup;
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
    public Optional<ComCommand> getComCommand(ComCommandKey key) {
        return this.getExistingCommand(key);
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
    public LoadProfileCommand findOrCreateLoadProfileCommand(LoadProfilesTask loadProfilesTask, CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution) {
        ComCommandKey key = new ComCommandKey(ComCommandTypes.LOAD_PROFILE_COMMAND, comTaskExecution, this.currentSecuritySetCommandsGroup);
        Optional<ComCommand> existingCommand = this.getExistingCommand(key);
        if (existingCommand.isPresent()) {
            LoadProfileCommand comCommand = (LoadProfileCommand) existingCommand.get();
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
    public RegisterCommand findOrCreateRegisterCommand(final RegistersTask registersTask, final CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution) {
        ComCommandKey key = new ComCommandKey(ComCommandTypes.REGISTERS_COMMAND, comTaskExecution, this.currentSecuritySetCommandsGroup);
        return
            this.getExistingCommand(key)
                .map(RegisterCommand.class::cast)
                .orElseGet(() -> this.createRegisterCommand(registersTask, possibleCommandOwner, comTaskExecution));
    }

    private RegisterCommand createRegisterCommand(RegistersTask registersTask, CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution) {
        RegisterCommand registerCommand = new RegisterCommandImpl(registersTask, this.offlineDevice, this, comTaskExecution);
        possibleCommandOwner.addUniqueCommand(registerCommand, comTaskExecution);
        return registerCommand;
    }

    @Override
    public LogBooksCommand findOrCreateLogBooksCommand(final LogBooksTask logBooksTask, final CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution) {
        ComCommandKey key = new ComCommandKey(ComCommandTypes.LOGBOOKS_COMMAND, comTaskExecution, this.currentSecuritySetCommandsGroup);
        return this.getExistingCommand(key)
                .map(LogBooksCommand.class::cast)
                .orElseGet(() -> this.createLogBooksCommand(logBooksTask, possibleCommandOwner, comTaskExecution));
    }

    private LogBooksCommand createLogBooksCommand(LogBooksTask logBooksTask, CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution) {
        LogBooksCommand logBooksCommand = new LogBooksCommandImpl(logBooksTask, this.offlineDevice, this, comTaskExecution);
        possibleCommandOwner.addUniqueCommand(logBooksCommand, comTaskExecution);
        return logBooksCommand;
    }

    @Override
    public LegacyLoadProfileLogBooksCommand findOrCreateLegacyLoadProfileLogBooksCommand(LoadProfilesTask loadProfilesTask, LogBooksTask logBooksTask, CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution) {
        ComCommandKey key = new ComCommandKey(ComCommandTypes.LEGACY_LOAD_PROFILE_LOGBOOKS_COMMAND, comTaskExecution, this.currentSecuritySetCommandsGroup);
        return this.getExistingCommand(key)
                .map(LegacyLoadProfileLogBooksCommand.class::cast)
                .orElseGet(() -> this.createLegacyLoadProfileLogBooksCommand(loadProfilesTask, logBooksTask, possibleCommandOwner, comTaskExecution));
    }

    private LegacyLoadProfileLogBooksCommand createLegacyLoadProfileLogBooksCommand(LoadProfilesTask loadProfilesTask, LogBooksTask logBooksTask, CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution) {
        LegacyLoadProfileLogBooksCommand legacyCommand = new LegacyLoadProfileLogBooksCommandImpl(loadProfilesTask, logBooksTask, this.offlineDevice, this, comTaskExecution);
        possibleCommandOwner.addUniqueCommand(legacyCommand, comTaskExecution);
        return legacyCommand;
    }

    @Override
    public BasicCheckCommand findOrCreateBasicCheckCommand(BasicCheckTask basicCheckTask, CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution) {
        ComCommandKey key = new ComCommandKey(ComCommandTypes.BASIC_CHECK_COMMAND, comTaskExecution, this.currentSecuritySetCommandsGroup);
        return this.getExistingCommand(key)
                .map(BasicCheckCommand.class::cast)
                .orElseGet(() -> this.createBasicCheckCommand(basicCheckTask, possibleCommandOwner, comTaskExecution));
    }

    private BasicCheckCommand createBasicCheckCommand(BasicCheckTask basicCheckTask, CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution) {
        BasicCheckCommand basicCheckCommand = new BasicCheckCommandImpl(basicCheckTask, this, comTaskExecution);
        possibleCommandOwner.addUniqueCommand(basicCheckCommand, comTaskExecution);
        return basicCheckCommand;
    }

    @Override
    public StatusInformationCommand findOrCreateStatusInformationCommand(final CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution) {
        ComCommandKey key = new ComCommandKey(ComCommandTypes.STATUS_INFORMATION_COMMAND, comTaskExecution, this.currentSecuritySetCommandsGroup);
        return this.getExistingCommand(key)
                .map(StatusInformationCommand.class::cast)
                .orElseGet(() -> this.createStatusInformationCommand(possibleCommandOwner, comTaskExecution));
    }

    private StatusInformationCommand createStatusInformationCommand(CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution) {
        StatusInformationCommand statusInformationCommand = new StatusInformationCommandImpl(this.offlineDevice, this, comTaskExecution);
        possibleCommandOwner.addUniqueCommand(statusInformationCommand, comTaskExecution);
        return statusInformationCommand;
    }

    @Override
    public ReadRegistersCommand findOrCreateReadRegistersCommand(final CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution) {
        ComCommandKey key = new ComCommandKey(ComCommandTypes.READ_REGISTERS_COMMAND, comTaskExecution, this.currentSecuritySetCommandsGroup);
        return this.getExistingCommand(key)
                .map(ReadRegistersCommand.class::cast)
                .orElseGet(() -> this.createReadRegistersCommand(possibleCommandOwner, comTaskExecution));
    }

    private ReadRegistersCommand createReadRegistersCommand(CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution) {
        ReadRegistersCommandImpl readRegistersCommand = new ReadRegistersCommandImpl(possibleCommandOwner,  this);
        possibleCommandOwner.addUniqueCommand(readRegistersCommand, comTaskExecution);
        return readRegistersCommand;
    }

    @Override
    public ReadLogBooksCommand findorCreateReadLogBooksCommand(LogBooksCommand logBooksCommand, ComTaskExecution comTaskExecution) {
        ComCommandKey key = new ComCommandKey(ComCommandTypes.READ_LOGBOOKS_COMMAND, comTaskExecution, this.currentSecuritySetCommandsGroup);
        return this.getExistingCommand(key)
                .map(ReadLogBooksCommand.class::cast)
                .orElseGet(() -> this.createReadLogBooksCommand(logBooksCommand, comTaskExecution));
    }

    private ReadLogBooksCommand createReadLogBooksCommand(LogBooksCommand logBooksCommand, ComTaskExecution comTaskExecution) {
        ReadLogBooksCommandImpl readLogBooksCommand = new ReadLogBooksCommandImpl(logBooksCommand, this);
        logBooksCommand.addUniqueCommand(readLogBooksCommand, comTaskExecution);
        return readLogBooksCommand;
    }

    @Override
    public ClockCommand findOrCreateClockCommand(ClockTask clockTask, CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution) {
        ComCommandKey key = new ComCommandKey(ComCommandTypes.CLOCK_COMMAND, comTaskExecution, this.currentSecuritySetCommandsGroup);
        return this.getExistingCommand(key)
                .map(ClockCommand.class::cast)
                .orElseGet(() -> this.createClockCommand(clockTask, possibleCommandOwner, comTaskExecution));
    }

    private ClockCommand createClockCommand(ClockTask clockTask, CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution) {
        ClockCommand clockCommand = new ClockCommandImpl(clockTask, this, comTaskExecution);
        possibleCommandOwner.addUniqueCommand(clockCommand, comTaskExecution);
        return clockCommand;
    }

    @Override
    public MessagesCommand findOrCreateMessagesCommand(MessagesTask messagesTask, CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution) {
        ComCommandKey key = new ComCommandKey(ComCommandTypes.MESSAGES_COMMAND, comTaskExecution, this.currentSecuritySetCommandsGroup);
        return this.getExistingCommand(key)
                .map(MessagesCommand.class::cast)
                .orElseGet(() -> this.createMessagesCommand(messagesTask, possibleCommandOwner, comTaskExecution));
    }

    private MessagesCommand createMessagesCommand(MessagesTask messagesTask, CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution) {
        MessagesCommand messagesCommand = new MessagesCommandImpl(messagesTask, this.offlineDevice, this, comTaskExecution, this.serviceProvider.issueService(), this.serviceProvider.thesaurus());
        possibleCommandOwner.addUniqueCommand(messagesCommand, comTaskExecution);
        return messagesCommand;
    }

    @Override
    public TimeDifferenceCommand findOrCreateTimeDifferenceCommand(final CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution) {
        ComCommandKey key = new ComCommandKey(ComCommandTypes.TIME_DIFFERENCE_COMMAND, comTaskExecution, this.currentSecuritySetCommandsGroup);
        return this.getExistingCommand(key)
                .map(TimeDifferenceCommand.class::cast)
                .orElseGet(() -> this.createTimeDifferenceCommand(possibleCommandOwner, comTaskExecution));
    }

    private TimeDifferenceCommand createTimeDifferenceCommand(CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution) {
        TimeDifferenceCommand timeDifferenceCommand = new TimeDifferenceCommandImpl(this);
        possibleCommandOwner.addUniqueCommand(timeDifferenceCommand, comTaskExecution);
        return timeDifferenceCommand;
    }

    @Override
    public VerifyTimeDifferenceCommand findOrCreateVerifyTimeDifferenceCommand(BasicCheckCommand basicCheckCommand, CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution) {
        ComCommandKey key = new ComCommandKey(ComCommandTypes.VERIFY_TIME_DIFFERENCE_COMMAND, comTaskExecution, this.currentSecuritySetCommandsGroup);
        return this.getExistingCommand(key)
                .map(VerifyTimeDifferenceCommand.class::cast)
                .orElseGet(() -> this.createVerifyTimeDifferenceCommand(basicCheckCommand, possibleCommandOwner, comTaskExecution));
    }

    private VerifyTimeDifferenceCommand createVerifyTimeDifferenceCommand(BasicCheckCommand basicCheckCommand, CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution) {
        VerifyTimeDifferenceCommand verifyTimeDifferenceCommand = new VerifyTimeDifferenceCommandImpl(basicCheckCommand, this);
        possibleCommandOwner.addUniqueCommand(verifyTimeDifferenceCommand, comTaskExecution);
        return verifyTimeDifferenceCommand;
    }

    @Override
    public VerifyLoadProfilesCommand findOrCreateVerifyLoadProfileCommand(LoadProfileCommand loadProfileCommand, ComTaskExecution comTaskExecution) {
        ComCommandKey key = new ComCommandKey(ComCommandTypes.VERIFY_LOAD_PROFILE_COMMAND, comTaskExecution, this.currentSecuritySetCommandsGroup);
        return this.getExistingCommand(key)
                .map(VerifyLoadProfilesCommand.class::cast)
                .orElseGet(() -> this.createVerifyLoadProfilesCommand(loadProfileCommand, comTaskExecution));
    }

    private VerifyLoadProfilesCommand createVerifyLoadProfilesCommand(LoadProfileCommand loadProfileCommand, ComTaskExecution comTaskExecution) {
        VerifyLoadProfilesCommand verifyLoadProfilesCommand = new VerifyLoadProfilesCommandImpl(loadProfileCommand, this);
        loadProfileCommand.addUniqueCommand(verifyLoadProfilesCommand, comTaskExecution);
        return verifyLoadProfilesCommand;
    }

    @Override
    public ReadLoadProfileDataCommand findOrCreateReadLoadProfileDataCommand(LoadProfileCommand loadProfileCommand, ComTaskExecution comTaskExecution) {
        ComCommandKey key = new ComCommandKey(ComCommandTypes.READ_LOAD_PROFILE_COMMAND, comTaskExecution, this.currentSecuritySetCommandsGroup);
        return this.getExistingCommand(key)
                .map(ReadLoadProfileDataCommand.class::cast)
                .orElseGet(() -> this.createReadLoadProfileDataCommand(loadProfileCommand, comTaskExecution));
    }

    private ReadLoadProfileDataCommand createReadLoadProfileDataCommand(LoadProfileCommand loadProfileCommand, ComTaskExecution comTaskExecution) {
        ReadLoadProfileDataCommand readLoadProfileDataCommand = new ReadLoadProfileDataCommandImpl(loadProfileCommand, this);
        loadProfileCommand.addUniqueCommand(readLoadProfileDataCommand, comTaskExecution);
        return readLoadProfileDataCommand;
    }

    @Override
       public ReadLegacyLoadProfileLogBooksDataCommand findOrCreateReadLegacyLoadProfileLogBooksDataCommand(LegacyLoadProfileLogBooksCommand legacyLoadProfileLogBooksCommand, ComTaskExecution comTaskExecution) {
        ComCommandKey key = new ComCommandKey(ComCommandTypes.READ_LEGACY_LOAD_PROFILE_LOGBOOKS_COMMAND, comTaskExecution, this.currentSecuritySetCommandsGroup);
        return this.getExistingCommand(key)
                .map(ReadLegacyLoadProfileLogBooksDataCommand.class::cast)
                .orElseGet(() -> this.createReadLegacyLoadProfileLogBooksDataCommand(legacyLoadProfileLogBooksCommand, comTaskExecution));
    }

    private ReadLegacyLoadProfileLogBooksDataCommand createReadLegacyLoadProfileLogBooksDataCommand(LegacyLoadProfileLogBooksCommand legacyLoadProfileLogBooksCommand, ComTaskExecution comTaskExecution) {
        ReadLegacyLoadProfileLogBooksDataCommand readLegacyLoadProfileLogBooksDataCommand = new ReadLegacyLoadProfileLogBooksDataCommandImpl(legacyLoadProfileLogBooksCommand, this);
        legacyLoadProfileLogBooksCommand.addUniqueCommand(readLegacyLoadProfileLogBooksDataCommand, comTaskExecution);
        return readLegacyLoadProfileLogBooksDataCommand;
    }

    @Override
    public MarkIntervalsAsBadTimeCommand findOrCreateMarkIntervalsAsBadTimeCommand(final LoadProfileCommand loadProfileCommand, ComTaskExecution comTaskExecution) {
        ComCommandKey key = new ComCommandKey(ComCommandTypes.MARK_LOAD_PROFILES_AS_BAD_TIME, comTaskExecution, this.currentSecuritySetCommandsGroup);
        return this.getExistingCommand(key)
                .map(MarkIntervalsAsBadTimeCommand.class::cast)
                .orElseGet(() -> this. createMarkIntervalsAsBadTimeCommand(loadProfileCommand, comTaskExecution));
    }

    private MarkIntervalsAsBadTimeCommand createMarkIntervalsAsBadTimeCommand(LoadProfileCommand loadProfileCommand, ComTaskExecution comTaskExecution) {
        MarkIntervalsAsBadTimeCommand markIntervalsAsBadTimeCommand = new MarkIntervalsAsBadTimeCommandImpl(loadProfileCommand, this);
        loadProfileCommand.addUniqueCommand(markIntervalsAsBadTimeCommand, comTaskExecution);
        return markIntervalsAsBadTimeCommand;
    }

    @Override
    public CreateMeterEventsFromStatusFlagsCommand findOrCreateCreateMeterEventsFromStatusFlagsCommand(final LoadProfileCommand loadProfileCommand, ComTaskExecution comTaskExecution) {
        ComCommandKey key = new ComCommandKey(ComCommandTypes.CREATE_METER_EVENTS_IN_LOAD_PROFILE_FROM_STATUS_FLAGS, comTaskExecution, this.currentSecuritySetCommandsGroup);
        return this.getExistingCommand(key)
                .map(CreateMeterEventsFromStatusFlagsCommand.class::cast)
                .orElseGet(() -> this.createMeterEventsFromStatusFlagsCommand(loadProfileCommand, comTaskExecution));
    }

    private CreateMeterEventsFromStatusFlagsCommand createMeterEventsFromStatusFlagsCommand(LoadProfileCommand loadProfileCommand, ComTaskExecution comTaskExecution) {
        CreateMeterEventsFromStatusFlagsCommand createMeterEventsFromStatusFlagsCommand = new CreateMeterEventsFromStatusFlagsCommandImpl(loadProfileCommand, this);
        loadProfileCommand.addUniqueCommand(createMeterEventsFromStatusFlagsCommand, comTaskExecution);
        return createMeterEventsFromStatusFlagsCommand;
    }

    @Override
    public ForceClockCommand findOrCreateForceClockCommand(final ClockCommand clockCommand, ComTaskExecution comTaskExecution) {
        ComCommandKey key = new ComCommandKey(ComCommandTypes.FORCE_CLOCK_COMMAND, comTaskExecution, this.currentSecuritySetCommandsGroup);
        return this.getExistingCommand(key)
                .map(ForceClockCommand.class::cast)
                .orElseGet(() -> this.createForceClockCommand(clockCommand, comTaskExecution));
    }

    private ForceClockCommand createForceClockCommand(ClockCommand clockCommand, ComTaskExecution comTaskExecution) {
        ForceClockCommand forceClockCommand = new ForceClockCommandImpl(this);
        clockCommand.addUniqueCommand(forceClockCommand, comTaskExecution);
        return forceClockCommand;
    }

    @Override
    public SetClockCommand findOrCreateSetClockCommand(final ClockCommand clockCommand, ComTaskExecution comTaskExecution) {
        ComCommandKey key = new ComCommandKey(ComCommandTypes.SET_CLOCK_COMMAND, comTaskExecution, this.currentSecuritySetCommandsGroup);
        return this.getExistingCommand(key)
                .map(SetClockCommand.class::cast)
                .orElseGet(() -> this.createSetClockCommand(clockCommand, comTaskExecution));
    }

    private SetClockCommand createSetClockCommand(ClockCommand clockCommand, ComTaskExecution comTaskExecution) {
        SetClockCommand setClockCommand = new SetClockCommandImpl(clockCommand, this, comTaskExecution);
        clockCommand.addUniqueCommand(setClockCommand, comTaskExecution);
        return setClockCommand;
    }

    @Override
    public SynchronizeClockCommand findOrCreateSynchronizeClockCommand(final ClockCommand clockCommand, ComTaskExecution comTaskExecution) {
        ComCommandKey key = new ComCommandKey(ComCommandTypes.SYNCHRONIZE_CLOCK_COMMAND, comTaskExecution, this.currentSecuritySetCommandsGroup);
        return this.getExistingCommand(key)
                .map(SynchronizeClockCommand.class::cast)
                .orElseGet(() -> this.createSynchronizeClockCommand(clockCommand, comTaskExecution));
    }

    private SynchronizeClockCommand createSynchronizeClockCommand(ClockCommand clockCommand, ComTaskExecution comTaskExecution) {
        SynchronizeClockCommand synchronizeClockCommand = new SynchronizeClockCommandImpl(clockCommand, this, comTaskExecution);
        clockCommand.addUniqueCommand(synchronizeClockCommand, comTaskExecution);
        return synchronizeClockCommand;
    }

    @Override
    public TopologyCommand findOrCreateTopologyCommand(final TopologyTask topologyTask, final CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution) {
        ComCommandKey key = new ComCommandKey(ComCommandTypes.TOPOLOGY_COMMAND, comTaskExecution, this.currentSecuritySetCommandsGroup);
        return this.getExistingCommand(key)
                .map(TopologyCommand.class::cast)
                .orElseGet(() -> this.createTopologyCommand(topologyTask, possibleCommandOwner, comTaskExecution));
    }

    private TopologyCommand createTopologyCommand(TopologyTask topologyTask, CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution) {
        TopologyCommand topologyCommand = new TopologyCommandImpl(this, topologyTask.getTopologyAction(), this.offlineDevice, comTaskExecution);
        possibleCommandOwner.addUniqueCommand(topologyCommand, comTaskExecution);
        return topologyCommand;
    }

    @Override
    public FirmwareManagementCommand findOrCreateFirmwareCommand(FirmwareManagementTask firmwareManagementTask, CommandRoot possibleCommandOwner, ComTaskExecution comTaskExecution) {
        ComCommandKey key = new ComCommandKey(ComCommandTypes.FIRMWARE_COMMAND, comTaskExecution, this.currentSecuritySetCommandsGroup);
        return this.getExistingCommand(key)
                .map(FirmwareManagementCommand.class::cast)
                .orElseGet(() -> this.createFirmwareUpgradeCommand(firmwareManagementTask, possibleCommandOwner, comTaskExecution));
    }

    private FirmwareManagementCommand createFirmwareUpgradeCommand(FirmwareManagementTask firmwareManagementTask, CommandRoot possibleCommandOwner, ComTaskExecution comTaskExecution) {
        FirmwareManagementCommandImpl firmwareUpgradeCommand = new FirmwareManagementCommandImpl(this, firmwareManagementTask, comTaskExecution, this.offlineDevice);
        possibleCommandOwner.addUniqueCommand(firmwareUpgradeCommand, comTaskExecution);
        return firmwareUpgradeCommand;
    }

    @Override
    public VerifySerialNumberCommand findOrCreateVerifySerialNumberCommand(final BasicCheckCommand comCommands, ComTaskExecution comTaskExecution) {
        ComCommandKey key = new ComCommandKey(ComCommandTypes.VERIFY_SERIAL_NUMBER_COMMAND, comTaskExecution, this.currentSecuritySetCommandsGroup);
        return this.getExistingCommand(key)
                .map(VerifySerialNumberCommand.class::cast)
                .orElseGet(() -> this.createVerifySerialNumberCommand(comCommands, comTaskExecution));
    }

    private VerifySerialNumberCommand createVerifySerialNumberCommand(BasicCheckCommand comCommands, ComTaskExecution comTaskExecution) {
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
    public boolean containsDaisyChainedLogOnCommandFor(ComTaskExecution comTaskExecution) {
        ComCommandKey key = new ComCommandKey(ComCommandTypes.DAISY_CHAINED_LOGON, comTaskExecution.getDevice());
        return this.getExistingCommand(key).isPresent();
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
        if (this.preparedComTaskExecution != null) {
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
    public ComCommandType getCommandType() {
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