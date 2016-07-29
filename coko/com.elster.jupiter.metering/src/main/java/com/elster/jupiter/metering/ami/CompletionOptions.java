package com.elster.jupiter.metering.ami;

import com.elster.jupiter.messaging.DestinationSpec;

public interface CompletionOptions {

    void whenFinishedSendCompletionMessageWith(String correlationId, DestinationSpec destinationSpec);

}