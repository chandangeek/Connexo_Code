package com.energyict.mdc.engine.impl.commands.collect;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.device.data.DeviceService;
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

import java.time.Clock;
import java.util.Optional;

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

    interface ServiceProvider {

        IssueService issueService();

        Clock clock();

        Thesaurus thesaurus();

        DeviceService deviceService();

        MdcReadingTypeUtilService mdcReadingTypeUtilService();

        TransactionService transactionService();

        IdentificationService identificationService();

        MeteringService meteringService();

    }

    /**
     * Gets the {@link com.energyict.mdc.engine.impl.core.ExecutionContext} in which
     * all the {@link ComCommand}s are running.
     *
     * @return The ExecutionContext
     */
    ExecutionContext getExecutionContext();

    /**
     * Uses the next security group for commands that need the same security settings
     * but other settings than used for previously added commands.<br/>
     * As a result, these commands <b>cannot</b> be grouped with any previously created commands, e.g.:
     * RegisterCommands withing the same 'group' can be merged together/combined, but when belonging to different groups, merging is not possible.
     */
    void nextSecurityCommandGroup();

    /**
     * Getter for the current 'securitySet command group'<br/>
     * Commands having the same command group use the same security settings and thus can be combined together (e.g. multiple RegisterCommands merged)<br/>
     * Commands having a different command group use different security settings and thus cannot be combined together
     */
    long getSecuritySetCommandGroupId();

    /**
     * Gets the requested ComCommand from the command list based for the given {@link ComCommandKey}.
     *
     * @param key The ComCommandKey
     * @return the requested command or Optional.empty() if no such ComCommand exists
     */
    Optional<ComCommand> getComCommand(ComCommandKey key);

    /**
     * Gets or creates the {@link ComTaskExecutionComCommand} that contains all the {@link ComCommand}s
     * that relate the to specified {@link ComTaskExecution}.
     *
     * @param comTaskExecution The ComTaskExecution
     * @return The ComTaskExecutionComCommand
     */
    ComTaskExecutionComCommand getComTaskRoot(ComTaskExecution comTaskExecution);

    /**
     * @param loadProfilesTask     the task for which this command is created for
     * @param possibleCommandOwner the possible owner of this command if it does not exist yet
     * @param comTaskExecution     the ComTaskExecution that drives this ComCommand
     * @return the {@link LoadProfileCommand} in this {@link CommandRoot}
     */
    LoadProfileCommand findOrCreateLoadProfileCommand(LoadProfilesTask loadProfilesTask, CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution);

    /**
     * @param registersTask        the task for which this command is created for
     * @param possibleCommandOwner the possible owner of this command if it does not exist yet
     * @param comTaskExecution
     * @return the {@link RegisterCommand} in this {@link CommandRoot}
     */
    RegisterCommand findOrCreateRegisterCommand(RegistersTask registersTask, CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution);

    /**
     * @param logBooksTask         the task for which this command is created for
     * @param possibleCommandOwner the possible owner of this command if it does not exist yet
     * @param comTaskExecution     the ComTaskExecution that drives this ComCommand
     * @return the {@link LogBooksCommand} in this {@link CommandRoot}
     */
    LogBooksCommand findOrCreateLogBooksCommand(LogBooksTask logBooksTask, CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution);

    /**
     * @param loadProfilesTask     the loadProfilesTask for which this command is created for. Use NULL if there is no loadProfilesTask for this command.
     * @param logBooksTask         the logBooksTask for which this command is created for. Use NULL if there is no logBooksTask for this command.
     * @param possibleCommandOwner the possible owner of this command if it does not exist yet
     * @param comTaskExecution     the ComTaskExecution that drives this ComCommand
     * @return the {@link LegacyLoadProfileLogBooksCommand} in this {@link CommandRoot}
     */
    LegacyLoadProfileLogBooksCommand findOrCreateLegacyLoadProfileLogBooksCommand(LoadProfilesTask loadProfilesTask, LogBooksTask logBooksTask, CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution);

    /**
     * @param basicCheckTask       the task fro which this command is created for
     * @param possibleCommandOwner the possible owner of this command if it does not exist yet
     * @param comTaskExecution     the ComTaskExecution that drives this ComCommand
     * @return the {@link BasicCheckCommand} in this {@link CommandRoot}
     */
    BasicCheckCommand findOrCreateBasicCheckCommand(BasicCheckTask basicCheckTask, CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution);

    /**
     * @param possibleCommandOwner the possible owner of this command if it does not exist yet
     * @param comTaskExecution     the ComTaskExecution that drives this ComCommand
     * @return the {@link StatusInformationCommand} in this {@link CommandRoot}
     */
    StatusInformationCommand findOrCreateStatusInformationCommand(CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution);

    /**
     * This command can actually perform the reading of the registers of a device
     *
     * @param possibleCommandOwner the possible owner of this command if it does not exist yet
     * @param comTaskExecution     the ComTaskExecution that drives this ComCommand
     * @return the {@link ReadRegistersCommand} in this {@link CommandRoot}
     */
    ReadRegistersCommand findOrCreateReadRegistersCommand(CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution);

    /**
     * This command can actually perform the reading of the {@link com.energyict.mdc.protocol.api.device.BaseLogBook}s of a device
     *
     * @param logBooksCommand  the {@link LogBooksCommand} that will own this command.
     * @param comTaskExecution the ComTaskExecution that drives this ComCommand
     * @return the {@link ReadLogBooksCommand} in this {@link CommandRoot}
     */
    ReadLogBooksCommand findorCreateReadLogBooksCommand(LogBooksCommand logBooksCommand, ComTaskExecution comTaskExecution);

    /**
     * @param clockTask            the task for which this command is created for
     * @param possibleCommandOwner the possible owner of this command if it does not exist yet
     * @param comTaskExecution     the ComTaskExecution that drives this ComCommand
     * @return the {@link ClockCommand} in this {@link CommandRoot}
     */
    ClockCommand findOrCreateClockCommand(ClockTask clockTask, CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution);

    /**
     *
     * @param messagesTask         the task for which this command is created for
     * @param possibleCommandOwner the possible owner of this command if it does not exist yet
     * @param comTaskExecution  the ComTaskExecution that drives this ComCommand
     * @return the {@link MessagesCommand} in this {@link CommandRoot}
     */
    MessagesCommand findOrCreateMessagesCommand(MessagesTask messagesTask, CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution);

    /**
     * @param possibleCommandOwner the possible owner of this command if it does not exist yet
     * @param comTaskExecution     the ComTaskExecution that drives this ComCommand
     * @return the {@link TimeDifferenceCommand} in this {@link CommandRoot}
     */
    TimeDifferenceCommand findOrCreateTimeDifferenceCommand(CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution);

    /**
     * @param basicCheckCommand    the basicCheckCommand which will own this VerifyTimeDifferenceCommand
     * @param possibleCommandOwner the possible owner of this command if it does not exist yet
     * @param comTaskExecution     the ComTaskExecution that drives this ComCommand
     * @return the {@link VerifyTimeDifferenceCommand} in this {@link CommandRoot}
     */
    VerifyTimeDifferenceCommand findOrCreateVerifyTimeDifferenceCommand(BasicCheckCommand basicCheckCommand, CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution);

    /**
     * @param loadProfileCommand the LoadProfileCommand that will own this VerifyLoadProfileCommand
     * @param comTaskExecution   the ComTaskExecution that drives this ComCommand
     * @return the VerifyLoadProfilesCommandImpl in this {@link CommandRoot}
     */
    VerifyLoadProfilesCommand findOrCreateVerifyLoadProfileCommand(LoadProfileCommand loadProfileCommand, ComTaskExecution comTaskExecution);

    /**
     * @param loadProfileCommand the LoadProfileCommand that will own this ReadLoadProfileDataCommand
     * @param comTaskExecution   the ComTaskExecution that drives this ComCommand
     * @return the ReadLoadProfileDataCommandImpl in this {@link CommandRoot}
     */
    ReadLoadProfileDataCommand findOrCreateReadLoadProfileDataCommand(LoadProfileCommand loadProfileCommand, ComTaskExecution comTaskExecution);

    /**
     * @param legacyLoadProfileLogBooksCommand the LegacyLoadProfileLogBooksCommand that will own this ReadLegacyLoadProfileLogBooksDataCommand
     * @param comTaskExecution                 the ComTaskExecution that drives this ComCommand
     * @return the {@link ReadLegacyLoadProfileLogBooksDataCommand} in this {@link CommandRoot}
     */
    ReadLegacyLoadProfileLogBooksDataCommand findOrCreateReadLegacyLoadProfileLogBooksDataCommand(LegacyLoadProfileLogBooksCommand legacyLoadProfileLogBooksCommand, ComTaskExecution comTaskExecution);

    /**
     * @param loadProfileCommand the LoadProfileCommand that will own this MarkIntervalsAsBadTimeCommand
     * @param comTaskExecution   the ComTaskExecution that drives this ComCommand
     * @return the MarkIntervalsAsBadTimeCommandImpl in this {@link CommandRoot}
     */
    MarkIntervalsAsBadTimeCommand findOrCreateMarkIntervalsAsBadTimeCommand(LoadProfileCommand loadProfileCommand, ComTaskExecution comTaskExecution);

    /**
     * @param loadProfileCommand the LoadProfileCommand that will own this CreateMeterEventsFromStatusFlagsCommand
     * @param comTaskExecution   the ComTaskExecution that drives this ComCommand
     * @return the {@link CreateMeterEventsFromStatusFlagsCommand} in this {@link CommandRoot}
     */
    CreateMeterEventsFromStatusFlagsCommand findOrCreateCreateMeterEventsFromStatusFlagsCommand(LoadProfileCommand loadProfileCommand, ComTaskExecution comTaskExecution);

    /**
     * @param clockCommand     the {@link ClockCommand} that will own this ForceClockCommand
     * @param comTaskExecution the ComTaskExecution that drives this ComCommand
     * @return the {@link ForceClockCommand} in this {@link CommandRoot}
     */
    ComCommand findOrCreateForceClockCommand(ClockCommand clockCommand, ComTaskExecution comTaskExecution);

    /**
     * @param clockCommand     the {@link ClockCommand} that will own this SetClockCommand
     * @param comTaskExecution the ComTaskExecution that drives this ComCommand
     * @return the {@link SetClockCommand} in this {@link CommandRoot}
     */
    SetClockCommand findOrCreateSetClockCommand(ClockCommand clockCommand, ComTaskExecution comTaskExecution);

    /**
     * @param clockCommand     the {@link ClockCommand} that will own this SynchronizeClockCommand
     * @param comTaskExecution the ComTaskExecution that drives this ComCommand
     * @return the {@link SynchronizeClockCommand} in this {@link CommandRoot}
     */
    SynchronizeClockCommand findOrCreateSynchronizeClockCommand(ClockCommand clockCommand, ComTaskExecution comTaskExecution);

    /**
     * @param topologyTask         the task for which this command is created for
     * @param possibleCommandOwner the possible owner of this command if it does not exist yet
     * @param comTaskExecution     the ComTaskExecution that drives this ComCommand
     * @return the {@link TopologyCommand} in this {@link CommandRoot}
     */
    TopologyCommand findOrCreateTopologyCommand(TopologyTask topologyTask, CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution);

    /**
     * @param comCommands      the BasicCheckCommand that will own this VerifySerialNumberCommand
     * @param comTaskExecution the ComTaskExecution that drives this ComCommand
     * @return the {@link VerifySerialNumberCommand} in this {@link CommandRoot}
     */
    VerifySerialNumberCommand findOrCreateVerifySerialNumberCommand(BasicCheckCommand comCommands, ComTaskExecution comTaskExecution);

    /**
     * @param protocolTask the task for which this command is created
     * @param possibleCommandOwner the possible owner of this command if it does not exist yet
     * @param comTaskExecution the ComTaskExecution that drives this ComCommand
     * @return the newly created CreateComTaskExecutionSessionCommand
     */
    CreateComTaskExecutionSessionCommand createComTaskSessionTask(CreateComTaskExecutionSessionTask protocolTask, CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution);

    FirmwareManagementCommand findOrCreateFirmwareCommand(FirmwareManagementTask firmwareManagementTask, CommandRoot possibleCommandOwner, ComTaskExecution comTaskExecution);

    /**
     * Executes the ComCommands related to the given preparedComTaskExecution
     *
     * @param preparedComTaskExecution the given PreparedComTaskExecution
     * @param executionContext         the executionContext
     */
    void executeFor(JobExecution.PreparedComTaskExecution preparedComTaskExecution, ExecutionContext executionContext);

    /**
     * Gets the ServiceProvider which collects the Services which are required for the creation/execution of ComCommands
     *
     * @return the ServiceProvider
     */
    ServiceProvider getServiceProvider();

    /**
     * Indicates if any exceptions (during storing of underlying collected data) should be exposed to the DeviceCommandExecutor
     */
    boolean isExposeStoringException();

}
