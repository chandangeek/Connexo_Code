package com.energyict.mdc.engine.impl.commands.store.core;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.comserver.logging.CanProvideDescriptionTitle;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilderImpl;
import com.energyict.mdc.device.data.exceptions.CanNotFindForIdentifier;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.engine.impl.commands.MessageSeeds;
import com.energyict.mdc.engine.impl.commands.collect.ComCommand;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.logging.LogLevelMapper;
import com.energyict.mdc.engine.impl.tools.StackTracePrinter;
import com.energyict.mdc.io.CommunicationException;
import com.energyict.mdc.io.ConnectionCommunicationException;
import com.energyict.mdc.io.ModemException;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.exceptions.ConnectionSetupException;
import com.energyict.mdc.protocol.api.exceptions.DataParseException;
import com.energyict.mdc.protocol.api.exceptions.DeviceConfigurationException;
import com.energyict.mdc.protocol.api.exceptions.DuplicateException;
import com.energyict.mdc.protocol.api.exceptions.LegacyProtocolException;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.issue.Problem;
import com.energyict.mdc.upl.issue.Warning;
import com.energyict.mdc.upl.meterdata.CollectedData;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.LoadProfileReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Provides an implementation for the {@link ComCommand} interface.
 *
 * @author gna
 * @since 9/05/12 - 12:02
 */

public abstract class SimpleComCommand implements ComCommand, CanProvideDescriptionTitle {

    private final GroupedDeviceCommand groupedDeviceCommand;

    private final BasicComCommandBehavior basicComCommandBehavior;

    /**
     * A List containing all the {@link CollectedData} which is collected during the execution of this {@link ComCommand}
     */
    private List<CollectedData> collectedDataList = new ArrayList<>();

    /**
     * A List containing all the issue which occurred during the execution of this {@link ComCommand}
     */
    private List<Issue> issueList = new ArrayList<>();

    protected SimpleComCommand(final GroupedDeviceCommand groupedDeviceCommand) {
        if (groupedDeviceCommand == null) {
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "constructor", "groupedDeviceCommand", com.energyict.mdc.engine.impl.MessageSeeds.METHOD_ARGUMENT_CAN_NOT_BE_NULL);
        }

