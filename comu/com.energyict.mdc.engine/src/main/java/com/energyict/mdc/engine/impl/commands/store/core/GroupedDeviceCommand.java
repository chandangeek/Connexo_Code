package com.energyict.mdc.engine.impl.commands.store.core;

import com.elster.jupiter.orm.MacException;
import com.energyict.mdc.common.comserver.logging.CanProvideDescriptionTitle;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.impl.commands.MessageSeeds;
import com.energyict.mdc.engine.impl.commands.collect.*;
import com.energyict.mdc.engine.impl.commands.store.common.DeviceProtocolInitializeCommand;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.*;
import com.energyict.mdc.engine.impl.core.ComPortRelatedComChannel;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.issues.Problem;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.data.CollectedData;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.tasks.*;

import java.util.*;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 11/07/2016 - 14:12
 */
public class GroupedDeviceCommand implements Iterable<ComTaskExecutionComCommandImpl>, CanProvideDescriptionTitle {

    private final OfflineDevice offlineDevice;
    private final DeviceProtocol deviceProtocol;
    private final DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet;

    /**
     * If a certain ComCommand fails with a fatal error, the protocol session to this device should stop.
     */
    private boolean skipOtherComTaskExecutions = false;

    /**
     * Keeps track of the ComTaskExecutions that need to be performed for this GroupedDeviceCommand
     */
    private Map<ComTaskExecution, ComTaskExecutionComCommandImpl> comTaskExecutionComCommands = new LinkedHashMap<>();
    private CommandRoot commandRoot;
    private ComCommand basicCheckCommand;

    public GroupedDeviceCommand(CommandRoot commandRoot, OfflineDevice offlineDevice, DeviceProtocol deviceProtocol, DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        this.commandRoot = commandRoot;
        this.offlineDevice = offlineDevice;
        this.deviceProtocol = deviceProtocol;
        this.deviceProtocolSecurityPropertySet = deviceProtocolSecurityPropertySet;
    }

    public void perform(ExecutionContext executionContext) {
        if (!commandRoot.hasConnectionSetupError()) {
            performAfterConnectionSetup(executionContext);
        } else {
            this.executeForConnectionSetupError(executionContext);
        }
    }

    public void performAfterConnectionSetup(ExecutionContext executionContext) {
        if (!commandRoot.hasConnectionErrorOccurred()) {
            this.execute(executionContext);
        } else {
            this.executeForConnectionError(executionContext);
        }
    }

    public void execute(ExecutionContext executionContext) {
        basicCheckCommand = getBasicCheckCommandIfPresent(); // initialize it if present
        for (ComTaskExecutionComCommandImpl comTaskExecutionComCommand : comTaskExecutionComCommands.values()) {
            try {
                executionContext.start(comTaskExecutionComCommand);
                if (hasBasicCheckFailedForThisGroupedDeviceCommand()) {
                    Problem problem = getServiceProvider().issueService().newProblem(comTaskExecutionComCommand, MessageSeeds.NOT_EXECUTED_DUE_TO_BASIC_CHECK_FAILURE);
                    comTaskExecutionComCommand.addIssue(problem, CompletionCode.NotExecuted);
                } else if (commandRoot.hasConnectionErrorOccurred()) {
                    Problem problem = getServiceProvider().issueService().newProblem(comTaskExecutionComCommand, MessageSeeds.NOT_EXECUTED_DUE_TO_CONNECTION_ERROR);
                    comTaskExecutionComCommand.addIssue(problem, CompletionCode.NotExecuted);
                } else if (getCompletionCode().equals(CompletionCode.InitError)) {
                    Problem problem = getServiceProvider().issueService().newProblem(comTaskExecutionComCommand, MessageSeeds.NOT_EXECUTED_DUE_TO_INIT_ERROR);
                    comTaskExecutionComCommand.addIssue(problem, CompletionCode.NotExecuted);
                } else if (skipOtherComTaskExecutions) {
                    Problem problem = getServiceProvider().issueService().newProblem(comTaskExecutionComCommand, MessageSeeds.NOT_EXECUTED_DUE_TO_OTHER_COMTASK_EXECUTION_ERROR);
                    comTaskExecutionComCommand.addIssue(problem, CompletionCode.NotExecuted);
                }
                comTaskExecutionComCommand.execute(deviceProtocol, executionContext);
            } finally {
                ComTaskExecutionSession.SuccessIndicator successIndicator;
                if (comTaskExecutionComCommand.getCompletionCode().equals(CompletionCode.NotExecuted)) {
                    comTaskExecutionComCommand.setExecutionState(BasicComCommandBehavior.ExecutionState.NOT_EXECUTED);
                    successIndicator = ComTaskExecutionSession.SuccessIndicator.Failure;
                } else if (comTaskExecutionComCommand.getProblems().size() > 0) {
                    comTaskExecutionComCommand.setExecutionState(BasicComCommandBehavior.ExecutionState.FAILED);
                    executionContext.comTaskExecutionFailed(comTaskExecutionComCommand.getComTaskExecution());
                    successIndicator = ComTaskExecutionSession.SuccessIndicator.Failure;
                } else {
                    comTaskExecutionComCommand.setExecutionState(BasicComCommandBehavior.ExecutionState.SUCCESSFULLY_EXECUTED);
                    successIndicator = ComTaskExecutionSession.SuccessIndicator.Success;
                }
                executionContext.completeExecutedComTask(comTaskExecutionComCommand.getComTaskExecution(), successIndicator);
            }
        }
    }

