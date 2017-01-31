/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.messaging;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

/**
 * Thrown when attempting to create a second subscriber on a single subscriber queue.
 */
public class AlreadyASubscriberForQueueException extends LocalizedException {

	private static final long serialVersionUID = 1L;
	private static final String DESTINATION = "destination";

    public AlreadyASubscriberForQueueException(Thesaurus thesaurus, DestinationSpec destinationSpec) {
        super(thesaurus, MessageSeeds.MULTIPLE_SUBSCRIBER_ON_QUEUE, destinationSpec);
        set(DESTINATION, destinationSpec);
    }
}
