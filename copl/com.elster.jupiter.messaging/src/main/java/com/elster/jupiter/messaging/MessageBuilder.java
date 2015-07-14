package com.elster.jupiter.messaging;

import aQute.bnd.annotation.ProviderType;

import java.time.Duration;

/**
 * Builder interface for messages.
 */
@ProviderType
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
    MessageBuilder expiringAfter(Duration duration);

    MessageBuilder withCorrelationId(String correlationId);

}
