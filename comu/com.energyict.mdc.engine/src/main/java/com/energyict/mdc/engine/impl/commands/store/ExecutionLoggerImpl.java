package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.device.data.journal.CompletionCode;
import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.StackTracePrinter;
import com.energyict.mdc.tasks.history.ComSessionBuilder;
import com.energyict.mdc.tasks.history.ComTaskExecutionSessionBuilder;
import com.energyict.mdc.tasks.history.CompletionCode;

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

    protected abstract ComSessionBuilder.EndedComSessionBuilder getComSessionBuilder();

    @Override
    public void executed (DeviceCommand deviceCommand) {
        if (this.isLogLevelEnabled(deviceCommand)) {
            ComSessionJournalEntryShadow shadow = new ComSessionJournalEntryShadow();
            shadow.setMessage(deviceCommand.toJournalMessageDescription(this.logLevel));
            shadow.setTimestamp(Clocks.getAppServerClock().now());
            this.getComSessionBuilder().addJournaleEntry(shadow);
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
        for (ComTaskExecutionSessionShadow shadow : this.getComSessionBuilder().getComTaskExecutionSessionShadows()) {
            if (shadow.getComTaskExecutionId() == comTaskExecution.getId()) {
                return shadow;
            }
        }
        throw CodingException.comTaskSessionMissing(comTaskExecution);
    }

    private void logFailure (Throwable t, ComTaskExecutionSessionBuilder builder) {
        builder.addComCommandJournalEntry(clock.now(), CompletionCode.UnexpectedError, StackTracePrinter.print(t), "General");
    }

    @Override
    public void addIssue(CompletionCode completionCode, Issue issue, ComTaskExecution comTaskExecution){
        this.logIssue(completionCode, issue, this.findComTaskExecutionSession(comTaskExecution));
    }

    private void logIssue(CompletionCode completionCode, Issue issue, ComTaskExecutionSessionBuilder builder) {
        builder.addComCommandJournalEntry(issue.getTimestamp(), completionCode, issue.isProblem() ? issue.getDescription() : "", issue.getDescription());
    }

}