/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.exceptions;

import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Models the exceptional situation that occurs when
 * the className of the primary key of a message spec
 * could not be located on the classPath.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-08-27 (13:29)
 */
public class UnknownDeviceMessageSpecClass extends CodingException {

    protected UnknownDeviceMessageSpecClass(MessageSeed messageSeed, Object... messageArguments) {
        super(messageSeed, messageArguments);
    }

}