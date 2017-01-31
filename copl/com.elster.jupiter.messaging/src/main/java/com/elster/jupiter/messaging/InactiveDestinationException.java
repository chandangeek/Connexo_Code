/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.messaging;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

/**
 * Thrown when trying to perform an operation on a DestinationSpec which requires it to be active, when it isn't.
 */
public class InactiveDestinationException extends LocalizedException {

	private static final long serialVersionUID = 1L;
	private static final String DESTINATION = "destinationSpec";
    private static final String SUBSCRIBER_NAME = "subscriberName";

    public InactiveDestinationException(Thesaurus thesaurus, DestinationSpec destinationSpec, String subscriberName) {
        super(thesaurus, MessageSeeds.CANNOT_SUBSCRIBE_ON_INACTIVE_DESTINATION, destinationSpec.getName(), subscriberName);
        set(DESTINATION, destinationSpec);
        set(SUBSCRIBER_NAME, subscriberName);
    }

}
