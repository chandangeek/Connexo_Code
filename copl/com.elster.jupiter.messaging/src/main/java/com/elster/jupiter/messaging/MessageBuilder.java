package com.elster.jupiter.messaging;

import org.joda.time.Seconds;

/**
 * Builder interface for messages.
 */
public interface MessageBuilder {

    /**
     * Sends the Message under construction.
     */
    void send();

    /**
     * Makes the message under construction expiring after the given number of seconds.
     * @param seconds
     * @return the messageBuilder, for chaining calls.
     */
    MessageBuilder expiringAfter(Seconds seconds);

}
