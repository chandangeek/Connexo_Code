package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.comserver.exceptions.CodingException;
import com.energyict.comserver.time.Clocks;
import com.energyict.mdc.device.data.journal.CompletionCode;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.journal.StackTracePrinter;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.shadow.journal.ComCommandJournalEntryShadow;
import com.energyict.mdc.shadow.journal.ComSessionJournalEntryShadow;
import com.energyict.mdc.shadow.journal.ComSessionShadow;
import com.energyict.mdc.shadow.journal.ComTaskExecutionSessionShadow;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;

/**
 * Provides an implementation for the {@link DeviceCommand.ExecutionLogger} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-09-17 (16:45)
 */
public abstract class ExecutionLoggerImpl implements DeviceCommand.ExecutionLogger {

    private ComServer.LogLevel logLevel;

    protected ExecutionLoggerImpl (ComServer.LogLevel logLevel) {
        super();
        this.logLevel = logLevel;
    }

    protected abstract ComSessionShadow getComSessionShadow();

    @Override
    public void executed (DeviceCommand deviceCommand) {
        if (this.isLogLevelEnabled(deviceCommand)) {
            ComSessionJournalEntryShadow shadow = new ComSessionJournalEntryShadow();
            shadow.setMessage(deviceCommand.toJournalMessageDescription(this.logLevel));
            shadow.setTimestamp(Clocks.getAppServerClock().now());
            this.getComSessionShadow().addJournaleEntry(shadow);
        }
    }

    /**
     * Tests if the {@link DeviceCommand}'s LogLevel is enabled,
     * in which case it will be logged in the ComSession.
     *
     * @param deviceCommand The DeviceCommand
     * @return A flag that indicates if the DeviceCommand should be logged
     */
    private boolean isLogLevelEnabled (DeviceCommand deviceCommand) {
        return this.logLevel.compareTo(deviceCommand.getJournalingLogLevel()) >= 0;
    }

    @Override
    public void logUnexpected (Throwable t, ComTaskExecution comTaskExecution) {
        this.logFailure(t, this.findComTaskExecutionSession(comTaskExecution));
    }

    private ComTaskExecutionSessionShadow findComTaskExecutionSession (ComTaskExecution comTaskExecution) {
        for (ComTaskExecutionSessionShadow shadow : this.getComSessionShadow().getComTaskExecutionSessionShadows()) {
            if (shadow.getComTaskExecutionId() == comTaskExecution.getId()) {
                return shadow;
            }
        }
        throw CodingException.comTaskSessionMissing(comTaskExecution);
    }

    private void logFailure (Throwable t, ComTaskExecutionSessionShadow shadow) {
        ComCommandJournalEntryShadow comCommandJournalEntryShadow = new ComCommandJournalEntryShadow();
        comCommandJournalEntryShadow.setCompletionCode(CompletionCode.UnexpectedError);
        comCommandJournalEntryShadow.setErrorDescription(StackTracePrinter.print(t));
        comCommandJournalEntryShadow.setTimestamp(Clocks.getAppServerClock().now());
        comCommandJournalEntryShadow.setCommandDescription("General");
        shadow.addComTaskJournalEntry(comCommandJournalEntryShadow);
    }

    @Override
    public void addIssue(CompletionCode completionCode, Issue issue, ComTaskExecution comTaskExecution){
        this.logIssue(completionCode, issue, this.findComTaskExecutionSession(comTaskExecution));
    }

    private void logIssue(CompletionCode completionCode, Issue issue, ComTaskExecutionSessionShadow comTaskExecutionSession) {
        ComCommandJournalEntryShadow comCommandJournalEntryShadow = new ComCommandJournalEntryShadow();
        comCommandJournalEntryShadow.setCompletionCode(completionCode);
        comCommandJournalEntryShadow.setCommandDescription(issue.getDescription());
        comCommandJournalEntryShadow.setTimestamp(issue.getTimestamp());
        if (issue.isProblem()) {
            comCommandJournalEntryShadow.setErrorDescription(issue.getDescription());
        }
        comTaskExecutionSession.addComTaskJournalEntry(comCommandJournalEntryShadow);
    }

}