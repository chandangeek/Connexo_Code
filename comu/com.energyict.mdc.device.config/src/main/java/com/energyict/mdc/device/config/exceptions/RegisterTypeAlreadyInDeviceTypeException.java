package com.energyict.mdc.device.config.exceptions;

import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.masterdata.MeasurementType;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to add a {@link com.energyict.mdc.masterdata.MeasurementType} to a {@link DeviceType}
 * but that RegisterType was already added to the DeviceType before.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-02-10 (16:54)
 */
public class RegisterTypeAlreadyInDeviceTypeException extends LocalizedException {

    public RegisterTypeAlreadyInDeviceTypeException(DeviceType deviceType, MeasurementType measurementType, Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed, measurementType.getReadingType().getAliasName(), deviceType.getName());
        this.set("deviceType", deviceType);
        this.set("measurementType", measurementType);
    }

}