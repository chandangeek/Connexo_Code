package com.energyict.mdc.device.config.exceptions;

import com.energyict.mdc.masterdata.MeasurementType;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Models the exceptional situation that occurs when an attempt
 * is made to update the {@link com.energyict.mdc.common.ObisCode}
 * of a {@link com.energyict.mdc.masterdata.MeasurementType} that is in use.
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