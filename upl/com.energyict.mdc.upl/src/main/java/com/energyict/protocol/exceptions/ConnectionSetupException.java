/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocol.exceptions;

import com.energyict.mdc.upl.nls.MessageSeed;

/**
 * Models the exceptional situation that occurs when the setup of the Connection failed.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-24 (15:48)
 */
public class ConnectionSetupException extends CommunicationException {

    public ConnectionSetupException(MessageSeed messageSeed, Object... messageArguments) {
        super(messageSeed, messageArguments);
    }

    public ConnectionSetupException(Throwable cause, MessageSeed messageSeed, Object... messageArguments) {
        super(cause, messageSeed, messageArguments);
    }
}