    private CommandRoot.ServiceProvider getServiceProvider() {
        return getCommandRoot().getServiceProvider();
    }

    public void skipOtherComTaskExecutions() {
        this.skipOtherComTaskExecutions = true;
    }

    /**
     * Checks within one ComTaskExecutionComCommand if the given ComCommand can still be executed.
     */
    boolean areWeAllowedToPerformTheCommand(ComCommand comCommand) {
        switch (getCompletionCode()) {
            case ConfigurationError: {
                return isItALogOffCommand(comCommand) || !hasBasicCheckFailedForThisGroupedDeviceCommand();
            }
            case TimeError:
                return isItALogOffCommand(comCommand) || isItTerminateOrUpdateCache(comCommand);
            case InitError:             // intentional fallthrough
            case UnexpectedError:       // intentional fallthrough
            case TimeoutError:          // intentional fallthrough
            case ConnectionError:
                return isItTerminateOrUpdateCache(comCommand);
            default:
                return true;    // all others are ok
        }
    }

    private boolean isItTerminateOrUpdateCache(ComCommand comCommand) {
        return comCommand.getCommandType().equals(ComCommandTypes.DEVICE_PROTOCOL_TERMINATE) || comCommand.getCommandType().equals(ComCommandTypes.DEVICE_PROTOCOL_UPDATE_CACHE_COMMAND);
    }

    public boolean hasBasicCheckFailedForThisGroupedDeviceCommand() {
        return basicCheckCommand != null &&
                (basicCheckCommand.getCompletionCode().equals(CompletionCode.ConfigurationError) || basicCheckCommand.getCompletionCode().equals(CompletionCode.TimeError));
    }

    private boolean isItALogOffCommand(ComCommand comCommand) {
        return comCommand.getCommandType().equals(ComCommandTypes.DAISY_CHAINED_LOGOFF) || comCommand.getCommandType().equals(ComCommandTypes.LOGOFF);
    }

    private ComCommand getBasicCheckCommandIfPresent() {
        for (Map.Entry<ComCommandType, ComCommand> comCommandTypesComCommandEntry : getCommands().entrySet()) {
            if (comCommandTypesComCommandEntry.getKey().equals(ComCommandTypes.BASIC_CHECK_COMMAND)) {
                return comCommandTypesComCommandEntry.getValue();
            }
        }
        return null;
    }

    /**
     * When there was a connection setup error at the beginning of the session, all other ComTaskExecutions
     * will not be executed anymore. They will be marked with a proper problem and get rescheduled .
     *
     * @param executionContext the context that is used for all these commands
     */
    void executeForConnectionSetupError(ExecutionContext executionContext) {
        executeWithAProblem(executionContext, MessageSeeds.NOT_EXECUTED_DUE_TO_CONNECTION_SETUP_ERROR);
    }

    /**
     * When there was a connection error during one of the ComTaskExecutions, all other ComTaskExecutions
     * will not be executed anymore. They will be marked with a proper problem and get rescheduled .
     *
     * @param executionContext the context that is used for all these commands
     */
    void executeForConnectionError(ExecutionContext executionContext) {
        executeWithAProblem(executionContext, MessageSeeds.NOT_EXECUTED_DUE_TO_CONNECTION_ERROR);
    }

    private void executeWithAProblem(ExecutionContext executionContext, MessageSeeds messageSeed) {
        for (ComTaskExecutionComCommandImpl comTaskExecutionComCommand : comTaskExecutionComCommands.values()) {
            try {
                executionContext.start(comTaskExecutionComCommand);
                Problem problem = getServiceProvider().issueService().newProblem(comTaskExecutionComCommand, messageSeed);
                comTaskExecutionComCommand.addIssue(problem, CompletionCode.NotExecuted);
                comTaskExecutionComCommand.setExecutionState(BasicComCommandBehavior.ExecutionState.NOT_EXECUTED);
                comTaskExecutionComCommand.delegateToJournalistIfAny(executionContext);
                executionContext.comTaskExecutionFailed(comTaskExecutionComCommand.getComTaskExecution());
            } finally {
                executionContext.completeExecutedComTask(comTaskExecutionComCommand.getComTaskExecution(), ComTaskExecutionSession.SuccessIndicator.Failure);
            }
        }
    }

    void executeForGeneralSetupError(ExecutionContext executionContext, List<? extends ComTaskExecution> scheduledButNotPreparedComTaskExecutions) {
        for (ComTaskExecution scheduledButNotPreparedComTaskExecution : scheduledButNotPreparedComTaskExecutions) {
            ComTaskExecutionComCommandImpl comTaskExecutionComCommand = getComTaskRoot(scheduledButNotPreparedComTaskExecution);
            try {
                executionContext.start(comTaskExecutionComCommand);
                Problem problem = getServiceProvider().issueService().newProblem(comTaskExecutionComCommand, MessageSeeds.NOT_EXECUTED_DUE_TO_GENERAL_SETUP_ERROR);
                comTaskExecutionComCommand.addIssue(problem, CompletionCode.NotExecuted);
                comTaskExecutionComCommand.setExecutionState(BasicComCommandBehavior.ExecutionState.NOT_EXECUTED);
                comTaskExecutionComCommand.delegateToJournalistIfAny(executionContext);
                executionContext.comTaskExecutionFailed(comTaskExecutionComCommand.getComTaskExecution());
            } finally {
                executionContext.completeExecutedComTask(comTaskExecutionComCommand.getComTaskExecution(), ComTaskExecutionSession.SuccessIndicator.Failure);
            }
        }
    }

