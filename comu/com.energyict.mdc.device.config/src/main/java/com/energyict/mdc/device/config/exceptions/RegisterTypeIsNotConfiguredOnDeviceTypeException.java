package com.energyict.mdc.device.config.exceptions;

import com.energyict.mdc.masterdata.MeasurementType;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Copyrights EnergyICT
 * Date: 18/02/14
 * Time: 15:13
 */
public class RegisterTypeIsNotConfiguredOnDeviceTypeException extends LocalizedException {

    public RegisterTypeIsNotConfiguredOnDeviceTypeException(MeasurementType measurementType, Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed, measurementType.getReadingType().getAliasName());
        set("measurementTypeId", measurementType.getId());
    }

}
