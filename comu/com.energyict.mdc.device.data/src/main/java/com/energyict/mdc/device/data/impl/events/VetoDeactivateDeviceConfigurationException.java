package com.energyict.mdc.device.data.impl.events;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.data.impl.MessageSeeds;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

/**
 * Models the exceptional situation that occurs when a {@link DeviceConfiguration}
 * is being deactivated while it is still being used by one or more
 * {@link com.energyict.mdc.device.data.Device}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-06-13 (16:11)
 */
public class VetoDeactivateDeviceConfigurationException extends LocalizedException {

    public VetoDeactivateDeviceConfigurationException(Thesaurus thesaurus, DeviceConfiguration deviceConfiguration) {
        super(thesaurus, MessageSeeds.VETO_DEVICE_CONFIGURATION_DEACTIVATION, deviceConfiguration.getName());
        this.set("deviceConfigurationName", deviceConfiguration.getName());
    }

}