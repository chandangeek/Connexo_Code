/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.messaging;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

/**
 * Thrown when attempting to create a subscriber on a destination with the same name as an already registered subscriber.
 */
public class DuplicateSubscriberNameException extends LocalizedException {

	private static final long serialVersionUID = 1L;

	public DuplicateSubscriberNameException(Thesaurus thesaurus, String name) {
        super(thesaurus, MessageSeeds.DUPLICATE_SUBSCRIBER_NAME, name);
        set("name", name);
    }
}
