/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.masterdata.LogBookType;

/**
 * Models the exceptional situation that occurs when an attempt
 * is made to update the {@link com.energyict.obis.ObisCode}
 * of a {@link LogBookType} that is in use.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-31 (09:03)
 */
public class CannotUpdateObisCodeWhenLogBookTypeIsInUseException extends LocalizedException {

    public CannotUpdateObisCodeWhenLogBookTypeIsInUseException(Thesaurus thesaurus, LogBookType logBookType, MessageSeed messageSeed) {
        super(thesaurus, messageSeed, logBookType.getName());
        this.set("logBookType", logBookType);
    }

}