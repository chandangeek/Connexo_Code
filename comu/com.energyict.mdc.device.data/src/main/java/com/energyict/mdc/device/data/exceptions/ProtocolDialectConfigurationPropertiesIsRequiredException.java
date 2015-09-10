package com.energyict.mdc.device.data.exceptions;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Models the exceptional situation that occurs when an attempt
 * is made to create a {@link com.energyict.mdc.device.data.ProtocolDialectProperties}
 * without existing or matching {@link com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties}
 * on the {@link com.energyict.mdc.device.config.DeviceConfiguration}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-09 (10:55)
 */
public class ProtocolDialectConfigurationPropertiesIsRequiredException extends LocalizedFieldValidationException {

    public ProtocolDialectConfigurationPropertiesIsRequiredException(MessageSeed messageSeed) {
        super(messageSeed, "configurationProperties");
    }

}