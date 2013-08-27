package com.elster.jupiter.messaging;

import org.joda.time.Seconds;

/**
 * Builder interface for messages.
 */
public interface MessageBuilder {

    void send();

    MessageBuilder expiringAfter(Seconds seconds);

}
