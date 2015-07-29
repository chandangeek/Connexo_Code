package com.energyict.mdc.device.data.tasks.history;

import com.elster.jupiter.util.HasId;
import com.energyict.mdc.engine.config.ComServer;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;

/**
 * Models an entry in the journal of a {@link ComTaskExecutionSession}.
 * All ComTaskSessionJournalEntries will provide a complete overview
 * of events that happened during the ComTaskExecutionSession.
 * <br>
 * Copyrights EnergyICT
 *
 * @author sva
 * @since 23/04/12 (14:27)
 */
@ProviderType
public interface ComTaskExecutionJournalEntry extends HasId {

    public ComTaskExecutionSession getComTaskExecutionSession ();

    public Instant getTimestamp ();

    public String getErrorDescription ();

    public void accept(JournalEntryVisitor visitor);

    /**
     * Gets the level at which this message journal entry was logged.
     *
     * @return The LogLevel
     */
    public ComServer.LogLevel getLogLevel();

}