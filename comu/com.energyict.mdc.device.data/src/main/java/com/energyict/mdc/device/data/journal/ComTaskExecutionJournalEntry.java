package com.energyict.mdc.device.data.journal;

import com.energyict.mdc.common.IdBusinessObject;

import java.util.Date;

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
public interface ComTaskExecutionJournalEntry extends IdBusinessObject {

    public ComTaskExecutionSession getComTaskExecutionSession ();

    public Date getTimestamp ();

    public String getErrorDescription ();

}