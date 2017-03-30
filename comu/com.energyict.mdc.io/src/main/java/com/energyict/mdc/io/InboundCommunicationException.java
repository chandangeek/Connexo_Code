/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.io;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.common.ComServerRuntimeException;

import java.io.IOException;

public class InboundCommunicationException extends ComServerRuntimeException {

    /**
     * Exception indicating that the <b>setup</b> of the inbound call failed.
     *
     * @param cause the actual cause of the exception
     */
    public InboundCommunicationException(MessageSeed messageSeed, IOException cause) {
        super(messageSeed, cause);
    }

    /**
     * Exception indicating that the <b>setup</b> of the inbound call failed.
     *
     * @param cause the actual cause of the exception
     */
    public InboundCommunicationException(MessageSeed messageSeed, Exception cause) {
        super(messageSeed, cause);
    }

}