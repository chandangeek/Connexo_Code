package com.energyict.mdc.engine.impl.commands.collect;

import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.LogBookService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.store.core.ComTaskExecutionComCommand;
import com.energyict.mdc.engine.impl.core.CreateComTaskExecutionSessionTask;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.core.JobExecution;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.tasks.BasicCheckTask;
import com.energyict.mdc.tasks.ClockTask;
import com.energyict.mdc.tasks.FirmwareManagementTask;
import com.energyict.mdc.tasks.LoadProfilesTask;
import com.energyict.mdc.tasks.LogBooksTask;
import com.energyict.mdc.tasks.MessagesTask;
import com.energyict.mdc.tasks.RegistersTask;
import com.energyict.mdc.tasks.TopologyTask;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.transaction.TransactionService;

import java.time.Clock;

/**
 * A CommandRoot contains all {@link ComCommand ComCommands} which are to be executed
 * for a single logical entity.
 * A {@link CommandRoot} can only contain each {@link ComCommandTypes ComCommandType} once, otherwise they should
 * be merged together.
 *
 * @author gna
 * @since 10/05/12 - 11:58
 */
public interface CommandRoot extends CompositeComCommand {

    public interface ServiceProvider {

        public IssueService issueService();

        public Clock clock();

        public Thesaurus thesaurus();

        public DeviceService deviceService();

        public MdcReadingTypeUtilService mdcReadingTypeUtilService();

        public TransactionService transactionService();

        public IdentificationService identificationService();

        public MeteringService meteringService();

    }

    /**
     * Gets the {@link com.energyict.mdc.engine.impl.core.ExecutionContext} in which
     * all the {@link ComCommand}s are running.
     *
     * @return The ExecutionContext
     */
    public ExecutionContext getExecutionContext();

    /**
     * Gets the requested ComCommand from the command list based on the given type.
     *
     * @param commandType the commandType of the Command to return
     * @return the requested command
     */
    public ComCommand getComCommand(final ComCommandTypes commandType);

    /**
     * Gets or creates the {@link ComTaskExecutionComCommand} that contains all the {@link ComCommand}s
     * that relate the to specified {@link ComTaskExecution}.
     *
     * @param comTaskExecution The ComTaskExecution
     * @return The ComTaskExecutionComCommand
     */
    public ComTaskExecutionComCommand getComTaskRoot(ComTaskExecution comTaskExecution);

    /**
     * @param loadProfilesTask     the task for which this command is created for
     * @param possibleCommandOwner the possible owner of this command if it does not exist yet
     * @param comTaskExecution     the ComTaskExecution that drives this ComCommand
     * @return the {@link LoadProfileCommand} in this {@link CommandRoot}
     */
    public LoadProfileCommand getLoadProfileCommand(final LoadProfilesTask loadProfilesTask, final CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution);

    /**
     * @param registersTask        the task for which this command is created for
     * @param possibleCommandOwner the possible owner of this command if it does not exist yet
     * @param comTaskExecution
     * @return the {@link RegisterCommand} in this {@link CommandRoot}
     */
    public RegisterCommand getRegisterCommand(final RegistersTask registersTask, final CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution);

    /**
     * @param logBooksTask         the task for which this command is created for
     * @param possibleCommandOwner the possible owner of this command if it does not exist yet
     * @param comTaskExecution     the ComTaskExecution that drives this ComCommand
     * @return the {@link LogBooksCommand} in this {@link CommandRoot}
     */
    public LogBooksCommand getLogBooksCommand(final LogBooksTask logBooksTask, final CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution);

    /**
     * @param loadProfilesTask     the loadProfilesTask for which this command is created for. Use NULL if there is no loadProfilesTask for this command.
     * @param logBooksTask         the logBooksTask for which this command is created for. Use NULL if there is no logBooksTask for this command.
     * @param possibleCommandOwner the possible owner of this command if it does not exist yet
     * @param comTaskExecution     the ComTaskExecution that drives this ComCommand
     * @return the {@link LegacyLoadProfileLogBooksCommand} in this {@link CommandRoot}
     */
    public LegacyLoadProfileLogBooksCommand getLegacyLoadProfileLogBooksCommand(final LoadProfilesTask loadProfilesTask, final LogBooksTask logBooksTask, final CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution);

