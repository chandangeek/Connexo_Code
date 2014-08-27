package com.energyict.protocols.mdc.exceptions;

import com.energyict.mdc.protocol.api.exceptions.ComServerRuntimeException;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.protocols.mdc.services.impl.MessageSeeds;

/**
 * Models the exceptional situations that occur when a developer has
 * forgotton or neglected to comply with coding standards
 * or constraints imposed by common framework components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-05-15 (09:02)
 */
public final class AsynchroneousCommunicationIsNotSupportedException extends ComServerRuntimeException {

    public AsynchroneousCommunicationIsNotSupportedException () {
        this(MessageSeeds.ASYNCHRONEOUS_COMMUNICATION_IS_NOT_SUPPORTED);
    }

    private AsynchroneousCommunicationIsNotSupportedException(MessageSeed messageSeed, Object... messageArguments) {
        super(messageSeed, messageArguments);
    }

}