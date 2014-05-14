package com.energyict.mdc.engine.impl.core.aspects.journaling;

import com.elster.jupiter.util.time.Clock;
import com.energyict.mdc.engine.impl.commands.collect.ComCommand;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.tasks.history.ComTaskExecutionSessionBuilder;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

/**
 * Writes the journal entries on behalf of the ComCommandJournaling aspect.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-01-15 (13:50)
 */
public class ComCommandJournalist {

    private ComTaskExecutionSessionBuilder comTaskExecutionSessionBuilder;
    private final Clock clock;
    public static final NumberFormat NUMBER_FORMAT = new DecimalFormat("00");
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss:SSS");

    public ComCommandJournalist(ComTaskExecutionSessionBuilder comTaskExecutionSessionBuilder) {
        super();
        this.comTaskExecutionSessionBuilder = comTaskExecutionSessionBuilder;
    }

    /**
     * Notifies this ComCommandJournalist that the specified
     * {@link ComCommand} completed (with or without success)
     * and that the appropriate journal entries should be created.
     *
     * @param comCommand     The ComCommand that completed
     * @param serverLogLevel The current LogLevel of the ComServer that has executed the ComCommand
     */
    public void executionCompleted(ComCommand comCommand, LogLevel serverLogLevel) {
        if (this.shouldLog(comCommand, serverLogLevel)) {
            this.addJournalEntries(comCommand, serverLogLevel);
        }
    }

    private boolean shouldLog(ComCommand comCommand, LogLevel serverLogLevel) {
        return serverLogLevel.compareTo(comCommand.getJournalingLogLevel()) >= 0;
    }

    private void addJournalEntries(ComCommand comCommand, LogLevel serverLogLevel) {
        String errorDescription = buildErrorDescription(comCommand);
        String commandDescription = comCommand.toJournalMessageDescription(serverLogLevel);
        comTaskExecutionSessionBuilder.addComCommandJournalEntry(clock.now(), comCommand.getCompletionCode(), errorDescription, commandDescription);
    }

    private String buildErrorDescription(ComCommand comCommand) {
        StringBuilder builder = new StringBuilder();
        if (hasIssues(comCommand)) {
            appendHeader(builder, comCommand);
            appendIssues(builder, "Problems", comCommand.getProblems());
            builder.append(comCommand.getProblems().isEmpty() ? "" : "\n");
            appendIssues(builder, "Warnings", comCommand.getWarnings());
        }
        return builder.toString();
    }

    private void appendHeader(StringBuilder builder, ComCommand comCommand) {
        builder.append("Execution completed with ").
                append(comCommand.getWarnings().size()).
                append(" warning(s) and ").
                append(comCommand.getProblems().size()).
                append(" problem(s)\n");
    }

    private boolean hasIssues(ComCommand comCommand) {
        return numberOfIssues(comCommand) > 0;
    }

    private int numberOfIssues(ComCommand comCommand) {
        return comCommand.getProblems().size() + comCommand.getWarnings().size();
    }

    private void appendIssues(StringBuilder builder, String heading, List<? extends Issue<?>> issues) {
        builder.append(heading).append(':');
        int issueNumber = 1;
        for (Issue<?> issue : issues) {
            appendIssue(builder, issueNumber++, issue);
        }
    }

    private void appendIssue(StringBuilder builder, int issueNumber, Issue<?> issue) {
        builder.append('\n').append('\t').append(NUMBER_FORMAT.format(issueNumber)).append('.').append(' ');
        builder.append(issue.getDescription());
        builder.append(' ').append('(').append(DATE_FORMAT.print(issue.getTimestamp().getTime())).append(')');
    }

}