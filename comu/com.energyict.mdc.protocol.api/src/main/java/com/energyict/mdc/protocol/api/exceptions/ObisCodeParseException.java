/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.exceptions;

import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Models the exceptional situation that occurs when
 * an ObisCode could not be parsed from its String representation.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-08-27 (15:08)
 */
public class ObisCodeParseException extends GeneralParseException {

    public ObisCodeParseException(MessageSeed messageSeed, Exception cause) {
        super(messageSeed, cause);
    }

}