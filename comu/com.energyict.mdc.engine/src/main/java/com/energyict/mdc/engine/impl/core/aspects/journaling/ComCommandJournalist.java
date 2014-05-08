package com.energyict.mdc.engine.impl.core.aspects.journaling;

import com.energyict.mdc.engine.impl.commands.collect.ComCommand;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.issues.Problem;
import com.energyict.mdc.issues.Warning;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Writes the journal entries on behalf of the ComCommandJournaling aspect.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-01-15 (13:50)
 */
public class ComCommandJournalist {

    private ComTaskExecutionSessionShadow comTaskExecutionSessionShadow;

    public ComCommandJournalist (ComTaskExecutionSessionShadow comTaskExecutionSessionShadow) {
        super();
        this.comTaskExecutionSessionShadow = comTaskExecutionSessionShadow;
    }

    /**
     * Notifies this ComCommandJournalist that the specified
     * {@link ComCommand} completed (with or without success)
     * and that the appropriate journal entries should be created.
     *
     * @param comCommand The ComCommand that completed
     * @param serverLogLevel The current LogLevel of the ComServer that has executed the ComCommand
     */
    public void executionCompleted (ComCommand comCommand, LogLevel serverLogLevel) {
        if (this.shouldLog(comCommand, serverLogLevel)) {
            this.addJournalEntries(comCommand, serverLogLevel);
        }
    }

    private boolean shouldLog (ComCommand comCommand, LogLevel serverLogLevel) {
        return serverLogLevel.compareTo(comCommand.getJournalingLogLevel()) >= 0;
    }

    private void addJournalEntries (ComCommand comCommand, LogLevel serverLogLevel) {
        List<ComTaskExecutionJournalEntryShadow> entryShadows = this.toJournalEntries(comCommand, serverLogLevel);
        for (ComTaskExecutionJournalEntryShadow entryShadow : entryShadows) {
            this.comTaskExecutionSessionShadow.addComTaskJournalEntry(entryShadow);
        }
    }

    private List<ComTaskExecutionJournalEntryShadow> toJournalEntries (ComCommand comCommand, LogLevel serverLogLevel) {
        List<ComTaskExecutionJournalEntryShadow> journalEntryShadows = new ArrayList<>();
        journalEntryShadows.add(this.toJournalEntry(comCommand, serverLogLevel));
        return journalEntryShadows;
    }

    private ComTaskExecutionJournalEntryShadow toJournalEntry (ComCommand comCommand, LogLevel serverLogLevel) {
        ComCommandJournalEntryShadow shadow = new ComCommandJournalEntryShadow();
        shadow.setTimestamp(Clocks.getAppServerClock().now());
        shadow.setCommandDescription(comCommand.toJournalMessageDescription(serverLogLevel));
        this.setErrorDescription(shadow, comCommand);
        shadow.setCompletionCode(comCommand.getCompletionCode());
        return shadow;
    }

    private void setErrorDescription (ComCommandJournalEntryShadow shadow, ComCommand comCommand) {
        StringBuilder builder = new StringBuilder();
        this.buildErrorDescription(builder, comCommand);
        if (builder.length() != 0) {
            shadow.setErrorDescription(builder.toString());
        }
    }

    private void buildErrorDescription (StringBuilder builder, ComCommand comCommand) {
        List<Problem> problems = comCommand.getProblems();
        List<Warning> warnings = comCommand.getWarnings();
        int numberOfIssues = problems.size() + warnings.size();
        if (numberOfIssues > 0) {
            builder.
                append("Execution completed with ").
                append(warnings.size()).
                append(" warning(s) and ").
                append(problems.size()).
                append(" problem(s)\n");
        }
        String separator = this.buildErrorDescription(builder, "Problems", problems);
        builder.append(separator);
        this.buildErrorDescription(builder, "Warnings", warnings);
    }

    private <T extends Issue> String buildErrorDescription (StringBuilder builder, String heading, List<T> issues) {
        if (!issues.isEmpty()) {
            NumberFormat numberFormat = new DecimalFormat("00");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSSS");
            builder.append(heading).append(":\n");
            String separator = "\t";
            int issueNumber = 1;
            for (T issue : issues) {
                builder.append(separator).append(numberFormat.format(issueNumber)).append(". ");
                builder.append(issue.getDescription());
                this.appendIssueTimestamp(builder, dateFormat, issue);
                separator = "\n\t";
                issueNumber++;
            }
            return "\n";
        }
        else {
            return "";
        }
    }

    private <T extends Issue> void appendIssueTimestamp (StringBuilder builder, SimpleDateFormat dateFormat, T issue) {
        builder.append(" (").append(dateFormat.format(issue.getTimestamp())).append(")");
    }

}