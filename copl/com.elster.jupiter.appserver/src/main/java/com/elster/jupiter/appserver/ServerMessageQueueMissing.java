package com.elster.jupiter.appserver;

import java.text.MessageFormat;

public class ServerMessageQueueMissing extends RuntimeException {

    public ServerMessageQueueMissing(String destinationName) {
        super(MessageFormat.format("Server's message queue with name ''{0}'' not found", destinationName));
    }
}
