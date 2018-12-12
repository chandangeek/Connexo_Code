/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocol.exceptions;

import com.energyict.mdc.upl.nls.MessageSeed;

/**
 * Models exceptional situations that occur while working with a AT serial modem components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-09 (16:01)
 */
public class ModemException extends CommunicationException {

    public ModemException(MessageSeed messageSeed, Object... messageArguments) {
        super(messageSeed, messageArguments);
    }

    public ModemException(Throwable cause, MessageSeed messageSeed, Object... messageArguments) {
        super(cause, messageSeed, messageArguments);
    }
}