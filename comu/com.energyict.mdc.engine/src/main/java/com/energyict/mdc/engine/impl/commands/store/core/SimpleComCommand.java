/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store.core;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.comserver.logging.CanProvideDescriptionTitle;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilderImpl;
import com.energyict.mdc.common.protocol.DeviceProtocol;
import com.energyict.mdc.common.tasks.history.CompletionCode;
import com.energyict.mdc.device.data.exceptions.CanNotFindForIdentifier;
import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.engine.impl.commands.MessageSeeds;
import com.energyict.mdc.engine.impl.commands.collect.ComCommand;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.logging.LogLevelMapper;
import com.energyict.mdc.engine.impl.tools.StackTracePrinter;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.exceptions.LegacyProtocolException;
import com.energyict.mdc.protocol.api.exceptions.NestedPropertyValidationException;
import com.energyict.mdc.protocol.pluggable.adapters.upl.UPLNlsServiceAdapter;
import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.issue.Problem;
import com.energyict.mdc.upl.issue.Warning;
import com.energyict.mdc.upl.meterdata.CollectedData;
import com.energyict.protocol.exceptions.CommunicationException;
import com.energyict.protocol.exceptions.CommunicationInterruptedException;
import com.energyict.protocol.exceptions.ConnectionCommunicationException;
import com.energyict.protocol.exceptions.ConnectionSetupException;
import com.energyict.protocol.exceptions.DataParseException;
import com.energyict.protocol.exceptions.DeviceConfigurationException;
import com.energyict.protocol.exceptions.ModemException;
import com.energyict.protocol.exceptions.ProtocolRuntimeException;

