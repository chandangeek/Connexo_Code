package com.elster.jupiter.messaging;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.BaseException;

/**
 * Thrown when attempting to create a subscriber on a destination with the same name as an already registered subscriber.
 */
public class DuplicateSubscriberNameException extends BaseException {

    public DuplicateSubscriberNameException(Thesaurus thesaurus, String name) {
        super(ExceptionTypes.DUPLICATE_SUBSCRIBER_NAME, buildMessage(thesaurus, name));
        set("name", name);
    }

    private static String buildMessage(Thesaurus thesaurus, String name) {
        return thesaurus.getFormat(MessageSeeds.DUPLICATE_SUBSCRIBER_NAME).format(name);
    }
}
