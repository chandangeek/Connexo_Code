package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to change the {@link DeviceProtocolPluggableClass}
 * of a {@link DeviceType} when that type has active
 * {@link com.energyict.mdc.device.config.DeviceConfiguration}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-02-12 (11:20)
 */
public class CannotChangeDeviceProtocolWithActiveConfigurationsException extends LocalizedException{

    public CannotChangeDeviceProtocolWithActiveConfigurationsException(Thesaurus thesaurus, DeviceType deviceType) {
        super(thesaurus, MessageSeeds.DEVICE_PROTOCOL_CANNOT_CHANGE_WITH_EXISTING_CONFIGURATIONS, deviceType);
        this.set("deviceType", deviceType);
    }

}