import java.io.IOException;
import java.util.ArrayList;
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
     * A List containing all the {@link CollectedData} which is collected during the executi
     * on of this {@link ComCommand}
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
        this.basicComCommandBehavior = new BasicComCommandBehavior(this, getDescriptionTitle(), getServiceProvider().clock(), getServiceProvider().deviceMessageService());
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
                boolean connectionInterrupted = false;
                try {
                    doExecute(deviceProtocol, executionContext);
                    success = true;
                } catch (CommunicationInterruptedException e) {
                    // Indicates the comports thread has been interrupted
                    // This is most likely caused by the priority scheduler, who has interrupted a normal priority task in order to free the comport for execution of a priority task
                    // The next comtasks for this connection will be set to 'not executed'.
                    injectNlsServiceIfNeeded(e);
                    connectionInterrupted = true;
                    addIssue(getServiceProvider().issueService().newProblem(deviceProtocol, MessageSeeds.DEVICEPROTOCOL_PROTOCOL_ISSUE, e), CompletionCode.Rescheduled);
                    getGroupedDeviceCommand().connectionInterrupted();
                    logTaskExecutionFailed(executionContext, e);
                } catch (ConnectionCommunicationException e) {
                    injectNlsServiceIfNeeded(e);
                    if (e.getExceptionType().equals(ConnectionCommunicationException.Type.CONNECTION_STILL_INTACT)) {
                        /* A special case applicable for physical slaves that have the same gateway (and thus connection task)
                         * Communication to current physical slave has failed, but the connection is still intact. Other physical slaves can still use it. */
                        addIssue(getServiceProvider().issueService().newProblem(deviceProtocol, MessageSeeds.DEVICEPROTOCOL_PROTOCOL_ISSUE, e), CompletionCode.TimeoutError);
                        getGroupedDeviceCommand().skipOtherComTaskExecutions();
                    } else {
                        /* Any other ConnectionCommunicationException means that the connection is broken/closed and can no longer be used.
                         * The next comtasks for this connection will be set to 'not executed'. */
                        connectionErrorOccurred(deviceProtocol, e);
                        executionContext.connect();
                    }
                    logTaskExecutionFailed(executionContext, e);
                } catch (ConnectionSetupException | ModemException e) {
                    injectNlsServiceIfNeeded(e);
                    connectionErrorOccurred(deviceProtocol, e);
                    logTaskExecutionFailed(executionContext, e);
                } catch (CommunicationException | DataParseException e) {
                    injectNlsServiceIfNeeded(e);
                    addIssue(getServiceProvider().issueService().newProblem(deviceProtocol, MessageSeeds.DEVICEPROTOCOL_PROTOCOL_ISSUE, e), CompletionCode.ProtocolError);
                    logTaskExecutionFailed(executionContext, e);
                } catch (DeviceConfigurationException | CanNotFindForIdentifier e) {
                    injectNlsServiceIfNeeded(e);
                    addIssue(getServiceProvider().issueService().newProblem(deviceProtocol, MessageSeeds.DEVICEPROTOCOL_PROTOCOL_ISSUE, e), CompletionCode.ConfigurationError);
                    logTaskExecutionFailedDueToProblems(executionContext);
                } catch (NestedPropertyValidationException e) {
                    injectNlsServiceIfNeeded(e);
                    addIssue(getServiceProvider().issueService()
                            .newProblem(deviceProtocol, MessageSeeds.NOT_EXECUTED_DUE_TO_GENERAL_SETUP_ERROR, e.getUplException()), CompletionCode.ConfigurationError);
                    logTaskExecutionFailedDueToProblems(executionContext);
                } catch (LegacyProtocolException e) {
                    injectNlsServiceIfNeeded(e);
                    if (isExceptionCausedByALegacyTimeout(e)) {
                        connectionErrorOccurred(deviceProtocol, e);
                    } else {
                        addIssue(getServiceProvider().issueService()
                                .newProblem(deviceProtocol, MessageSeeds.DEVICEPROTOCOL_LEGACY_ISSUE, StackTracePrinter.print(e, getCommunicationLogLevel(executionContext))), CompletionCode.UnexpectedError);
                    }
                    getGroupedDeviceCommand().skipOtherComTaskExecutions();
                    logTaskExecutionFailed(executionContext, e);
                } finally {
                    if (success) {
                        this.basicComCommandBehavior.setExecutionState(BasicComCommandBehavior.ExecutionState.SUCCESSFULLY_EXECUTED);
                    } else if (connectionInterrupted) {
                        this.basicComCommandBehavior.setExecutionState(BasicComCommandBehavior.ExecutionState.NOT_EXECUTED);
                    }else {
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

    private void injectNlsServiceIfNeeded(RuntimeException e) {
        if (e instanceof ProtocolRuntimeException) {
            ((ProtocolRuntimeException)e).injectNlsService(UPLNlsServiceAdapter.adaptTo(getServiceProvider().nlsService()));
        }
    }

    private void logTaskExecutionFailed(ExecutionContext executionContext, Throwable e) {
        executionContext.connectionLogger.taskExecutionFailed(e, Thread.currentThread().getName(), getComTasksDescription(executionContext), executionContext.getComTaskExecution()
                .getDevice()
                .getName());
    }

    private void logTaskExecutionFailedDueToProblems(ExecutionContext executionContext) {
        executionContext.connectionLogger.taskExecutionFailedDueToProblems(Thread.currentThread()
                .getName(), getComTasksDescription(executionContext), executionContext.getComTaskExecution().getDevice().getName());
    }

    private String getComTasksDescription(ExecutionContext executionContext) {
        return executionContext.getComTaskExecution().getComTask().getName();
    }

    private void connectionErrorOccurred(DeviceProtocol deviceProtocol, Throwable e) {
        addIssue(getServiceProvider().issueService().newProblem(deviceProtocol, MessageSeeds.COMMAND_FAILED_DUE_TO_CONNECTION_RELATED_ISSUE, e), CompletionCode.ConnectionError);
    }

    @Override
    public OfflineDevice getOfflineDevice() {
        return this.groupedDeviceCommand.getOfflineDevice();
    }

    protected boolean isEmpty(String meterSerialNumber) {
        return meterSerialNumber == null || meterSerialNumber.isEmpty();
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
     * @param issue the {@link Issue} to add
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
     * @param minimumLevel The minimum level that is required for a message to show up in journaling
     * @return A flag that indicates if message details of the minimum level should show up in journaling
     */
    protected boolean isJournalingLevelEnabled(LogLevel serverLogLevel, LogLevel minimumLevel) {
        return this.basicComCommandBehavior.isJournalingLevelEnabled(serverLogLevel, minimumLevel);
    }

    protected IssueService getIssueService() {
        return getServiceProvider().issueService();
    }

    protected Thesaurus getThesaurus() {
        return getServiceProvider().thesaurus();
    }
}