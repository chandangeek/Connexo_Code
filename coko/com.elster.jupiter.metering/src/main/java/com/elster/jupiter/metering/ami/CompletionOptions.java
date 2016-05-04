package com.elster.jupiter.metering.ami;

import com.elster.jupiter.messaging.DestinationSpec;

public interface CompletionOptions {

    void whenFinishedSend(String message, DestinationSpec destinationSpec);
    CompletionOptions setDestination(DestinationSpec destination);
    CompletionOptions setMessage(String message);
}
