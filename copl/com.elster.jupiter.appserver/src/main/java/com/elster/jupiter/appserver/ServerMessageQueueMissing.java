package com.elster.jupiter.appserver;

import com.elster.jupiter.util.exception.BaseException;

import java.text.MessageFormat;

public class ServerMessageQueueMissing extends BaseException {

    public ServerMessageQueueMissing(String destinationName) {
        super(ExceptionTypes.SERVER_MESSAGE_QUEUE_MISSING, MessageFormat.format("Server's message queue with name ''{0}'' not found", destinationName));
    }
}