    public CommandRoot getCommandRoot() {
        return commandRoot;
    }

    public void setCommandRoot(CommandRoot commandRoot) {
        this.commandRoot = commandRoot;
    }

    public OfflineDevice getOfflineDevice() {
        return offlineDevice;
    }

    public List<CollectedData> getCollectedData() {
        List<CollectedData> everything = new ArrayList<>();
        for (ComCommand command : comTaskExecutionComCommands.values()) {
            everything.addAll(command.getCollectedData());
        }
        return everything;
    }

    public void addCommand(ComCommand command, ComTaskExecution comTaskExecution) {
        ComTaskExecutionComCommand comTaskExecutionComCommand = this.getComTaskRoot(comTaskExecution);
        comTaskExecutionComCommand.addCommand(command, comTaskExecution);
    }

    public ComTaskExecutionComCommandImpl getComTaskRoot(ComTaskExecution comTaskExecution) {
        ComTaskExecutionComCommandImpl comTaskExecutionComCommand = this.comTaskExecutionComCommands.get(comTaskExecution);
        if (comTaskExecutionComCommand == null) {
            comTaskExecutionComCommand = new ComTaskExecutionComCommandImpl(this, comTaskExecution);
            this.comTaskExecutionComCommands.put(comTaskExecution, comTaskExecutionComCommand);
        }
        return comTaskExecutionComCommand;
    }

    private CompletionCode getCompletionCode() {
        CompletionCode completionCode = CompletionCode.Ok;

        // Also look at the CompletionCode of all child ComCommands
        for (Map.Entry<ComTaskExecution, ComTaskExecutionComCommandImpl> comCommandEntry : comTaskExecutionComCommands.entrySet()) {
            final ComCommand comCommand = comCommandEntry.getValue();
            completionCode = completionCode.upgradeTo(comCommand.getCompletionCode());
        }

        return completionCode;
    }

    public DeviceProtocolSecurityPropertySet getDeviceProtocolSecurityPropertySet() {
        return deviceProtocolSecurityPropertySet;
    }

    @Override
    public String getDescriptionTitle() {
        return ComCommandDescriptionTitle.GroupedDeviceCommand.getDescription();
    }

    private boolean checkIfCommandExists(ComCommandTypes comCommandTypes) {
        for (ComTaskExecutionComCommand comTaskExecutionComCommand : comTaskExecutionComCommands.values()) {
            for (ComCommand comCommand : comTaskExecutionComCommand) {
                if (comCommand.getCommandType().equals(comCommandTypes)) {
                    return true;
                }
            }
        }
        return false;
    }

    public ComCommand getComCommand(ComCommandTypes comCommandTypes) {
        for (ComTaskExecutionComCommand comTaskExecutionComCommand : comTaskExecutionComCommands.values()) {
            for (ComCommand comCommand : comTaskExecutionComCommand) {
                if (comCommand.getCommandType().equals(comCommandTypes)) {
                    return comCommand;
                }
            }
        }
        return null;
    }


    private AlreadyExecutedComCommand getAlreadyExecutedCommand(final CompositeComCommand compositeComCommand, ComTaskExecution comTaskExecution, ComCommandTypes comCommandType) {
        return getAlreadyExecutedCommand(compositeComCommand.getGroupedDeviceCommand(), comTaskExecution, comCommandType);
    }

    private AlreadyExecutedComCommand getAlreadyExecutedCommand(final GroupedDeviceCommand groupedDeviceCommand, ComTaskExecution comTaskExecution, ComCommandTypes comCommandType) {
        if (checkIfCommandExists(ComCommandTypes.ALREADY_EXECUTED)) { // Note: as we use 'containsKey', object equals is used (which is something we want, as then comTaskExecutionId is also checked)
            AlreadyExecutedComCommand alreadyExecutedCommand = (AlreadyExecutedComCommand) getComCommand(ComCommandTypes.ALREADY_EXECUTED);
            alreadyExecutedCommand.linkToComCommandDoingActualExecution(comCommandType, getComCommand(comCommandType));
            return alreadyExecutedCommand;
        } else {
            AlreadyExecutedComCommand alreadyExecutedCommand = createAlreadyExecutedCommand(comCommandType);
            alreadyExecutedCommand.linkToComCommandDoingActualExecution(comCommandType, getComCommand(comCommandType));
            groupedDeviceCommand.addCommand(alreadyExecutedCommand, comTaskExecution);
            return alreadyExecutedCommand;
        }
    }

    private AlreadyExecutedComCommand createAlreadyExecutedCommand(final ComCommandTypes comCommandType) {
        return new AlreadyExecutedComCommandImpl(this, comCommandType);
    }

