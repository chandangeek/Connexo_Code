package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to create a new entity within this bundle without
 * specifying an {@link com.energyict.mdc.device.config.DeviceType}.
 * <p/>
 * Copyrights EnergyICT
 * Date: 07/02/14
 * Time: 16:31
 */
public class DeviceTypeIsRequiredException extends LocalizedException {

    public DeviceTypeIsRequiredException(Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.DEVICE_CONFIGURATION_DEVICE_TYPE_IS_REQUIRED);
    }
}
