package com.energyict.mdc.device.data.impl.events;

import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.data.impl.MessageSeeds;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

/**
 * Models the exceptional situation that occurs when a {@link ProtocolDialectConfigurationProperties}
 * is being deleted while it is still being used by one or more {@link com.energyict.mdc.device.data.Device}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-02-18 (16:11)
 */
public class VetoDeleteProtocolDialectConfigurationPropertiesException extends LocalizedException {

    public VetoDeleteProtocolDialectConfigurationPropertiesException(Thesaurus thesaurus, ProtocolDialectConfigurationProperties configurationProperties) {
        super(thesaurus, MessageSeeds.VETO_PROTOCOL_DIALECT_CONFIGURATION_DELETION, configurationProperties.getDeviceProtocolDialectName());
        this.set("dialectName", configurationProperties.getDeviceProtocolDialectName());
    }

}