package com.energyict.mdc.engine.impl.commands.store.core;

import com.energyict.mdc.common.StackTracePrinter;
import com.energyict.mdc.common.comserver.logging.CanProvideDescriptionTitle;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilderImpl;
import com.energyict.mdc.common.comserver.logging.PropertyDescriptionBuilder;
import com.energyict.mdc.engine.exceptions.*;
import com.energyict.mdc.engine.impl.commands.collect.ComCommand;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.core.ComCommandJournalist;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.logging.LogLevelMapper;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.io.ConnectionCommunicationException;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.issues.Problem;
import com.energyict.mdc.issues.Warning;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.data.CollectedData;
import com.energyict.mdc.io.CommunicationException;
import com.energyict.mdc.protocol.api.exceptions.LegacyProtocolException;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;

import com.elster.jupiter.nls.Thesaurus;

import java.io.IOException;
import java.time.Clock;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.energyict.mdc.device.data.tasks.history.CompletionCode.*;

/**
 * Provides an implementation for the {@link ComCommand} interface.
 *
 * @author gna
 * @since 9/05/12 - 12:02
 */

public abstract class SimpleComCommand implements ComCommand, CanProvideDescriptionTitle {

    private final CommandRoot commandRoot;
    private CompletionCode completionCode = Ok;

    /**
     * All the issue that have occurred during the execution of this {@link ComCommand}.
     */
    private List<Issue> issueList = new ArrayList<>();

    /**
     * All the {@link CollectedData} that have been collected during the execution of this {@link ComCommand}.
     */
    private List<CollectedData> collectedDataList = new ArrayList<>();

    /**
     * The state of the command execution.
     */
    public enum ExecutionState {
        /**
         * Command is not yet executed.
         */
        NOT_EXECUTED,
        /**
         * Command is successfully executed.
         */
        SUCCESSFULLY_EXECUTED,
        /**
         * Command is executed but failed.
         */
        FAILED
    }

    /**
     * Keeps track of the executionState of this command.
     */
    private ExecutionState executionState = ExecutionState.NOT_EXECUTED;

    /**
     * Perform the actions represented by this ComCommand.<br/>
     * <b>Note:</b> this action will only perform once.
     *
     * @param deviceProtocol the {@link DeviceProtocol} which will perform the actions
     * @param executionContext The ExecutionContext
     */
    public abstract void doExecute (final DeviceProtocol deviceProtocol, ExecutionContext executionContext);

    protected SimpleComCommand(final CommandRoot commandRoot) {
        this.commandRoot = commandRoot;
    }

    @Override
    public CompletionCode getCompletionCode () {
        return this.getHighestCompletionCode();
    }

    protected CompletionCode getMyCompletionCode () {
        return this.completionCode;
    }

    /**
     * Search for the most important completionCode. This can be the
     * completionCode from the ComCommand itself, but it can also be
     * a CompletionCode subtracted from the resultType of a CollectedData object.
     *
     * @return the highest CompletionCode
     */
    private CompletionCode getHighestCompletionCode() {
        CompletionCode completionCode = this.completionCode;
        for (CollectedData collectedData : getCollectedData()) {
            CompletionCode collectedDataCompletionCode = forResultType(collectedData.getResultType());
            if (collectedDataCompletionCode.hasPriorityOver(completionCode)) {
                completionCode = collectedDataCompletionCode;
            }
        }
        return completionCode;
    }

    protected void setCompletionCode (CompletionCode completionCode) {
        this.completionCode = completionCode;
    }

    @Override
    public CommandRoot getCommandRoot() {
        return this.commandRoot;
    }

