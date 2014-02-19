package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.RegisterMapping;

/**
 * Copyrights EnergyICT
 * Date: 18/02/14
 * Time: 15:13
 */
public class RegisterMappingIsNotConfiguredOnDeviceTypeException extends LocalizedException {

    public RegisterMappingIsNotConfiguredOnDeviceTypeException(Thesaurus thesaurus, RegisterMapping registerMapping) {
        super(thesaurus, MessageSeeds.REGISTER_SPEC_REGISTER_MAPPING_IS_NOT_ON_DEVICE_TYPE, registerMapping.getName());
        set("registerMapping", registerMapping.getName());    }
}
