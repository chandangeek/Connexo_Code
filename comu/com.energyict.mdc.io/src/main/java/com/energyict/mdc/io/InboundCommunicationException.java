package com.energyict.mdc.io;

import com.energyict.mdc.common.ComServerRuntimeException;
import com.energyict.mdc.io.CommunicationException;

import com.elster.jupiter.util.exception.MessageSeed;

import java.io.IOException;

/**
 * Wrapper around a {@link CommunicationException} indicating that an exception occurred
 * during the <b>setup</b> of an inbound call.
 * <p/>
 * Copyrights EnergyICT
 * Date: 5/11/12
 * Time: 14:26
 */
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