package com.energyict.mdc.device.config.exceptions;

import com.energyict.mdc.masterdata.MeasurementType;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

/**
 * Copyrights EnergyICT
 * Date: 18/02/14
 * Time: 15:13
 */
public class RegisterTypeIsNotConfiguredOnDeviceTypeException extends LocalizedException {

    public RegisterTypeIsNotConfiguredOnDeviceTypeException(Thesaurus thesaurus, MeasurementType measurementType) {
        super(thesaurus, MessageSeeds.REGISTER_SPEC_REGISTER_TYPE_IS_NOT_ON_DEVICE_TYPE, measurementType.getName());
        set("measurementTypeId", measurementType.getId());
    }

}