    public LoadProfileCommand getLoadProfileCommand(final LoadProfilesTask loadProfilesTask, final GroupedDeviceCommand groupedDeviceCommand, ComTaskExecution comTaskExecution) {
        if (checkIfCommandExists(ComCommandTypes.LOAD_PROFILE_COMMAND)) {
            final LoadProfileCommand loadProfileCommand = (LoadProfileCommand) getComCommand(ComCommandTypes.LOAD_PROFILE_COMMAND);
            loadProfileCommand.updateAccordingTo(loadProfilesTask, this, comTaskExecution);
            getAlreadyExecutedCommand(groupedDeviceCommand, comTaskExecution, ComCommandTypes.LOAD_PROFILE_COMMAND);
            return loadProfileCommand;
        } else {
            return createLoadProfileCommand(loadProfilesTask, groupedDeviceCommand, comTaskExecution);
        }
    }

    private LoadProfileCommand createLoadProfileCommand(LoadProfilesTask loadProfilesTask, GroupedDeviceCommand groupedDeviceCommand, ComTaskExecution comTaskExecution) {
        LoadProfileCommand loadProfileCommand = new LoadProfileCommandImpl(this, loadProfilesTask, comTaskExecution);
        groupedDeviceCommand.addCommand(loadProfileCommand, comTaskExecution);
        return loadProfileCommand;
    }

    public RegisterCommand getRegisterCommand(final RegistersTask registersTask, final GroupedDeviceCommand groupedDeviceCommand, ComTaskExecution comTaskExecution) {
        if (checkIfCommandExists(ComCommandTypes.REGISTERS_COMMAND)) {
            RegisterCommand registerCommand = (RegisterCommand) getComCommand(ComCommandTypes.REGISTERS_COMMAND);
            registerCommand.addAdditionalRegisterGroups(registersTask, this.offlineDevice, comTaskExecution);
            getAlreadyExecutedCommand(groupedDeviceCommand, comTaskExecution, ComCommandTypes.REGISTERS_COMMAND);
            return registerCommand;
        } else {
            return createRegisterCommand(registersTask, groupedDeviceCommand, comTaskExecution);
        }
    }

    private RegisterCommand createRegisterCommand(RegistersTask registersTask, GroupedDeviceCommand groupedDeviceCommand, ComTaskExecution comTaskExecution) {
        RegisterCommand registerCommand = new RegisterCommandImpl(this, registersTask, comTaskExecution);
        groupedDeviceCommand.addCommand(registerCommand, comTaskExecution);
        return registerCommand;
    }

    public LogBooksCommand getLogBooksCommand(final LogBooksTask logBooksTask, final GroupedDeviceCommand groupedDeviceCommand, ComTaskExecution comTaskExecution) {
        if (checkIfCommandExists(ComCommandTypes.LOGBOOKS_COMMAND)) {
            LogBooksCommand logBooksCommand = (LogBooksCommand) getComCommand(ComCommandTypes.LOGBOOKS_COMMAND);
            logBooksCommand.updateAccordingTo(logBooksTask, this, comTaskExecution);
            getAlreadyExecutedCommand(groupedDeviceCommand, comTaskExecution, ComCommandTypes.LOGBOOKS_COMMAND);
            return logBooksCommand;
        } else {
            return createLogBooksCommand(logBooksTask, groupedDeviceCommand, comTaskExecution);
        }
    }

    private LogBooksCommand createLogBooksCommand(LogBooksTask logBooksTask, GroupedDeviceCommand groupedDeviceCommand, ComTaskExecution comTaskExecution) {
        LogBooksCommand logBooksCommand = new LogBooksCommandImpl(this, logBooksTask, comTaskExecution);
        groupedDeviceCommand.addCommand(logBooksCommand, comTaskExecution);
        return logBooksCommand;
    }

    public LegacyLoadProfileLogBooksCommand getLegacyLoadProfileLogBooksCommand(LoadProfilesTask loadProfilesTask, LogBooksTask logBooksTask, GroupedDeviceCommand groupedDeviceCommand, ComTaskExecution comTaskExecution) {
        if (checkIfCommandExists(ComCommandTypes.LEGACY_LOAD_PROFILE_LOGBOOKS_COMMAND)) {
            LegacyLoadProfileLogBooksCommand legacyLoadProfileLogBooksCommand = (LegacyLoadProfileLogBooksCommand) getComCommand(ComCommandTypes.LEGACY_LOAD_PROFILE_LOGBOOKS_COMMAND);
            legacyLoadProfileLogBooksCommand.updateAccordingTo(loadProfilesTask, this, comTaskExecution);
            legacyLoadProfileLogBooksCommand.updateAccordingTo(logBooksTask, this, comTaskExecution);
            getAlreadyExecutedCommand(groupedDeviceCommand, comTaskExecution, ComCommandTypes.LEGACY_LOAD_PROFILE_LOGBOOKS_COMMAND);
            return legacyLoadProfileLogBooksCommand;
        } else {
            return createLegacyLoadProfileLogBooksCommand(loadProfilesTask, logBooksTask, groupedDeviceCommand, comTaskExecution);
        }
    }

    private LegacyLoadProfileLogBooksCommand createLegacyLoadProfileLogBooksCommand(LoadProfilesTask loadProfilesTask, LogBooksTask logBooksTask, GroupedDeviceCommand groupedDeviceCommand, ComTaskExecution comTaskExecution) {
        LegacyLoadProfileLogBooksCommand legacyCommand = new LegacyLoadProfileLogBooksCommandImpl(this, loadProfilesTask, logBooksTask, comTaskExecution);
        groupedDeviceCommand.addCommand(legacyCommand, comTaskExecution);
        return legacyCommand;
    }