        this.groupedDeviceCommand = groupedDeviceCommand;
        this.basicComCommandBehavior = new BasicComCommandBehavior(this, ComCommandDescriptionTitle.getComCommandDescriptionTitleFor(this.getClass()).getDescription(), getServiceProvider().clock());
    }

    private CommandRoot.ServiceProvider getServiceProvider() {
        return getCommandRoot().getServiceProvider();
    }

    public abstract void doExecute(final DeviceProtocol deviceProtocol, ExecutionContext executionContext);

    @Override
    public void execute(final DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        try {
        /* Regular code */
            this.validateArguments(deviceProtocol, executionContext);
            if (!hasExecuted()) {
                this.setCompletionCode(CompletionCode.Ok);  // First optimistic
                boolean success = false;    // then pessimistic, does that make me manic
                try {
                    doExecute(deviceProtocol, executionContext);
                    success = true;
                } catch (CommunicationException e) {
                    if (e instanceof ConnectionCommunicationException) {

                        if (e.getMessageSeed() == com.energyict.mdc.protocol.api.MessageSeeds.NUMBER_OF_RETRIES_REACHED_CONNECTION_STILL_INTACT) {
                            //A special case applicable for physical slaves that have the same gateway (and thus connection task)
                            //It is a common timeout (we did not receive the response of the slave device in time), but the connection is still intact. Other physical slaves can still use it.
                            addIssue(getServiceProvider().issueService().newProblem(deviceProtocol, MessageSeeds.DEVICEPROTOCOL_PROTOCOL_ISSUE, e), CompletionCode.TimeoutError);
                            getGroupedDeviceCommand().skipOtherComTaskExecutions();
                        } else if (e.getMessageSeed() == com.energyict.mdc.protocol.api.MessageSeeds.UNEXPECTED_PROTOCOL_ERROR
                                || e.getMessageSeed() == com.energyict.mdc.protocol.api.MessageSeeds.CIPHERING_EXCEPTION) {
                            //Problem in the application layer of the protocol, specific for the current physical slave. The next physical slaves can still be read out.
                            //For example: invalid frame counter, decryption failure, empty object list, etc.
                            addIssue(getServiceProvider().issueService().newProblem(deviceProtocol, MessageSeeds.DEVICEPROTOCOL_PROTOCOL_ISSUE, e), CompletionCode.UnexpectedError);
                            getGroupedDeviceCommand().skipOtherComTaskExecutions();
                        } else {
                            //Any other ConnectionCommunicationException means that the connection is broken/closed and can no longer be used.
                            //The next comtasks for this connection will be set to 'not executed'.
                            connectionErrorOccurred(deviceProtocol, e);
                        }

                    } else if (e instanceof ConnectionSetupException || e instanceof ModemException) {
                        connectionErrorOccurred(deviceProtocol, e);
                    } else {
                        addIssue(getServiceProvider().issueService().newProblem(deviceProtocol, MessageSeeds.DEVICEPROTOCOL_PROTOCOL_ISSUE, e), CompletionCode.ProtocolError);
                    }
                    executionContext.connectionLogger.taskExecutionFailed(e, Thread.currentThread().getName(), getComTasksDescription(executionContext), executionContext.getComTaskExecution().getDevice().getmRID());

                } catch (DataParseException e) {
                    addIssue(getServiceProvider().issueService().newProblem(deviceProtocol, MessageSeeds.DEVICEPROTOCOL_PROTOCOL_ISSUE, e), CompletionCode.ProtocolError);
                    executionContext.connectionLogger.taskExecutionFailed(e, Thread.currentThread().getName(), getComTasksDescription(executionContext), executionContext.getComTaskExecution().getDevice().getmRID());
                } catch (DeviceConfigurationException | CanNotFindForIdentifier | DuplicateException e) {
                    addIssue(getServiceProvider().issueService().newProblem(deviceProtocol, MessageSeeds.DEVICEPROTOCOL_PROTOCOL_ISSUE, e), CompletionCode.ConfigurationError);
                    executionContext.connectionLogger.taskExecutionFailedDueToProblems(Thread.currentThread().getName(), getComTasksDescription(executionContext), executionContext.getComTaskExecution().getDevice().getmRID());

                } catch (LegacyProtocolException e) {
                    if (isExceptionCausedByALegacyTimeout(e)) {
                        connectionErrorOccurred(deviceProtocol, e);
                    } else {
                        addIssue(getServiceProvider().issueService().newProblem(deviceProtocol, MessageSeeds.DEVICEPROTOCOL_LEGACY_ISSUE, StackTracePrinter.print(e, getCommunicationLogLevel(executionContext))), CompletionCode.UnexpectedError);
                    }
                    getGroupedDeviceCommand().skipOtherComTaskExecutions();
                    executionContext.connectionLogger.taskExecutionFailed(e, Thread.currentThread().getName(), getComTasksDescription(executionContext), executionContext.getComTaskExecution().getDevice().getmRID());
                } finally {
                    if (success) {
                        this.basicComCommandBehavior.setExecutionState(BasicComCommandBehavior.ExecutionState.SUCCESSFULLY_EXECUTED);
                    } else {
                        this.basicComCommandBehavior.setExecutionState(BasicComCommandBehavior.ExecutionState.FAILED);
                    }
                }
            }
        } finally {
            if (executionContext != null) {
            /* AspectJ - ComCommandJournaling - ComCommandLogging */
                this.delegateToJournalistIfAny(executionContext);

            /* AspectJ - ComCommandJournaling - ComCommandJournalEventPublisher */
                executionContext.eventPublisher().publish(basicComCommandBehavior.toEvent(executionContext));
            }
        }
    }


    private String getComTasksDescription(ExecutionContext executionContext) {
        StringBuilder result = new StringBuilder();
        List<ComTask> comTasks = executionContext.getComTaskExecution().getComTasks();
        for (ComTask comTask : comTasks) {
            if (result.length() > 0) {
                result.append(", ");
            }
            result.append(comTask.getName());
        }
        return result.toString();
    }

    private void connectionErrorOccurred(DeviceProtocol deviceProtocol, Throwable e) {
        addIssue(getServiceProvider().issueService().newProblem(deviceProtocol, MessageSeeds.COMMAND_FAILED_DUE_TO_CONNECTION_RELATED_ISSUE, e.getLocalizedMessage()), CompletionCode.ConnectionError);
        groupedDeviceCommand.connectionErrorOccurred();
    }

    @Override
    public OfflineDevice getOfflineDevice() {
        return this.groupedDeviceCommand.getOfflineDevice();
    }

    @Override
    public CompletionCode getCompletionCode() {
        return this.basicComCommandBehavior.getHighestCompletionCode();
    }

    @Override
    public void setCompletionCode(CompletionCode completionCode) {
        this.basicComCommandBehavior.setCompletionCode(completionCode);
    }

    /**
     * Add the given {@link Issue} to the {@link #issueList} and upgrade the {@link CompletionCode} to the given one.
     *
     * @param issue          the {@link Issue} to add
     * @param completionCode the {@link CompletionCode} to upgrade to
     */
    public void addIssue(Issue issue, CompletionCode completionCode) {
        this.issueList.add(issue);
        this.setCompletionCode(basicComCommandBehavior.getCompletionCode().upgradeTo(completionCode));
    }

    @Override
    public CommandRoot getCommandRoot() {
        return this.groupedDeviceCommand.getCommandRoot();
    }

    @Override
    public GroupedDeviceCommand getGroupedDeviceCommand() {
        return this.groupedDeviceCommand;
    }

    public void addCollectedDataItem(CollectedData collectedData) {
        this.collectedDataList.add(collectedData);
    }

    public void addListOfCollectedDataItems(List<? extends CollectedData> collectedDataList) {
        this.collectedDataList.addAll(collectedDataList);
    }


    @Override
    public List<Issue> getIssues() {
        List<Issue> issues = new ArrayList<>(this.issueList);
        for (CollectedData collectedData : this.getCollectedData()) {
            issues.addAll(collectedData.getIssues());
        }
        return issues;
    }

    @Override
    public List<Problem> getProblems() {
        List<Issue> issues = this.getIssues();
        List<Problem> problems = new ArrayList<>(issues.size());    // At most all issues are problems
        for (Issue issue : issues) {
            if (issue.isProblem()) {
                problems.add((Problem) issue);
            }
        }
        return problems;
    }

    @Override
    public List<Warning> getWarnings() {
        List<Issue> issues = this.getIssues();
        List<Warning> warnings = new ArrayList<>(issues.size());    // At most all issues are warnings
        for (Issue issue : issues) {
            if (issue.isWarning()) {
                warnings.add((Warning) issue);
            }
        }
        return warnings;
    }

    @Override
    public List<CollectedData> getCollectedData() {
        return new ArrayList<>(collectedDataList);
    }

    void delegateToJournalistIfAny(ExecutionContext executionContext) {
        this.basicComCommandBehavior.delegateToJournalistIfAny(executionContext);
    }

    private boolean isExceptionCausedByALegacyTimeout(LegacyProtocolException e) {
        return e.getMessage().toLowerCase().contains("timeout") && IOException.class.isAssignableFrom(e.getCause().getClass());
    }

    private void validateArguments(DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        if (deviceProtocol == null) {
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "execute", "deviceProtocol", com.energyict.mdc.engine.impl.MessageSeeds.METHOD_ARGUMENT_CAN_NOT_BE_NULL);
        }
        if (executionContext == null) {
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "execute", "executionContext", com.energyict.mdc.engine.impl.MessageSeeds.METHOD_ARGUMENT_CAN_NOT_BE_NULL);
        }
    }

    public LogLevel getCommunicationLogLevel(ExecutionContext executionContext) {
        try {
            return LogLevelMapper.forComServerLogLevel().toLogLevel(executionContext.getComPort().getComServer().getCommunicationLogLevel());
        } catch (NullPointerException e) {
            return null;
        }
    }

    /**
     * @return the executionState
     */
    public BasicComCommandBehavior.ExecutionState getExecutionState() {
        return this.basicComCommandBehavior.getExecutionState();
    }

    public void setExecutionState(BasicComCommandBehavior.ExecutionState state) {
        this.basicComCommandBehavior.setExecutionState(state);
    }

    public boolean hasExecuted() {
        return this.basicComCommandBehavior.hasExecuted();
    }

    @Override
    public LogLevel getJournalingLogLevel() {
        return this.basicComCommandBehavior.getJournalingLogLevel();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    protected LogLevel defaultJournalingLogLevel() {
        return this.basicComCommandBehavior.defaultJournalingLogLevel();
    }

    @Override
    public String getDescriptionTitle() {
        return this.basicComCommandBehavior.getDescriptionTitle();
    }

    @Override
    public String toJournalMessageDescription(LogLevel serverLogLevel) {
        DescriptionBuilder builder = new DescriptionBuilderImpl(this);
        this.toJournalMessageDescription(builder, serverLogLevel);
        return builder.toString();
    }

    protected void toJournalMessageDescription(DescriptionBuilder builder, LogLevel serverLogLevel) {
        this.basicComCommandBehavior.toJournalMessageDescription(builder, serverLogLevel);
    }

    public String issuesToJournalMessageDescription() {
        return this.basicComCommandBehavior.issuesToJournalMessageDescription();
    }

    /**
     * Tests if the specified server log level enables details of the
     * minimum level to be shown in journal messages.
     *
     * @param serverLogLevel The server LogLevel
     * @param minimumLevel   The minimum level that is required for a message to show up in journaling
     * @return A flag that indicates if message details of the minimum level should show up in journaling
     */
    protected boolean isJournalingLevelEnabled(LogLevel serverLogLevel, LogLevel minimumLevel) {
        return this.basicComCommandBehavior.isJournalingLevelEnabled(serverLogLevel, minimumLevel);
    }

    /**
     * Used by the load profile commands to remove any channel intervals (and channel infos) from the collected LP that were not requested by the LP reader.
     * This can be the case for protocols that do not have selective access yet based for channels.
     */
    protected void removeUnwantedChannels(List<LoadProfileReader> loadProfileReaders, List<CollectedData> collectedDatas) {
        for (LoadProfileReader loadProfileReader : loadProfileReaders) {
            for (CollectedData collectedData : collectedDatas) {
                if (collectedData instanceof CollectedLoadProfile) {
                    CollectedLoadProfile collectedLoadProfile = (CollectedLoadProfile) collectedData;

                    if (collectedLoadProfile.getLoadProfileIdentifier().getProfileObisCode().equalsIgnoreBChannel(loadProfileReader.getProfileObisCode())) {

                        //Only remove unwanted channels if the protocol generated more channel infos than the number of channels configured in EIServer
                        if (collectedLoadProfile.getChannelInfo().size() > loadProfileReader.getChannelInfos().size()) {

                            int index = 0;
                            Iterator<ChannelInfo> channelInfoIterator = collectedLoadProfile.getChannelInfo().iterator();
                            while (channelInfoIterator.hasNext()) {
                                ChannelInfo readChannel = channelInfoIterator.next();
                                if (!channelIsConfigured(loadProfileReader, readChannel)) {
                                    //Remove channel data that was not requested
                                    channelInfoIterator.remove();
                                    for (IntervalData intervalData : collectedLoadProfile.getCollectedIntervalData()) {
                                        intervalData.getIntervalValues().remove(index);
                                    }
                                } else {
                                    index++;
                                }
                            }

                            for (int channelIndex = 0; channelIndex < collectedLoadProfile.getChannelInfo().size(); channelIndex++) {
                                collectedLoadProfile.getChannelInfo().get(channelIndex).setId(channelIndex);
                                collectedLoadProfile.getChannelInfo().get(channelIndex).setChannelId(channelIndex);
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * Return true if the read out channel (identified by obiscode, unit, serial number) is also configured in EIServer.
     * Otherwise, interval data for this channel cannot be stored in EIServer. It will be filtered out.
     * <p>
     * Note that there's a special case here: if the read out channel has the same obiscode and serial number,
     * but a flow unit instead of a configured volume unit (e.g. meter channel is kWh instead of configured kW),
     * the collected interval data for that channel should still be stored, after it has been converted.
     * This conversion is done in the EIServer storer class.
     * The other direction (meter channel kW and configured in EIServer as kWh) is also supported.
     */
    private boolean channelIsConfigured(LoadProfileReader loadProfileReader, ChannelInfo readChannel) {

        //Clone the argument
        ChannelInfo clone = new ChannelInfo(readChannel.getId(), readChannel.getName(), readChannel.getUnit(), readChannel.getMeterIdentifier(), readChannel.isCumulative(), readChannel.getReadingTypeMRID());

        //We found an exact match, cool.
        if (loadProfileReader.getChannelInfos().contains(clone)) {
            return true;
        }

        //Check if we find a match if we change the received flow unit to its volume unit counter part.
        if (clone.getUnit().isFlowUnit() && clone.getUnit().getVolumeUnit() != null) {
            clone.setUnit(clone.getUnit().getVolumeUnit());
            return loadProfileReader.getChannelInfos().contains(clone);
        }

        //Check if we find a match if we change the received volume unit to its flow unit counter part.
        if (clone.getUnit().isVolumeUnit() && clone.getUnit().getFlowUnit() != null) {
            clone.setUnit(clone.getUnit().getFlowUnit());
            return loadProfileReader.getChannelInfos().contains(clone);
        }

        return false;
    }

    protected IssueService getIssueService() {
        return getServiceProvider().issueService();
    }

    protected Thesaurus getThesaurus() {
        return getServiceProvider().thesaurus();
    }
}