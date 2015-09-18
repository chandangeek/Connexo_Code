package com.energyict.mdc.device.data.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.MessageSeeds;

/**
 * Models an exception which occurs when one tries to change the configuration of a device to the configuration it already has
 */
public class CannotChangeDeviceConfigToSameConfig extends LocalizedException {

    public CannotChangeDeviceConfigToSameConfig(Thesaurus thesaurus, Device device) {
        super(thesaurus, MessageSeeds.CANNOT_CHANGE_DEVICE_CONFIG_TO_SAME_CONFIG, device);
    }
}