    public BasicCheckCommand getBasicCheckCommand(BasicCheckTask basicCheckTask, GroupedDeviceCommand groupedDeviceCommand, ComTaskExecution comTaskExecution) {
        if (checkIfCommandExists(ComCommandTypes.BASIC_CHECK_COMMAND)) {
            BasicCheckCommand basicCheckCommand = (BasicCheckCommand) getComCommand(ComCommandTypes.BASIC_CHECK_COMMAND);
            basicCheckCommand.updateAccordingTo(basicCheckTask, this, comTaskExecution);
            getAlreadyExecutedCommand(groupedDeviceCommand, comTaskExecution, ComCommandTypes.BASIC_CHECK_COMMAND);
            return basicCheckCommand;
        } else {
            return createBasicCheckCommand(basicCheckTask, groupedDeviceCommand, comTaskExecution);
        }
    }

    public BasicCheckCommand createBasicCheckCommand(BasicCheckTask basicCheckTask, GroupedDeviceCommand groupedDeviceCommand, ComTaskExecution comTaskExecution) {
        BasicCheckCommand basicCheckCommand = new BasicCheckCommandImpl(basicCheckTask, this, comTaskExecution);
        groupedDeviceCommand.addCommand(basicCheckCommand, comTaskExecution);
        return basicCheckCommand;
    }

    public StatusInformationCommand getStatusInformationCommand(final GroupedDeviceCommand groupedDeviceCommand, ComTaskExecution comTaskExecution) {
        if (checkIfCommandExists(ComCommandTypes.STATUS_INFORMATION_COMMAND)) {
            getAlreadyExecutedCommand(groupedDeviceCommand, comTaskExecution, ComCommandTypes.STATUS_INFORMATION_COMMAND);
            return (StatusInformationCommand) getComCommand(ComCommandTypes.STATUS_INFORMATION_COMMAND);
        } else {
            return createStatusInformationCommand(groupedDeviceCommand, comTaskExecution);
        }
    }

    public StatusInformationCommand createStatusInformationCommand(GroupedDeviceCommand groupedDeviceCommand, ComTaskExecution comTaskExecution) {
        StatusInformationCommand statusInformationCommand = new StatusInformationCommandImpl(getOfflineDevice(), this, comTaskExecution);
        groupedDeviceCommand.addCommand(statusInformationCommand, comTaskExecution);
        return statusInformationCommand;
    }

    public ReadRegistersCommand getReadRegistersCommand(final CompositeComCommand owner, ComTaskExecution comTaskExecution) {
        if (checkIfCommandExists(ComCommandTypes.READ_REGISTERS_COMMAND)) {
            return (ReadRegistersCommand) getComCommand(ComCommandTypes.READ_REGISTERS_COMMAND);
        } else {
            return createReadRegistersCommand(owner, comTaskExecution);
        }
    }

    public ReadRegistersCommand createReadRegistersCommand(CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution) {
        ReadRegistersCommandImpl readRegistersCommand = new ReadRegistersCommandImpl(this, possibleCommandOwner);
        possibleCommandOwner.addCommand(readRegistersCommand, comTaskExecution);
        return readRegistersCommand;
    }

    public ReadLogBooksCommand getReadLogBooksCommand(LogBooksCommand owner, ComTaskExecution comTaskExecution) {
        if (checkIfCommandExists(ComCommandTypes.READ_LOGBOOKS_COMMAND)) {
            return (ReadLogBooksCommand) getComCommand(ComCommandTypes.READ_LOGBOOKS_COMMAND);
        } else {
            return createReadLogBooksCommand(owner, comTaskExecution);
        }
    }

    public ReadLogBooksCommand createReadLogBooksCommand(LogBooksCommand logBooksCommand, ComTaskExecution comTaskExecution) {
        ReadLogBooksCommandImpl readLogBooksCommand = new ReadLogBooksCommandImpl(this, logBooksCommand);
        logBooksCommand.addCommand(readLogBooksCommand, comTaskExecution);
        return readLogBooksCommand;
    }

    public ClockCommand getClockCommand(final ClockTask clockTask, final GroupedDeviceCommand groupedDeviceCommand, ComTaskExecution comTaskExecution) {
        if (checkIfCommandExists(ComCommandTypes.CLOCK_COMMAND)) {
            ClockCommand clockCommand = (ClockCommand) getComCommand(ComCommandTypes.CLOCK_COMMAND);
            clockCommand.updateAccordingTo(clockTask, this, comTaskExecution);
            getAlreadyExecutedCommand(groupedDeviceCommand, comTaskExecution, ComCommandTypes.CLOCK_COMMAND);
            return clockCommand;
        } else {
            return createClockCommand(clockTask, groupedDeviceCommand, comTaskExecution);
        }
    }

    public ClockCommand createClockCommand(ClockTask clockTask, GroupedDeviceCommand groupedDeviceCommand, ComTaskExecution comTaskExecution) {
        ClockCommand clockCommand = new ClockCommandImpl(this, clockTask, comTaskExecution);
        groupedDeviceCommand.addCommand(clockCommand, comTaskExecution);
        return clockCommand;
    }

