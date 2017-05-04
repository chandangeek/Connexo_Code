/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.exceptions;

import com.energyict.mdc.protocol.api.MessageSeeds;

/**
 * Models the exceptional situation that occurs when
 * an ObisCode could not be parsed from its String representation.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-08-27 (15:08)
 */
public class ObisCodeParseException extends GeneralParseException {

    public ObisCodeParseException(IllegalArgumentException cause) {
        super(MessageSeeds.COULD_NOT_PARSE_OBIS_CODE, cause);
    }

}