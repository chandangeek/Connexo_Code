package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.io.SerialComChannel;
import com.energyict.mdc.protocol.ComChannel;

import java.time.Duration;

/**
 * Wraps {@link ComChannel} to add that the ComChannel
 * is actually connected from a {@link ComPort}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-06-19 (14:07)
 */
public interface ComPortRelatedComChannel extends ComChannel, SerialComChannel {

    ComPort getComPort();

    void setComPort(ComPort comPort);

    ComChannel getActualComChannel();

    void setJournalEntryFactory(JournalEntryFactory journalEntryFactory);

    void logRemainingBytes();

    Duration talkTime();

    Counters getSessionCounters();

    Counters getTaskSessionCounters();

}