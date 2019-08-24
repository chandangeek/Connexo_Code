/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.data.impl.MessageSeeds;

/**
 * Models the exceptional situation that occurs when a {@link DeviceConfiguration}
 * is being deactivated while it is still being used by one or more
 * {@link Device}s.
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