    protected Thesaurus getThesaurus() {
        return this.getCommandRoot().getServiceProvider().thesaurus();
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
    public List<Problem> getProblems () {
        return this.getIssues()
                .stream()
                .filter(Issue::isProblem)
                .map(Problem.class::cast)
                .collect(Collectors.toList());
    }

    @Override
    public List<Warning> getWarnings () {
        return this.getIssues()
                .stream()
                .filter(Issue::isWarning)
                .map(Warning.class::cast)
                .collect(Collectors.toList());
    }

    @Override
    public List<CollectedData> getCollectedData() {
        return new ArrayList<>(collectedDataList);
    }

    /**
     * Add the given {@link Issue} to the {@link #issueList}.
     *
     * @param issue the {@link Issue} to add
     */
    public void addIssue(Issue issue) {
        this.issueList.add(issue);
    }

    public void addIssue (Issue issue, CompletionCode completionCode) {
        this.addIssue(issue);
        this.setCompletionCode(this.completionCode.upgradeTo(completionCode));
    }

    @Override
    public void addCollectedDataItem(final CollectedData collectedData) {
        this.collectedDataList.add(collectedData);
    }

    @Override
    public void addListOfCollectedDataItems(final List<? extends CollectedData> collectedDataList) {
        this.collectedDataList.addAll(collectedDataList);
    }

    @Override
    public void execute(final DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        this.validateArguments(deviceProtocol, executionContext);
        if (!hasExecuted()) {
            this.setCompletionCode(Ok);  // First optimistic
            boolean success = false;    // then pessimistic, does that make me manic
            try {
                doExecute(deviceProtocol, executionContext);
                success = true;
            } catch (ConnectionCommunicationException e){
                setCompletionCode(ConnectionError);
                throw e;
            } catch (CommunicationException e) {
                setCompletionCode(ProtocolError);
                addIssue(getIssueService().newProblem(deviceProtocol, "deviceprotocol.protocol.issue", StackTracePrinter.print(e)), ProtocolError);
                throw new ConnectionCommunicationException(MessageSeeds.COMMUNICATION_FAILURE, e);
            } catch (LegacyProtocolException e) {
                if (isExceptionCausedByALegacyTimeout(e)) {
                    setCompletionCode(ConnectionError);
                    throw new ConnectionCommunicationException(MessageSeeds.UNEXPECTED_IO_EXCEPTION, (IOException) e.getCause());
                } else {
                    addIssue(getIssueService().newProblem(deviceProtocol, "deviceprotocol.legacy.issue", StackTracePrinter.print(e)), UnexpectedError);
                }
            } finally {
                if (success) {
                    this.executionState = ExecutionState.SUCCESSFULLY_EXECUTED;
                } else {
                    this.executionState = ExecutionState.FAILED;
                }
                this.journalExecutionCompleted(executionContext);
                this.publishExecutionCompletedEvent(executionContext);
            }
        }
    }

    private void journalExecutionCompleted(ExecutionContext executionContext) {
        ComCommandJournalist journalist = executionContext.getJournalist();
        if (journalist != null) {
            journalist.executionCompleted(this, this.getServerLogLevel(executionContext));
        }
    }

    private void publishExecutionCompletedEvent(ExecutionContext executionContext) {
        new ComCommandJournalEventPublisher(executionContext.eventPublisher()).executionCompleted(this, executionContext);
    }

    private LogLevel getServerLogLevel (ExecutionContext executionContext) {
        return this.getServerLogLevel(executionContext.getComPort());
    }

    private LogLevel getServerLogLevel (ComPort comPort) {
        return this.getServerLogLevel(comPort.getComServer());
    }

    private LogLevel getServerLogLevel (ComServer comServer) {
        return LogLevelMapper.forComServerLogLevel().toLogLevel(comServer.getCommunicationLogLevel());
    }

    private boolean isExceptionCausedByALegacyTimeout(LegacyProtocolException e) {
        return e.getMessage().toLowerCase().contains("timeout") && IOException.class.isAssignableFrom(e.getCause().getClass());
    }

    private void validateArguments (DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        if (deviceProtocol == null) {
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "execute", "deviceProtocol");
        }
        if (executionContext == null) {
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "execute", "executionContext");
        }
    }

    /**
     * @return the {@link #executionState}
     */
    public ExecutionState getExecutionState() {
        return this.executionState;
    }

    /**
     * Indication whether this command has been executed
     *
     * @return true if the command has executed (successful or failed), false if the command hasn't executed
     */
    public boolean hasExecuted() {
        return this.executionState != ExecutionState.NOT_EXECUTED;
    }

    @Override
    public LogLevel getJournalingLogLevel () {
        switch (this.getCompletionCode()) {
            case Ok: {
                return this.defaultJournalingLogLevel();
            }
            case ConfigurationWarning: {
                return LogLevel.WARN;
            }
            case ProtocolError:
                // Intentional fall-through
            case TimeError:
                // Intentional fall-through
            case ConfigurationError:
                // Intentional fall-through
            case IOError:
                // Intentional fall-through
            case UnexpectedError:
                // Intentional fall-through
            case ConnectionError:
                return LogLevel.ERROR;
            default: {
                throw CodingException.unrecognizedEnumValue(LogLevel.class, this.completionCode.ordinal());
            }
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    /**
     * Gets the default LogLevel that the ComServer as a minimum
     * must be set to before this ComCommand will be logged
     * if the CompletionCode is Ok.
     *
     * @return The default log level
     */
    protected LogLevel defaultJournalingLogLevel () {
        return LogLevel.INFO;
    }

    @Override
    public final String toJournalMessageDescription (LogLevel serverLogLevel) {
        DescriptionBuilder builder = new DescriptionBuilderImpl(this);
        this.toJournalMessageDescription(builder, serverLogLevel);
        return builder.toString();
    }

    @Override
    public final String issuesToJournalMessageDescription() {
        if (getIssues().isEmpty()) {
            return "";
        }
        else {
            DescriptionBuilder builder = new DescriptionBuilderImpl(() -> "Issues");
            this.buildErrorDescription(builder);
            return builder.toString();
        }
    }

    private void buildErrorDescription(DescriptionBuilder builder) {
        this.buildErrorDescription(builder, "Problems", this.getProblems());
        this.buildErrorDescription(builder, "Warnings", this.getWarnings());
    }

    private <T extends Issue> void buildErrorDescription(DescriptionBuilder builder, String heading, List<T> issues) {
        if (!issues.isEmpty()) {
            DateTimeFormatter dateFormat = DateTimeFormatter.ISO_INSTANT;
            PropertyDescriptionBuilder listBuilder = builder.addListProperty(heading);
            for (T issue : issues) {
                listBuilder.append(issue.getDescription());
                appendIssueTimestamp(listBuilder, dateFormat, issue);
                listBuilder.next();
            }
        }
    }

    private <T extends Issue> void appendIssueTimestamp(PropertyDescriptionBuilder builder, DateTimeFormatter dateFormat, T issue) {
        builder.append(" (").append(dateFormat.format(issue.getTimestamp())).append(")");
    }

    protected void toJournalMessageDescription (DescriptionBuilder builder, LogLevel serverLogLevel) {
        if (   this.isJournalingLevelEnabled(serverLogLevel, LogLevel.INFO)
            && this.notSuccessFullyExecuted()) {
            builder.addProperty("executionState").append(this.executionState.name());
            builder.addProperty("completionCode").append(this.completionCode.name());
        }
        if (this.isJournalingLevelEnabled(serverLogLevel, LogLevel.DEBUG)) {
            builder.addProperty("nrOfWarnings").append(this.countWarnings());
            builder.addProperty("nrOfProblems").append(this.countProblems());
        }
    }

    /**
     * Tests if the specified server log level enables details of the
     * minimum level to be shown in journal messages.
     *
     * @param serverLogLevel The server LogLevel
     * @param minimumLevel The minimum level that is required for a message to show up in journaling
     * @return A flag that indicates if message details of the minimum level should show up in journaling
     */
    protected boolean isJournalingLevelEnabled (LogLevel serverLogLevel, LogLevel minimumLevel) {
        return serverLogLevel.compareTo(minimumLevel) >= 0;
    }

    private boolean notSuccessFullyExecuted () {
        return !ExecutionState.SUCCESSFULLY_EXECUTED.equals(this.executionState);
    }

    private long countWarnings () {
        return this.getWarnings().size();
    }

    private long countProblems () {
        return this.getProblems().size();
    }

    public IssueService getIssueService() {
        return getCommandRoot().getServiceProvider().issueService();
    }

    public Clock getClock() {
        return getCommandRoot().getServiceProvider().clock();
    }

}