/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.engine.impl.commands.collect.ComCommand;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.tools.StackTracePrinter;
import com.energyict.mdc.issues.Issue;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Clock;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Supplier;

/**
 * Writes the journal entries for {@link ComCommand}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-01-15 (13:50)
 */
public class ComCommandJournalist {

    private final JournalEntryFactory journalEntryFactory;
    private final Clock clock;
    private static final NumberFormat NUMBER_FORMAT = new DecimalFormat("00");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_INSTANT;

    ComCommandJournalist(JournalEntryFactory journalEntryFactory, Clock clock) {
        super();
        this.journalEntryFactory = journalEntryFactory;
        this.clock = clock;
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
        this.journalEntryFactory.createComCommandJournalEntry(clock.instant(), comCommand.getCompletionCode(), errorDescription, commandDescription);
    }

    private String buildErrorDescription(ComCommand comCommand) {
        StringBuilder builder = new StringBuilder();
        if (hasIssues(comCommand)) {
            appendHeader(builder, comCommand);
            if(comCommand.getProblems().size() > 0) {
                appendIssues(builder, "Problems", comCommand.getProblems(), () -> "\n");
            }
            if(comCommand.getWarnings().size() > 0) {
                appendIssues(builder, "Warnings", comCommand.getWarnings(), () -> "");
            }
        }
        return builder.toString();
    }

    private boolean hasIssues(ComCommand comCommand) {
        return !comCommand.getIssues().isEmpty();
    }

    private void appendHeader(StringBuilder builder, ComCommand comCommand) {
        builder.append("Execution completed with ").
                append(comCommand.getWarnings().size()).
                append(" warning(s) and ").
                append(comCommand.getProblems().size()).
                append(" problem(s)\n");
    }

    private void appendIssues(StringBuilder builder, String heading, List<? extends Issue> issues, Supplier<String> terminator) {
        builder.append(heading).append(':');
        int issueNumber = 1;
        for (Issue issue : issues) {
            appendIssue(builder, issueNumber++, issue);
        }
        if (!issues.isEmpty()) {
            builder.append(terminator.get());
        }
    }

    private void appendIssue(StringBuilder builder, int issueNumber, Issue issue) {
        builder.append('\n').append('\t').append(NUMBER_FORMAT.format(issueNumber)).append('.').append(' ');
        builder.append(issue.getDescription());
        issue.getException().ifPresent(ex -> builder.append("\r\n").append(StackTracePrinter.print(ex)));
    }

}