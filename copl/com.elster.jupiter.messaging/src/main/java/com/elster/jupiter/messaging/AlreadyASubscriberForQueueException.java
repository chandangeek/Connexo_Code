package com.elster.jupiter.messaging;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.BaseException;

/**
 * Thrown when attempting to create a second subscriber on a single subscriber queue.
 */
public class AlreadyASubscriberForQueueException extends BaseException {

    public AlreadyASubscriberForQueueException(Thesaurus thesaurus, DestinationSpec destinationSpec) {
        super(ExceptionTypes.MULTIPLE_SUBSCRIBER_ON_QUEUE, buildMessage(thesaurus, destinationSpec));
        set("destination", destinationSpec);
    }

    private static String buildMessage(Thesaurus thesaurus, DestinationSpec destinationSpec) {
        return thesaurus.getFormat(MessageSeeds.MULTIPLE_SUBSCRIBER_ON_QUEUE).format(destinationSpec.getName());
    }
}
