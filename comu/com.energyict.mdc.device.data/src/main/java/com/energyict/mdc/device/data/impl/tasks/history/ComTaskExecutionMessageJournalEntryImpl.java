package com.energyict.mdc.device.data.impl.tasks.history;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionMessageJournalEntry;
import com.energyict.mdc.device.data.tasks.history.JournalEntryVisitor;
import com.energyict.mdc.engine.model.ComServer;

import javax.inject.Inject;
import java.util.Date;

/**
 * Provides an implementation for the {@link ComTaskExecutionMessageJournalEntry} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-08-08 (10:51)
 */
public class ComTaskExecutionMessageJournalEntryImpl
        extends ComTaskExecutionJournalEntryImpl<ComTaskExecutionMessageJournalEntry>
        implements ComTaskExecutionMessageJournalEntry {

    private ComServer.LogLevel logLevel;
    private String message;

    @Inject
    ComTaskExecutionMessageJournalEntryImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus) {
        super(ComTaskExecutionMessageJournalEntry.class, dataModel, eventService, thesaurus);
    }

    @Override
    public ComServer.LogLevel getLogLevel() {
        return this.logLevel;
    }

    @Override
    public String getMessage () {
        return this.message;
    }

    @Override
    protected void doDelete() {
        // No specific actions required for deletion of this entity
    }

    @Override
    protected void validateDelete() {
        // No specific actions required to validate the deletion of this entity
    }

    @Override
    public void accept(JournalEntryVisitor visitor) {
        visitor.visit(this);
    }

    public static ComTaskExecutionMessageJournalEntryImpl from(DataModel dataModel, ComTaskExecutionSessionImpl comTaskExecutionSession, Date timestamp, String message, String errorDescription, ComServer.LogLevel logLevel) {
        return dataModel.getInstance(ComTaskExecutionMessageJournalEntryImpl.class).init(timestamp, comTaskExecutionSession, logLevel, message, errorDescription);
    }

    private ComTaskExecutionMessageJournalEntryImpl init(Date timestamp, ComTaskExecutionSessionImpl comTaskExecutionSession, ComServer.LogLevel logLevel, String message, String errorDescription) {
        this.comTaskExecutionSession.set(comTaskExecutionSession);
        this.timestamp = timestamp == null ? null : timestamp.toInstant();
        this.errorDescription = errorDescription;
        this.logLevel = logLevel;
        this.message = message;
        return this;
    }

}