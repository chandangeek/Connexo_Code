package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;

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

    public CannotDeleteProtocolDialectConfigurationPropertiesWhileInUseException(Thesaurus thesaurus, ProtocolDialectConfigurationProperties properties) {
        super(thesaurus, MessageSeeds.PROTOCOLDIALECT_CONF_PROPS_IN_USE, properties.getName(), properties.getDeviceConfiguration().getName());
        this.set("dialectName", properties.getName());
        this.set("deviceConfiguration", properties.getDeviceConfiguration());
    }

}