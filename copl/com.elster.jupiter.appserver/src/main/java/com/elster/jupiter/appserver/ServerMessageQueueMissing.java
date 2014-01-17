package com.elster.jupiter.appserver;

import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.BaseException;

/**
 * Thrown when the server's message queue can not be found. Likely the app server module needs to be installed.
 */
public class ServerMessageQueueMissing extends BaseException {

    public ServerMessageQueueMissing(String destinationName, Thesaurus thesaurus) {
        super(ExceptionTypes.SERVER_MESSAGE_QUEUE_MISSING, buildMessage(destinationName, thesaurus));
        set("destination", destinationName);
    }

    private static String buildMessage(String destinationName, Thesaurus thesaurus) {
        NlsMessageFormat format = thesaurus.getFormat(MessageSeeds.SERVER_MESSAGE_QUEUE_MISSING);
        return format.format(destinationName);
    }
}
