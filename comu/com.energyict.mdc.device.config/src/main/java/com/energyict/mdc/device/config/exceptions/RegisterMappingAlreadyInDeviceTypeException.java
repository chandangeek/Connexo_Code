package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.RegisterMapping;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to add a {@link RegisterMapping} to a {@link DeviceType}
 * but that RegisterMapping was already added to the DeviceType before.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-02-10 (16:54)
 */
public class RegisterMappingAlreadyInDeviceTypeException extends LocalizedException {

    public RegisterMappingAlreadyInDeviceTypeException(Thesaurus thesaurus, DeviceType deviceType, RegisterMapping registerMapping) {
        super(thesaurus, MessageSeeds.DUPLICATE_REGISTER_MAPPING_IN_DEVICE_TYPE, registerMapping.getName(), deviceType.getName());
        this.set("deviceType", deviceType);
        this.set("registerMapping", registerMapping);
    }

}