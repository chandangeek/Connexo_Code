package com.elster.jupiter.appserver;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

/**
 * Thrown when the server's message queue can not be found. Likely the app server module needs to be installed.
 */
public class ServerMessageQueueMissing extends LocalizedException {

    public ServerMessageQueueMissing(String destinationName, Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.SERVER_MESSAGE_QUEUE_MISSING, destinationName, thesaurus);
        set("destination", destinationName);
    }
}
