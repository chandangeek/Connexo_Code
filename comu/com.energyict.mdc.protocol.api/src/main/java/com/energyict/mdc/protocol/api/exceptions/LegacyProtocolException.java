package com.energyict.mdc.protocol.api.exceptions;

import com.elster.jupiter.util.exception.MessageSeed;

import java.io.IOException;

/**
 * Should only be used by the Adapter classes if {@link IOException}s are thrown by any of their interfaces.
 *
 * @author gna
 * @since 29/03/12 - 10:05
 */
public class LegacyProtocolException extends ComServerRuntimeException {

    public LegacyProtocolException(MessageSeed messageSeed, IOException cause){
        super(cause, messageSeed, cause.getMessage());
    }

}