    public MessagesCommand getMessagesCommand(MessagesTask messagesTask, GroupedDeviceCommand groupedDeviceCommand, ComTaskExecution comTaskExecution) {
        if (checkIfCommandExists(ComCommandTypes.MESSAGES_COMMAND)) {
            MessagesCommand messagesCommand = (MessagesCommand) getComCommand(ComCommandTypes.MESSAGES_COMMAND);
            messagesCommand.updateAccordingTo(messagesTask, this, comTaskExecution);
            getAlreadyExecutedCommand(groupedDeviceCommand, comTaskExecution, ComCommandTypes.MESSAGES_COMMAND);
            return messagesCommand;
        } else {
            try {
                return createMessagesCommand(messagesTask, groupedDeviceCommand, comTaskExecution);
            } catch (MacException e) {
                ComTaskExecutionComCommandImpl comTaskRoot = groupedDeviceCommand.getComTaskRoot(comTaskExecution);
                Problem problem = getServiceProvider().issueService().newProblem(this, MessageSeeds.MAC_CHECK_FAILURE);
                comTaskRoot.addIssue(problem, CompletionCode.NotExecuted);
                comTaskRoot.setExecutionState(BasicComCommandBehavior.ExecutionState.NOT_EXECUTED);
                return null;
            }
        }
    }

    public MessagesCommand createMessagesCommand(MessagesTask messagesTask, GroupedDeviceCommand groupedDeviceCommand, ComTaskExecution comTaskExecution) {
        MessagesCommand messagesCommand = new MessagesCommandImpl(this, messagesTask, comTaskExecution);
        groupedDeviceCommand.addCommand(messagesCommand, comTaskExecution);
        return messagesCommand;
    }

    public TimeDifferenceCommand getTimeDifferenceCommand(final CompositeComCommand owner, ComTaskExecution comTaskExecution) {
        if (checkIfCommandExists(ComCommandTypes.TIME_DIFFERENCE_COMMAND)) {
            getAlreadyExecutedCommand(owner, comTaskExecution, ComCommandTypes.TIME_DIFFERENCE_COMMAND);
            return (TimeDifferenceCommand) getComCommand(ComCommandTypes.TIME_DIFFERENCE_COMMAND);
        } else {
            return createTimeDifferenceCommand(owner, comTaskExecution);
        }
    }

    public TimeDifferenceCommand createTimeDifferenceCommand(CompositeComCommand possibleCommandOwner, ComTaskExecution comTaskExecution) {
        TimeDifferenceCommand timeDifferenceCommand = new TimeDifferenceCommandImpl(this);
        possibleCommandOwner.addCommand(timeDifferenceCommand, comTaskExecution);
        return timeDifferenceCommand;
    }

    public VerifyTimeDifferenceCommand getVerifyTimeDifferenceCommand(CompositeComCommand owner, ComTaskExecution comTaskExecution) {
        if (checkIfCommandExists(ComCommandTypes.VERIFY_TIME_DIFFERENCE_COMMAND)) {
            return (VerifyTimeDifferenceCommand) getComCommand(ComCommandTypes.VERIFY_TIME_DIFFERENCE_COMMAND);
        } else {
            return createVerifyTimeDifferenceCommand(owner, comTaskExecution);
        }
    }

    public VerifyTimeDifferenceCommand createVerifyTimeDifferenceCommand(CompositeComCommand owner, ComTaskExecution comTaskExecution) {
        VerifyTimeDifferenceCommand verifyTimeDifferenceCommand = new VerifyTimeDifferenceCommandImpl((BasicCheckCommand) owner, this);
        owner.addCommand(verifyTimeDifferenceCommand, comTaskExecution);
        return verifyTimeDifferenceCommand;
    }

    public VerifyLoadProfilesCommand getVerifyLoadProfileCommand(final LoadProfileCommand owner, ComTaskExecution comTaskExecution) {
        if (checkIfCommandExists(ComCommandTypes.VERIFY_LOAD_PROFILE_COMMAND)) {
            return (VerifyLoadProfilesCommand) getComCommand(ComCommandTypes.VERIFY_LOAD_PROFILE_COMMAND);
        } else {
            return createVerifyLoadProfilesCommand(owner, comTaskExecution);
        }
    }

    public VerifyLoadProfilesCommand createVerifyLoadProfilesCommand(LoadProfileCommand loadProfileCommand, ComTaskExecution comTaskExecution) {
        VerifyLoadProfilesCommand verifyLoadProfilesCommand = new VerifyLoadProfilesCommandImpl(this, loadProfileCommand);
        loadProfileCommand.addCommand(verifyLoadProfilesCommand, comTaskExecution);
        return verifyLoadProfilesCommand;
    }

    public ReadLoadProfileDataCommand getReadLoadProfileDataCommand(final LoadProfileCommand owner, ComTaskExecution comTaskExecution) {
        if (checkIfCommandExists(ComCommandTypes.READ_LOAD_PROFILE_COMMAND)) {
            return (ReadLoadProfileDataCommand) getComCommand(ComCommandTypes.READ_LOAD_PROFILE_COMMAND);
        } else {
            return createReadLoadProfileDataCommand(owner, comTaskExecution);
        }
    }

    public ReadLoadProfileDataCommand createReadLoadProfileDataCommand(LoadProfileCommand loadProfileCommand, ComTaskExecution comTaskExecution) {
        ReadLoadProfileDataCommand readLoadProfileDataCommand = new ReadLoadProfileDataCommandImpl(this, loadProfileCommand);
        loadProfileCommand.addCommand(readLoadProfileDataCommand, comTaskExecution);
        return readLoadProfileDataCommand;
    }

