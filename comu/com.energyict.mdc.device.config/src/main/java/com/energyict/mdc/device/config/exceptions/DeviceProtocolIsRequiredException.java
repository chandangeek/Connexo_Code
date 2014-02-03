package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to create a {@link com.energyict.mdc.device.config.DeviceType}
 * without a {@link com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-31 (15:47)
 */
public class DeviceProtocolIsRequiredException extends LocalizedException {

    public DeviceProtocolIsRequiredException(Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.DEVICE_PROTOCOL_IS_REQUIRED);
    }

}