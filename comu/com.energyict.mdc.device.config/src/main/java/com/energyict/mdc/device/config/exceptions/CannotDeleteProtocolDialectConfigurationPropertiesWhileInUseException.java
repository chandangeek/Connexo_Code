package com.energyict.mdc.device.config.exceptions;

import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Models the exceptional situation that occurs when an attempt
 * is made to delete a {@link ProtocolDialectConfigurationProperties}
 * that is still in use by {@link com.energyict.mdc.device.config.ComTaskEnablement}s
 * of the same configuration.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-23 (16:23)
 */
public class CannotDeleteProtocolDialectConfigurationPropertiesWhileInUseException extends LocalizedException {

    public CannotDeleteProtocolDialectConfigurationPropertiesWhileInUseException(ProtocolDialectConfigurationProperties properties, Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed, properties.getName(), properties.getDeviceConfiguration().getName());
        this.set("dialectName", properties.getName());
        this.set("deviceConfiguration", properties.getDeviceConfiguration());
    }

}