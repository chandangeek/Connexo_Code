package com.energyict.mdc.device.data.impl.tasks.history;

import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionJournalEntry;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.UtcInstant;
import com.google.common.collect.ImmutableMap;

import java.util.Date;
import java.util.Map;

/**
 * Serves as the root for ComTaskExecutionJournalEntry, implementation classes.
 * <br>
 * Copyrights EnergyICT
 *
 * User: sva
 * Date: 24/04/12
 * Time: 10:00
 */
public abstract class ComTaskExecutionJournalEntryImpl<T extends ComTaskExecutionJournalEntry> extends PersistentIdObject<T> implements ComTaskExecutionJournalEntry {

    public static final Map<String, Class<? extends ComTaskExecutionJournalEntry>> IMPLEMENTERS = ImmutableMap.<String, Class<? extends ComTaskExecutionJournalEntry>>of("0", ComCommandJournalEntryImpl.class, "1", ComTaskExecutionMessageJournalEntryImpl.class);

    Reference<ComTaskExecutionSession> comTaskExecutionSession = ValueReference.absent();

    String errorDescription;
    UtcInstant timestamp;
    private Date modDate;

    ComTaskExecutionJournalEntryImpl(Class<T> domainClass, DataModel dataModel, EventService eventService, Thesaurus thesaurus) {
        super(domainClass, dataModel, eventService, thesaurus);
    }

    @Override
    public ComTaskExecutionSession getComTaskExecutionSession () {
        return this.comTaskExecutionSession.get();
    }

    @Override
    public Date getTimestamp () {
        return timestamp.toDate();
    }

    @Override
    public String getErrorDescription () {
        return errorDescription;
    }

}
