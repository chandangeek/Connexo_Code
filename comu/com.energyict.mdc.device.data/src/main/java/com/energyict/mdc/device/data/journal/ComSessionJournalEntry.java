package com.energyict.mdc.device.data.journal;

import com.energyict.mdc.common.IdBusinessObject;

import java.util.Date;

/**
 * Models an entry in the journal of a {@link ComSession}.
 * All ComSessionJournalEntries will provide a complete overview
 * of events that happened during the ComSession.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-07-27 (16:47)
 */
public interface ComSessionJournalEntry extends IdBusinessObject {

    /**
     * Gets the {@link ComSession} to which this entry belongs.
     *
     * @return The ComSession
     */
    public ComSession getComSession ();

    /**
     * Gets the Date on which this ComSessionJournalEntry was created,
     * i.e. the Date on which the event that caused this ComSessionJournalEntry
     * to be created occurred.
     *
     * @return The Date
     */
    public Date getTimestamp ();

    /**
     * Gets the message that describes the event that caused this
     * ComSessionJournalEntry to be created.
     *
     * @return The message
     */
    public String getMessage ();

    /**
     * Gets the printed version of the error that caused this
     * ComSessionJournalEntry to be created.
     *
     * @return The printed version of the error
     * @see Exception#printStackTrace()
     */
    public String getStackTrace();

}