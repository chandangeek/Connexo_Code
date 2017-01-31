package com.energyict.mdc.engine.impl.commands.store.core;

import com.elster.jupiter.util.time.DefaultDateTimeFormatters;
import com.energyict.mdc.common.comserver.logging.CanProvideDescriptionTitle;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilderImpl;
import com.energyict.mdc.common.comserver.logging.PropertyDescriptionBuilder;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.events.ComServerEvent;
import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.engine.impl.MessageSeeds;
import com.energyict.mdc.engine.impl.commands.collect.ComCommand;
import com.energyict.mdc.engine.impl.core.ComCommandJournalist;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.events.AbstractComServerEventImpl;
import com.energyict.mdc.engine.impl.events.logging.ComCommandLoggingEvent;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.logging.LogLevelMapper;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.protocol.api.device.data.CollectedData;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.List;

/**
 * Groups basic ComCommand behavior.
 * Different implementations of a ComCommand can use me to reuse the same behavior
 */
public class BasicComCommandBehavior implements CanProvideDescriptionTitle {

    private final ComCommand comCommand;
    private final String descriptionTitle;
    private final Clock clock;
    private final com.energyict.mdc.engine.impl.events.AbstractComServerEventImpl.ServiceProvider serviceProvider;
    private CompletionCode completionCode = CompletionCode.Ok;

    /**
     * Keeps track of the executionState of this command
     */
    private BasicComCommandBehavior.ExecutionState executionState = BasicComCommandBehavior.ExecutionState.NOT_EXECUTED;

    BasicComCommandBehavior(ComCommand comCommand, String descriptionTitle, Clock clock) {
        this.comCommand = comCommand;
        this.descriptionTitle = descriptionTitle;
        this.clock = clock;
        this.serviceProvider = new ServiceProvider();
    }

    CompletionCode getCompletionCode() {
        return completionCode;
    }

    void setCompletionCode(CompletionCode completionCode) {
        this.completionCode = completionCode;
    }

    /**
     * Search for the most important completionCode. This can be the
     * completionCode from the ComCommand itself, but it can also be
     * a CompletionCode subtracted from the resultType of a CollectedData object.
     *
     * @return the highest CompletionCode
     */
    CompletionCode getHighestCompletionCode() {
        CompletionCode completionCode = this.completionCode;
        for (CollectedData collectedData : comCommand.getCollectedData()) {
            if (CompletionCode.forResultType(collectedData.getResultType()).hasPriorityOver(completionCode)) {
                completionCode = CompletionCode.forResultType(collectedData.getResultType());
            }
        }
        return completionCode;
    }

    protected LogLevel getServerLogLevel(ExecutionContext executionContext) {
        return this.getServerLogLevel(executionContext.getComPort());
    }

    protected LogLevel getServerLogLevel(ComPort comPort) {
        return this.getServerLogLevel(comPort.getComServer());
    }

    protected LogLevel getServerLogLevel(ComServer comServer) {
        return LogLevelMapper.forComServerLogLevel().toLogLevel(comServer.getCommunicationLogLevel());
    }

    void delegateToJournalistIfAny(ExecutionContext executionContext) {
        /* Business code validates that execution context can be null
         * and will throw a CodingException when that is the case. */
        if (executionContext != null) {
            ComCommandJournalist journalist = executionContext.getJournalist();
            if (journalist != null) {
                journalist.executionCompleted(comCommand, this.getServerLogLevel(executionContext));
            }
        }
    }

    ComServerEvent toEvent(ExecutionContext executionContext) {
        if (executionContext != null) {
            String journalMessageDescription = comCommand.toJournalMessageDescription(this.getServerLogLevel(executionContext.getComPort()));
            String errorDescription = comCommand.issuesToJournalMessageDescription();
            String logMessage = !errorDescription.isEmpty() ? journalMessageDescription.concat("; ").concat(errorDescription) : journalMessageDescription;
            return new ComCommandLoggingEvent(
                    serviceProvider,
                    executionContext.getComPort(),
                    executionContext.getConnectionTask(),
                    executionContext.getComTaskExecution(),
                    LogLevel.DEBUG,
                    logMessage);
        } else {
            String journalMessageDescription = comCommand.toJournalMessageDescription(LogLevel.DEBUG);
            String errorDescription = comCommand.issuesToJournalMessageDescription();
            String logMessage = !errorDescription.isEmpty() ? journalMessageDescription.concat("; ").concat(errorDescription) : journalMessageDescription;
            return new ComCommandLoggingEvent(
                    serviceProvider,
                    null,
                    null,
                    null,
                    LogLevel.DEBUG,
                    logMessage);
        }
    }

