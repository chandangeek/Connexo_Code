/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.masterdata.MeasurementType;
import com.energyict.mdc.device.config.impl.MessageSeeds;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to add a {@link MeasurementType} to a {@link DeviceType}
 * but that RegisterType was already added to the DeviceType before.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-02-10 (16:54)
 */
public class RegisterTypeAlreadyInDeviceTypeException extends LocalizedException {

    public RegisterTypeAlreadyInDeviceTypeException(DeviceType deviceType, MeasurementType measurementType, Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.DUPLICATE_REGISTER_TYPE_IN_DEVICE_TYPE, measurementType.getReadingType().getAliasName(), deviceType.getName());
        this.set("deviceType", deviceType);
        this.set("measurementType", measurementType);
    }

}