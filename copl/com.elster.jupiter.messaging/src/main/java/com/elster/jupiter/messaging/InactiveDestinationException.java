package com.elster.jupiter.messaging;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.BaseException;

/**
 * Thrown when trying to perform an operation on a DestinationSpec which requires it to be active, when it isn't.
 */
public class InactiveDestinationException extends BaseException {

    public InactiveDestinationException(Thesaurus thesaurus, DestinationSpec destinationSpec, String subscriberName) {
        super(ExceptionTypes.CANNOT_SUBSCRIBE_ON_INACTIVE_DESTINATION, buildMessage(thesaurus, destinationSpec, subscriberName));
        set("destinationSpec", destinationSpec);
        set("subscriberName", subscriberName);
    }

    private static String buildMessage(Thesaurus thesaurus, DestinationSpec destinationSpec, String subscriberName) {
        return thesaurus.getFormat(MessageSeeds.CANNOT_SUBSCRIBE_ON_INACTIVE_DESTINATION).format(destinationSpec.getName(), subscriberName);
    }
}
