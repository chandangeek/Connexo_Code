/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.io.impl;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.common.ComServerRuntimeException;

/**
 * Models the exceptional situations that occur when a developer has
 * forgotten or neglected to comply with coding standards
 * or constraints imposed by common framework components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-05-15 (09:02)
 */
public final class AsynchronousCommunicationIsNotSupportedException extends ComServerRuntimeException {

    public AsynchronousCommunicationIsNotSupportedException() {
        this(MessageSeeds.ASYNCHRONOUS_COMMUNICATION_IS_NOT_SUPPORTED);
    }

    private AsynchronousCommunicationIsNotSupportedException(MessageSeed messageSeed, Object... messageArguments) {
        super(messageSeed, messageArguments);
    }

}