    public ReadLegacyLoadProfileLogBooksDataCommand getReadLegacyLoadProfileLogBooksDataCommand(LegacyLoadProfileLogBooksCommand owner, ComTaskExecution comTaskExecution) {
        if (checkIfCommandExists(ComCommandTypes.READ_LEGACY_LOAD_PROFILE_LOGBOOKS_COMMAND)) {
            return (ReadLegacyLoadProfileLogBooksDataCommand) getComCommand(ComCommandTypes.READ_LEGACY_LOAD_PROFILE_LOGBOOKS_COMMAND);
        } else {
            return createReadLegacyLoadProfileLogBooksDataCommand(owner, comTaskExecution);
        }
    }

    public ReadLegacyLoadProfileLogBooksDataCommand createReadLegacyLoadProfileLogBooksDataCommand(LegacyLoadProfileLogBooksCommand legacyLoadProfileLogBooksCommand, ComTaskExecution comTaskExecution) {
        ReadLegacyLoadProfileLogBooksDataCommand readLegacyLoadProfileLogBooksDataCommand = new ReadLegacyLoadProfileLogBooksDataCommandImpl(this, legacyLoadProfileLogBooksCommand);
        legacyLoadProfileLogBooksCommand.addCommand(readLegacyLoadProfileLogBooksDataCommand, comTaskExecution);
        return readLegacyLoadProfileLogBooksDataCommand;
    }

    public MarkIntervalsAsBadTimeCommand getMarkIntervalsAsBadTimeCommand(final LoadProfileCommand owner, ComTaskExecution comTaskExecution) {
        if (checkIfCommandExists(ComCommandTypes.MARK_LOAD_PROFILES_AS_BAD_TIME)) {
            return (MarkIntervalsAsBadTimeCommand) getComCommand(ComCommandTypes.MARK_LOAD_PROFILES_AS_BAD_TIME);
        } else {
            return createMarkIntervalsAsBadTimeCommand(owner, comTaskExecution);
        }
    }

    public MarkIntervalsAsBadTimeCommand createMarkIntervalsAsBadTimeCommand(LoadProfileCommand loadProfileCommand, ComTaskExecution comTaskExecution) {
        MarkIntervalsAsBadTimeCommand markIntervalsAsBadTimeCommand = new MarkIntervalsAsBadTimeCommandImpl(this, loadProfileCommand);
        loadProfileCommand.addCommand(markIntervalsAsBadTimeCommand, comTaskExecution);
        return markIntervalsAsBadTimeCommand;
    }

    public CreateMeterEventsFromStatusFlagsCommand getCreateMeterEventsFromStatusFlagsCommand(final LoadProfileCommand owner, ComTaskExecution comTaskExecution) {
        if (checkIfCommandExists(ComCommandTypes.CREATE_METER_EVENTS_IN_LOAD_PROFILE_FROM_STATUS_FLAGS)) {
            return (CreateMeterEventsFromStatusFlagsCommand) getComCommand(ComCommandTypes.CREATE_METER_EVENTS_IN_LOAD_PROFILE_FROM_STATUS_FLAGS);
        } else {
            return createMeterEventsFromStatusFlagsCommand(owner, comTaskExecution);
        }
    }

    public CreateMeterEventsFromStatusFlagsCommand createMeterEventsFromStatusFlagsCommand(LoadProfileCommand loadProfileCommand, ComTaskExecution comTaskExecution) {
        CreateMeterEventsFromStatusFlagsCommand createMeterEventsFromStatusFlagsCommand = new CreateMeterEventsFromStatusFlagsCommandImpl(this, loadProfileCommand);
        loadProfileCommand.addCommand(createMeterEventsFromStatusFlagsCommand, comTaskExecution);
        return createMeterEventsFromStatusFlagsCommand;
    }

    public ForceClockCommand getForceClockCommand(final ClockCommand owner, ComTaskExecution comTaskExecution) {
        if (checkIfCommandExists(ComCommandTypes.FORCE_CLOCK_COMMAND)) {
            return (ForceClockCommand) getComCommand(ComCommandTypes.FORCE_CLOCK_COMMAND);
        } else {
            return createForceClockCommand(owner, comTaskExecution);
        }
    }

    public ForceClockCommand createForceClockCommand(ClockCommand clockCommand, ComTaskExecution comTaskExecution) {
        ForceClockCommand forceClockCommand = new ForceClockCommandImpl(this);
        clockCommand.addCommand(forceClockCommand, comTaskExecution);
        return forceClockCommand;
    }

    public SetClockCommand getSetClockCommand(final ClockCommand owner, ComTaskExecution comTaskExecution) {
        if (checkIfCommandExists(ComCommandTypes.SET_CLOCK_COMMAND)) {
            return (SetClockCommand) getComCommand(ComCommandTypes.SET_CLOCK_COMMAND);
        } else {
            return createSetClockCommand(owner, comTaskExecution);
        }
    }

    public SetClockCommand createSetClockCommand(ClockCommand clockCommand, ComTaskExecution comTaskExecution) {
        SetClockCommand setClockCommand = new SetClockCommandImpl(this, clockCommand, comTaskExecution);
        clockCommand.addCommand(setClockCommand, comTaskExecution);
        return setClockCommand;
    }

