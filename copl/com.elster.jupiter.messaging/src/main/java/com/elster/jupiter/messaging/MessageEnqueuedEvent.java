package com.elster.jupiter.messaging;

import oracle.jdbc.aq.AQMessage;

public final class MessageEnqueuedEvent {
    private final AQMessage message;

    public MessageEnqueuedEvent(AQMessage message) {
        this.message = message;
    }

    public AQMessage getMessage() {
        return message;
    }
}
