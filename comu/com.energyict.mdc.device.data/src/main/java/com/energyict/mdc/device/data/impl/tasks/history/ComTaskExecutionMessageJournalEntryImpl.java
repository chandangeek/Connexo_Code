package com.energyict.mdc.device.data.impl.tasks.history;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.UtcInstant;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionMessageJournalEntry;
import com.energyict.mdc.device.data.tasks.history.JournalEntryVisitor;

import javax.inject.Inject;
import java.util.Date;

/**
 * Provides an implementation for the {@link ComTaskExecutionMessageJournalEntry} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-08-08 (10:51)
 */
public class ComTaskExecutionMessageJournalEntryImpl extends ComTaskExecutionJournalEntryImpl<ComTaskExecutionMessageJournalEntry> implements ComTaskExecutionMessageJournalEntry {

    private String message;

    @Inject
    ComTaskExecutionMessageJournalEntryImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus) {
        super(ComTaskExecutionMessageJournalEntry.class, dataModel, eventService, thesaurus);
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

    public static ComTaskExecutionMessageJournalEntryImpl from(DataModel dataModel, ComTaskExecutionSessionImpl comTaskExecutionSession, Date timestamp, String errorDescription, String message) {
        return dataModel.getInstance(ComTaskExecutionMessageJournalEntryImpl.class).init(timestamp, comTaskExecutionSession, errorDescription, message);
    }

    private ComTaskExecutionMessageJournalEntryImpl init(Date timestamp, ComTaskExecutionSessionImpl comTaskExecutionSession, String errorDescription, String message) {
        this.comTaskExecutionSession.set(comTaskExecutionSession);
        this.timestamp = new UtcInstant(timestamp);
        this.errorDescription = errorDescription;
        this.message = message;
        return this;
    }

}