    public SynchronizeClockCommand getSynchronizeClockCommand(final ClockCommand owner, ComTaskExecution comTaskExecution) {
        if (checkIfCommandExists(ComCommandTypes.SYNCHRONIZE_CLOCK_COMMAND)) {
            return (SynchronizeClockCommand) getComCommand(ComCommandTypes.SYNCHRONIZE_CLOCK_COMMAND);
        } else {
            return createSynchronizeClockCommand(owner, comTaskExecution);
        }
    }

    public SynchronizeClockCommand createSynchronizeClockCommand(ClockCommand clockCommand, ComTaskExecution comTaskExecution) {
        SynchronizeClockCommand synchronizeClockCommand = new SynchronizeClockCommandImpl(this, clockCommand, comTaskExecution);
        clockCommand.addCommand(synchronizeClockCommand, comTaskExecution);
        return synchronizeClockCommand;
    }

    public TopologyCommand getTopologyCommand(final TopologyTask topologyTask, final GroupedDeviceCommand groupedDeviceCommand, ComTaskExecution comTaskExecution) {
        if (checkIfCommandExists(ComCommandTypes.TOPOLOGY_COMMAND)) {
            TopologyCommand topologyCommand = (TopologyCommand) getComCommand(ComCommandTypes.TOPOLOGY_COMMAND);
            topologyCommand.updateAccordingTo(topologyTask, this, comTaskExecution);
            getAlreadyExecutedCommand(groupedDeviceCommand, comTaskExecution, ComCommandTypes.TOPOLOGY_COMMAND);
            return topologyCommand;
        } else {
            return createTopologyCommand(topologyTask, groupedDeviceCommand, comTaskExecution);
        }
    }

    public TopologyCommand createTopologyCommand(TopologyTask topologyTask, GroupedDeviceCommand groupedDeviceCommand, ComTaskExecution comTaskExecution) {
        TopologyCommand topologyCommand = new TopologyCommandImpl(this, topologyTask.getTopologyAction(), comTaskExecution);
        groupedDeviceCommand.addCommand(topologyCommand, comTaskExecution);
        return topologyCommand;
    }

    public VerifySerialNumberCommand getVerifySerialNumberCommand(final BasicCheckCommand owner, ComTaskExecution comTaskExecution) {
        if (checkIfCommandExists(ComCommandTypes.VERIFY_SERIAL_NUMBER_COMMAND)) {
            return (VerifySerialNumberCommand) getComCommand(ComCommandTypes.VERIFY_SERIAL_NUMBER_COMMAND);
        } else {
            return createVerifySerialNumberCommand(owner, comTaskExecution);
        }
    }

    public VerifySerialNumberCommand createVerifySerialNumberCommand(BasicCheckCommand comCommands, ComTaskExecution comTaskExecution) {
        VerifySerialNumberCommand verifySerialNumberCommand = new VerifySerialNumberCommandImpl(this);
        comCommands.addCommand(verifySerialNumberCommand, comTaskExecution);
        return verifySerialNumberCommand;
    }

    public FirmwareManagementCommand getFirmwareCommand(GroupedDeviceCommand groupedDeviceCommand, FirmwareManagementTask firmwareManagementTask, ComTaskExecution comTaskExecution) {
        if (checkIfCommandExists(ComCommandTypes.FIRMWARE_COMMAND)) {
            return (FirmwareManagementCommand) getComCommand(ComCommandTypes.FIRMWARE_COMMAND);
        } else {
            return createFirmwareUpgradeCommand(groupedDeviceCommand, firmwareManagementTask, comTaskExecution);

        }
    }

    private FirmwareManagementCommand createFirmwareUpgradeCommand(GroupedDeviceCommand groupedDeviceCommand, FirmwareManagementTask firmwareManagementTask, ComTaskExecution comTaskExecution) {
        FirmwareManagementCommandImpl firmwareUpgradeCommand = new FirmwareManagementCommandImpl(this, firmwareManagementTask, comTaskExecution, this.offlineDevice);
        groupedDeviceCommand.addCommand(firmwareUpgradeCommand, comTaskExecution);
        return firmwareUpgradeCommand;
    }

    /**
     * Get the List of ComCommands
     *
     * @return the requested list of ComCommands
     */
    public Map<ComCommandType, ComCommand> getCommands() {
        Map<ComCommandType, ComCommand> allCommands = new LinkedHashMap<>();
        for (ComTaskExecutionComCommand comTaskExecutionComCommand : comTaskExecutionComCommands.values()) {
            allCommands.putAll(comTaskExecutionComCommand.getCommands());
        }
        return allCommands;
    }

    public DeviceProtocol getDeviceProtocol() {
        return this.deviceProtocol;
    }

    @Override
    public Iterator<ComTaskExecutionComCommandImpl> iterator() {
        return this.comTaskExecutionComCommands.values().iterator();
    }

    public void connectionErrorOccurred() {
        commandRoot.connectionErrorOccurred();
    }

    public void updateComChannelForComCommands(ComPortRelatedComChannel comChannel) {
        for (ComTaskExecutionComCommandImpl comCommands : this) {
            for (ComCommand comCommand : comCommands) {
                if (comCommand.getCommandType().equals(ComCommandTypes.DEVICE_PROTOCOL_INITIALIZE)) {
                    ((DeviceProtocolInitializeCommand) comCommand).updateComChannel(comChannel);
                }
            }
        }
    }
}