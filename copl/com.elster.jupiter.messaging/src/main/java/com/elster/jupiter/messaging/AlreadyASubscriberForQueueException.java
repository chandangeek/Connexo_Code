package com.elster.jupiter.messaging;

import com.elster.jupiter.util.exception.BaseException;

import java.text.MessageFormat;

/**
 * Thrown when attempting to create a second subscriber on a single subscriber queue.
 */
public class AlreadyASubscriberForQueueException extends BaseException {

    public AlreadyASubscriberForQueueException(DestinationSpec destinationSpec) {
        super(ExceptionTypes.MULTIPLE_SUBSCRIBER_ON_QUEUE, MessageFormat.format("Cannot register multiple subscribers on a queue, there is already a subscriber on queue {0}", destinationSpec.getName()));
        set("destination", destinationSpec);
    }
}
