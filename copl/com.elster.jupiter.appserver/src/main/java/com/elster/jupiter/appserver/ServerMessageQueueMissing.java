/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.appserver;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

/**
 * Thrown when the server's message queue can not be found. Likely the app server module needs to be installed.
 */
public class ServerMessageQueueMissing extends LocalizedException {

	private static final long serialVersionUID = 1L;

	public ServerMessageQueueMissing(String destinationName, Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.SERVER_MESSAGE_QUEUE_MISSING, destinationName);
        set("destination", destinationName);
    }
}