    public LogLevel getJournalingLogLevel() {
        switch (this.completionCode) {
            case Ok: {
                return this.defaultJournalingLogLevel();
            }
            case ConfigurationWarning: {
                return LogLevel.WARN;
            }
            case ProtocolError:
                // Intentional fall-through
            case ConfigurationError:
                // Intentional fall-through
            case IOError:
                // Intentional fall-through
            case UnexpectedError:
                // Intentional fall-through
            case NotExecuted:
                // Intentional fall-through
            case TimeError:
                // Intentional fall-through
            case InitError:
                // Intentional fall-through
            case TimeoutError:
                // Intentional fall-through
            case ConnectionError:
                return LogLevel.ERROR;
            default: {
                throw CodingException.unrecognizedEnumValue(LogLevel.class, this.completionCode.ordinal(), MessageSeeds.UNRECOGNIZED_ENUM_VALUE);
            }
        }
    }

    public LogLevel defaultJournalingLogLevel() {
        return LogLevel.INFO;
    }

    public String toJournalMessageDescription(LogLevel serverLogLevel) {
        DescriptionBuilder builder = new DescriptionBuilderImpl(this);
        this.toJournalMessageDescription(builder, serverLogLevel);
        return builder.toString();
    }

    public void toJournalMessageDescription(DescriptionBuilder builder, LogLevel serverLogLevel) {
        if (this.isJournalingLevelEnabled(serverLogLevel, LogLevel.INFO) && this.notSuccessFullyExecuted()) {
            builder.addProperty("executionState").append(this.executionState.name());
            builder.addProperty("completionCode").append(this.completionCode.name());
        }
        if (this.isJournalingLevelEnabled(serverLogLevel, LogLevel.DEBUG) && (this.countWarnings() != 0 || this.countProblems() != 0)) {
            builder.addProperty("nrOfWarnings").append(this.countWarnings());
            builder.addProperty("nrOfProblems").append(this.countProblems());
        }
    }

    @Override
    public String getDescriptionTitle() {
        return this.descriptionTitle;
    }

    public boolean isJournalingLevelEnabled(LogLevel serverLogLevel, LogLevel minimumLevel) {
        return serverLogLevel.compareTo(minimumLevel) >= 0;
    }

    private int countWarnings() {
        int counter = 0;
        for (Issue issue : comCommand.getIssues()) {
            if (issue.isWarning()) {
                counter++;
            }
        }
        return counter;
    }

    private int countProblems() {
        int counter = 0;
        for (Issue issue : comCommand.getIssues()) {
            if (issue.isProblem()) {
                counter++;
            }
        }
        return counter;
    }

    private boolean notSuccessFullyExecuted() {
        return !ExecutionState.SUCCESSFULLY_EXECUTED.equals(this.executionState);
    }

    public String issuesToJournalMessageDescription() {
        if (!comCommand.getIssues().isEmpty()) {
            DescriptionBuilder builder = new DescriptionBuilderImpl(() -> "Issues");
            this.buildErrorDescription(builder);
            return builder.toString();
        }
        return "";
    }

    private void buildErrorDescription(DescriptionBuilder builder) {
        buildErrorDescription(builder, "Problems", comCommand.getProblems());
        buildErrorDescription(builder, "Warnings", comCommand.getWarnings());
    }

    private <T extends Issue> void buildErrorDescription(DescriptionBuilder builder, String heading, List<T> issues) {
        if (!issues.isEmpty()) {
            DateTimeFormatter dateAndTimeFormatter = DefaultDateTimeFormatters.mediumDate().withLongTime().build();

            PropertyDescriptionBuilder listBuilder = builder.addListProperty(heading);
            for (T issue : issues) {
                listBuilder.append(issue.getDescription());
                appendIssueTimestamp(listBuilder, dateAndTimeFormatter.toFormat(), issue);
                listBuilder.next();
            }
        }
    }

    private <T extends Issue> void appendIssueTimestamp(PropertyDescriptionBuilder builder, Format dateFormat, Issue issue) {
        builder.append(" (").append(dateFormat.format(Date.from(issue.getTimestamp()))).append(")");
    }

    public boolean hasExecuted() {
        return this.executionState != BasicComCommandBehavior.ExecutionState.NOT_EXECUTED;
    }

    public ExecutionState getExecutionState() {
        return executionState;
    }

    public void setExecutionState(ExecutionState executionState) {
        this.executionState = executionState;
    }

    /**
     * The state of the command execution
     */
    public enum ExecutionState {
        /**
         * command is not yet executed
         */
        NOT_EXECUTED,
        /**
         * command is successfully executed
         */
        SUCCESSFULLY_EXECUTED,
        /**
         * command is executed but failed
         */
        FAILED
    }

    private class ServiceProvider implements AbstractComServerEventImpl.ServiceProvider {

        @Override
        public Clock clock() {
            return clock;
        }
    }
}

