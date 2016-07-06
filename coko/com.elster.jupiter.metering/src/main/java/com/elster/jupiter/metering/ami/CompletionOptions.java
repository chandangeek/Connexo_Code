package com.elster.jupiter.metering.ami;

import com.elster.jupiter.messaging.DestinationSpec;

public interface CompletionOptions {

    @Deprecated
        // temporary, should be replaced by #whenFinishedSend(com.elster.jupiter.metering.ami.CompletionMessageInfo, com.elster.jupiter.messaging.DestinationSpec)
    void whenFinishedSend(String message, DestinationSpec destinationSpec);

    void whenFinishedSend(CompletionMessageInfo message, DestinationSpec destinationSpec);
}
