package com.energyict.mdc.device.config.exceptions;

import com.energyict.mdc.device.config.DeviceConfiguration;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to delete an active DeviceConfiguration
 *
 * Copyrights EnergyICT
 * Date: 03/02/14
 * Time: 13:29
 */
public class DeviceConfigurationIsActiveException extends LocalizedException {

    public DeviceConfigurationIsActiveException(DeviceConfiguration deviceConfiguration, Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed);
        set("deviceConfiguration", deviceConfiguration);
    }
}
