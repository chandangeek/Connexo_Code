/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.io;

import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Models a ModemException that was caused by a timeout.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-08-27 (10:10)
 */
public class ModemTimeoutException extends ModemException {

    public ModemTimeoutException(MessageSeed messageSeed, Object... messageArguments) {
        super(messageSeed, messageArguments);
    }

}