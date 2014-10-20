package com.energyict.mdc.device.data.impl.tasks.history;

import com.energyict.mdc.device.data.tasks.history.ComCommandJournalEntry;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.device.data.tasks.history.JournalEntryVisitor;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;

import com.energyict.mdc.engine.model.ComServer;
import javax.inject.Inject;
import java.util.Date;

/**
 * Provides an implementation for the {@link ComCommandJournalEntry} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-08-08 (10:51)
 */
public class ComCommandJournalEntryImpl extends ComTaskExecutionJournalEntryImpl<ComCommandJournalEntry> implements ComCommandJournalEntry {

    private CompletionCode completionCode;
    private String commandDescription;

    @Inject
    ComCommandJournalEntryImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus) {
        super(ComCommandJournalEntry.class, dataModel, eventService, thesaurus);
    }

    @Override
    public CompletionCode getCompletionCode () {
        return completionCode;
    }

    @Override
    public String getCommandDescription () {
        return this.commandDescription;
    }

    @Override
    protected void doDelete() {
        this.dataModel.remove(this);
    }

    @Override
    protected void validateDelete() {
        // Nothing to validate right now
    }

    @Override
    public void accept(JournalEntryVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ComServer.LogLevel getLogLevel() {
        return ComServer.LogLevel.INFO;
    }

    public static ComCommandJournalEntryImpl from(DataModel dataModel, ComTaskExecutionSession comTaskExecutionSession, Date timestamp, CompletionCode completionCode, String errorDescription, String commandDescription) {
        ComCommandJournalEntryImpl instance = dataModel.getInstance(ComCommandJournalEntryImpl.class);
        return instance.init(comTaskExecutionSession, timestamp, completionCode, errorDescription, commandDescription);
    }

    private ComCommandJournalEntryImpl init(ComTaskExecutionSession comTaskExecutionSession, Date timestamp, CompletionCode completionCode, String errorDescription, String commandDescription) {
        this.comTaskExecutionSession.set(comTaskExecutionSession);
        this.completionCode = completionCode;
        this.errorDescription = errorDescription;
        this.timestamp = timestamp == null ? null : timestamp.toInstant();
        this.commandDescription = commandDescription;
        return this;
    }

}