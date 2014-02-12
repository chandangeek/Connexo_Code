package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.config.DeviceConfiguration;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to delete an active DeviceConfiguration
 *
 * Copyrights EnergyICT
 * Date: 03/02/14
 * Time: 13:29
 */
public class DeviceConfigurationIsActiveException extends LocalizedException {

    public DeviceConfigurationIsActiveException(Thesaurus thesaurus, DeviceConfiguration deviceConfiguration) {
        super(thesaurus, MessageSeeds.DEVICE_CONFIGURATION_IS_ACTIVE_CAN_NOT_DELETE);
        set("deviceConfiguration", deviceConfiguration);
    }
}
