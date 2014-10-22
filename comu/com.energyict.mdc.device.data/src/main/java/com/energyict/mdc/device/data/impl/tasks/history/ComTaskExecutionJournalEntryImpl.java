package com.energyict.mdc.device.data.impl.tasks.history;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionJournalEntry;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.google.common.collect.ImmutableMap;
import java.time.Instant;
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

    public static long ComCommandJournalEntryImplDiscriminator = 0;
    public static long ComTaskExecutionMessageJournalEntryImplDiscriminator = 1;

    public static final Map<String, Class<? extends ComTaskExecutionJournalEntry>> IMPLEMENTERS =
            ImmutableMap.<String, Class<? extends ComTaskExecutionJournalEntry>>of(
                    String.valueOf(ComCommandJournalEntryImplDiscriminator), ComCommandJournalEntryImpl.class,
                    String.valueOf(ComTaskExecutionMessageJournalEntryImplDiscriminator), ComTaskExecutionMessageJournalEntryImpl.class);

    enum Fields {
        ComTaskExecutionSession("comTaskExecutionSession"),
        LogLevel("logLevel"),
        timestamp("timestamp");
        private final String fieldName;

        Fields(String fieldName) {
            this.fieldName = fieldName;
        }

        public String fieldName() {
            return fieldName;
        }
    }

    Reference<ComTaskExecutionSession> comTaskExecutionSession = ValueReference.absent();

    String errorDescription;
    Instant timestamp;
    private Instant modDate;

    ComTaskExecutionJournalEntryImpl(Class<T> domainClass, DataModel dataModel, EventService eventService, Thesaurus thesaurus) {
        super(domainClass, dataModel, eventService, thesaurus);
    }

    @Override
    public ComTaskExecutionSession getComTaskExecutionSession () {
        return this.comTaskExecutionSession.get();
    }

    @Override
    public Instant getTimestamp () {
        return this.timestamp;
    }

    @Override
    public String getErrorDescription () {
        return errorDescription;
    }

}
