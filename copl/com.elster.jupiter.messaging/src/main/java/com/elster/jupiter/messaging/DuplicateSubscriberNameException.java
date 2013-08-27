package com.elster.jupiter.messaging;

import com.elster.jupiter.util.exception.BaseException;

import java.text.MessageFormat;

/**
 * Thrown when attempting to create a subscriber on a destination with the same name as an already registered subscriber.
 */
public class DuplicateSubscriberNameException extends BaseException {

    public DuplicateSubscriberNameException(String name) {
        super(ExceptionTypes.CANNOT_SUBSCRIBE_ON_INACTIVE_DESTINATION, MessageFormat.format("A subscriber with name {0} already exists.", name));
        set("name", name);
    }
}