    /**
     * @param basicCheckTask       the task fro which this command is created for
     * @param possibleCommandOwner the possible owner of this command if it does not exist yet
     * @param comTaskExecution     the ComTaskExecution that drives this ComCommand
     * @return the {@link BasicCheckCommand} in this {@link CommandRoot}
     */
    public BasicCheckCommand getBasicCheckCommand(final BasicCheckTask basicCheckTask, final CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution);

    /**
     * @param possibleCommandOwner the possible owner of this command if it does not exist yet
     * @param comTaskExecution     the ComTaskExecution that drives this ComCommand
     * @return the {@link StatusInformationCommand} in this {@link CommandRoot}
     */
    public StatusInformationCommand getStatusInformationCommand(final CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution);

    /**
     * This command can actually perform the reading of the registers of a device
     *
     * @param possibleCommandOwner the possible owner of this command if it does not exist yet
     * @param comTaskExecution     the ComTaskExecution that drives this ComCommand
     * @return the {@link ReadRegistersCommand} in this {@link CommandRoot}
     */
    public ReadRegistersCommand getReadRegistersCommand(final CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution);

    /**
     * This command can actually perform the reading of the {@link com.energyict.mdc.protocol.api.device.BaseLogBook}s of a device
     *
     * @param logBooksCommand  the {@link LogBooksCommand} that will own this command.
     * @param comTaskExecution the ComTaskExecution that drives this ComCommand
     * @return the {@link ReadLogBooksCommand} in this {@link CommandRoot}
     */
    public ReadLogBooksCommand getReadLogBooksCommand(final LogBooksCommand logBooksCommand, ComTaskExecution comTaskExecution);

    /**
     * @param clockTask            the task for which this command is created for
     * @param possibleCommandOwner the possible owner of this command if it does not exist yet
     * @param comTaskExecution     the ComTaskExecution that drives this ComCommand
     * @return the {@link ClockCommand} in this {@link CommandRoot}
     */
    public ClockCommand getClockCommand(final ClockTask clockTask, final CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution);

    /**
     *
     * @param messagesTask         the task for which this command is created for
     * @param possibleCommandOwner the possible owner of this command if it does not exist yet
     * @param comTaskExecution  the ComTaskExecution that drives this ComCommand
     * @return the {@link MessagesCommand} in this {@link CommandRoot}
     */
    public MessagesCommand getMessagesCommand(final MessagesTask messagesTask, final CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution);

    /**
     * @param possibleCommandOwner the possible owner of this command if it does not exist yet
     * @param comTaskExecution     the ComTaskExecution that drives this ComCommand
     * @return the {@link TimeDifferenceCommand} in this {@link CommandRoot}
     */
    public TimeDifferenceCommand getTimeDifferenceCommand(final CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution);

    /**
     * @param basicCheckCommand    the basicCheckCommand which will own this VerifyTimeDifferenceCommand
     * @param possibleCommandOwner the possible owner of this command if it does not exist yet
     * @param comTaskExecution     the ComTaskExecution that drives this ComCommand
     * @return the {@link VerifyTimeDifferenceCommand} in this {@link CommandRoot}
     */
    public VerifyTimeDifferenceCommand getVerifyTimeDifferenceCommand(final BasicCheckCommand basicCheckCommand, final CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution);

    /**
     * @param loadProfileCommand the LoadProfileCommand that will own this VerifyLoadProfileCommand
     * @param comTaskExecution   the ComTaskExecution that drives this ComCommand
     * @return the VerifyLoadProfilesCommandImpl in this {@link CommandRoot}
     */
    public VerifyLoadProfilesCommand getVerifyLoadProfileCommand(final LoadProfileCommand loadProfileCommand, ComTaskExecution comTaskExecution);

    /**
     * @param loadProfileCommand the LoadProfileCommand that will own this ReadLoadProfileDataCommand
     * @param comTaskExecution   the ComTaskExecution that drives this ComCommand
     * @return the ReadLoadProfileDataCommandImpl in this {@link CommandRoot}
     */
    public ReadLoadProfileDataCommand getReadLoadProfileDataCommand(final LoadProfileCommand loadProfileCommand, ComTaskExecution comTaskExecution);

    /**
     * @param legacyLoadProfileLogBooksCommand the LegacyLoadProfileLogBooksCommand that will own this ReadLegacyLoadProfileLogBooksDataCommand
     * @param comTaskExecution                 the ComTaskExecution that drives this ComCommand
     * @return the {@link ReadLegacyLoadProfileLogBooksDataCommand} in this {@link CommandRoot}
     */
    public ReadLegacyLoadProfileLogBooksDataCommand getReadLegacyLoadProfileLogBooksDataCommand(final LegacyLoadProfileLogBooksCommand legacyLoadProfileLogBooksCommand, ComTaskExecution comTaskExecution);

