/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.tasks.history;

import com.energyict.mdc.common.comserver.ComServer;

import aQute.bnd.annotation.ConsumerType;

import java.time.Instant;

/**
 * Models an entry in the journal of a {@link ComSession}.
 * All ComSessionJournalEntries will provide a complete overview
 * of events that happened during the ComSession.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-07-27 (16:47)
 */
@ConsumerType
public interface ComSessionJournalEntry {

    /**
     * Gets the {@link ComSession} to which this entry belongs.
     *
     * @return The ComSession
     */
    ComSession getComSession();

    /**
     * Set the {@link ComSession} to which this entry belongs.
     *
     * @arg The ComSession
     */
    void setComSession (ComSession comSession);

    /**
     * Gets the Date on which this ComSessionJournalEntry was created,
     * i.e. the Date on which the event that caused this ComSessionJournalEntry
     * to be created occurred.
     *
     * @return The Date
     */
    Instant getTimestamp();

    /**
     * Gets the level at which this message journal entry was logged.
     *
     * @return The LogLevel
     */
    ComServer.LogLevel getLogLevel();

    /**
     * Gets the message that describes the event that caused this
     * ComSessionJournalEntry to be created.
     *
     * @return The message
     */
    String getMessage();

    /**
     * Gets the printed version of the error that caused this
     * ComSessionJournalEntry to be created.
     *
     * @return The printed version of the error
     * @see Exception#printStackTrace()
     */
    String getStackTrace();

}