/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.common.masterdata.MeasurementType;

/**
 * Models the exceptional situation that occurs when an attempt
 * is made to update the {@link com.energyict.obis.ObisCode}
 * of a {@link MeasurementType} that is in use.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-30 (11:22)
 */
public class CannotUpdateObisCodeWhenMeasurementTypeIsInUseException extends LocalizedException {

    public CannotUpdateObisCodeWhenMeasurementTypeIsInUseException(Thesaurus thesaurus, MeasurementType measurementType, MessageSeed messageSeed) {
        super(thesaurus, messageSeed, measurementType.getReadingType().getAliasName());
        this.set("measurementType", measurementType);
    }

}