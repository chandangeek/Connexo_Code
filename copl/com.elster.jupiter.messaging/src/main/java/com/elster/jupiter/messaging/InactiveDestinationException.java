package com.elster.jupiter.messaging;

import com.elster.jupiter.util.exception.BaseException;

import java.text.MessageFormat;

/**
 * Thrown when trying to perform an operation on a DestinationSpec which requires it to be active, when it isn't.
 */
public class InactiveDestinationException extends BaseException {

    public InactiveDestinationException(DestinationSpec destinationSpec, String subscriberName) {
        super(ExceptionTypes.CANNOT_SUBSCRIBE_ON_INACTIVE_DESTINATION, MessageFormat.format("DestinationSpec with name {0} was inactive when attempting to create a subscription with name {1}", destinationSpec.getName(), subscriberName));
        set("destinationSpec", destinationSpec);
        set("subscriberName", subscriberName);
    }
}
