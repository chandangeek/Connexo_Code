package com.elster.jupiter.messaging;

import com.elster.jupiter.util.exception.BaseException;

import java.text.MessageFormat;

public class InactiveDestinationException extends BaseException {

    public InactiveDestinationException(DestinationSpec destinationSpec, String subscriberName) {
        super(ExceptionTypes.CANNOT_SUBSCRIBE_ON_INACTIVE_DESTINATION, MessageFormat.format("DestinationSpec with name {0} was inactive when attempting to create a subscription with name {1}", destinationSpec.getName(), subscriberName));
        set("destinationSpec", destinationSpec);
        set("subscriberName", subscriberName);
    }
}
