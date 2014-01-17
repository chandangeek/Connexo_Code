package com.energyict.mdc.protocol.api.exceptions;

import com.energyict.mdc.common.exceptions.ExceptionReferenceScope;

/**
 * Models the scope for exceptions defined by the protocols api.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-07 (13:39)
 */
public class ProtocolsExceptionReferenceScope implements ExceptionReferenceScope {

    @Override
    public String resourceKeyPrefix () {
        return "PRA";   // As in PRotocols API
    }

}