    /**
     * @param loadProfileCommand the LoadProfileCommand that will own this MarkIntervalsAsBadTimeCommand
     * @param comTaskExecution   the ComTaskExecution that drives this ComCommand
     * @return the MarkIntervalsAsBadTimeCommandImpl in this {@link CommandRoot}
     */
    public MarkIntervalsAsBadTimeCommand getMarkIntervalsAsBadTimeCommand(final LoadProfileCommand loadProfileCommand, ComTaskExecution comTaskExecution);

    /**
     * @param loadProfileCommand the LoadProfileCommand that will own this CreateMeterEventsFromStatusFlagsCommand
     * @param comTaskExecution   the ComTaskExecution that drives this ComCommand
     * @return the {@link CreateMeterEventsFromStatusFlagsCommand} in this {@link CommandRoot}
     */
    public CreateMeterEventsFromStatusFlagsCommand getCreateMeterEventsFromStatusFlagsCommand(final LoadProfileCommand loadProfileCommand, ComTaskExecution comTaskExecution);

    /**
     * @param clockCommand     the {@link ClockCommand} that will own this ForceClockCommand
     * @param comTaskExecution the ComTaskExecution that drives this ComCommand
     * @return the {@link ForceClockCommand} in this {@link CommandRoot}
     */
    public ComCommand getForceClockCommand(final ClockCommand clockCommand, ComTaskExecution comTaskExecution);

    /**
     * @param clockCommand     the {@link ClockCommand} that will own this SetClockCommand
     * @param comTaskExecution the ComTaskExecution that drives this ComCommand
     * @return the {@link SetClockCommand} in this {@link CommandRoot}
     */
    public SetClockCommand getSetClockCommand(final ClockCommand clockCommand, ComTaskExecution comTaskExecution);

    /**
     * @param clockCommand     the {@link ClockCommand} that will own this SynchronizeClockCommand
     * @param comTaskExecution the ComTaskExecution that drives this ComCommand
     * @return the {@link SynchronizeClockCommand} in this {@link CommandRoot}
     */
    public SynchronizeClockCommand getSynchronizeClockCommand(final ClockCommand clockCommand, ComTaskExecution comTaskExecution);

    /**
     * @param topologyTask         the task for which this command is created for
     * @param possibleCommandOwner the possible owner of this command if it does not exist yet
     * @param comTaskExecution     the ComTaskExecution that drives this ComCommand
     * @return the {@link TopologyCommand} in this {@link CommandRoot}
     */
    public TopologyCommand getTopologyCommand(final TopologyTask topologyTask, final CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution);

    /**
     * @param comCommands      the BasicCheckCommand that will own this VerifySerialNumberCommand
     * @param comTaskExecution the ComTaskExecution that drives this ComCommand
     * @return the {@link VerifySerialNumberCommand} in this {@link CommandRoot}
     */
    public VerifySerialNumberCommand getVerifySerialNumberCommand(final BasicCheckCommand comCommands, ComTaskExecution comTaskExecution);

    /**
     * @param protocolTask the task for which this command is created
     * @param possibleCommandOwner the possible owner of this command if it does not exist yet
     * @param comTaskExecution the ComTaskExecution that drives this ComCommand
     * @return the newly created CreateComTaskExecutionSessionCommand
     */
    public CreateComTaskExecutionSessionCommand createComTaskSessionTask(CreateComTaskExecutionSessionTask protocolTask, CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution);

    public FirmwareManagementCommand getFirmwareCommand(FirmwareManagementTask firmwareManagementTask, CommandRoot possibleCommandOwner, ComTaskExecution comTaskExecution);

    /**
     * Executes the ComCommands related to the given preparedComTaskExecution
     *
     * @param preparedComTaskExecution the given PreparedComTaskExecution
     * @param executionContext         the executionContext
     */
    public void executeFor(JobExecution.PreparedComTaskExecution preparedComTaskExecution, ExecutionContext executionContext);

    /**
     * Gets the ServiceProvider which collects the Services which are required for the creation/execution of ComCommands
     *
     * @return the ServiceProvider
     */
    public ServiceProvider getServiceProvider();

    /**
     * Indicates if any exceptions (during storing of underlying collected data) should be exposed to the DeviceCommandExecutor
     */
    public boolean isExposeStoringException();

}
