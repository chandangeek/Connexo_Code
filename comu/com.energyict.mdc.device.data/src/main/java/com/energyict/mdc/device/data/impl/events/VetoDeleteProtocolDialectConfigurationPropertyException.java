package com.energyict.mdc.device.data.impl.events;

import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperty;
import com.energyict.mdc.device.data.impl.MessageSeeds;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

/**
 * Models the exceptional situation that occurs when a {@link ProtocolDialectConfigurationProperty}
 * is being removed from a {@link ProtocolDialectConfigurationProperties}
 * while it is still being used by one or more {@link com.energyict.mdc.device.data.Device}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-06-13 (16:11)
 */
public class VetoDeleteProtocolDialectConfigurationPropertyException extends LocalizedException {

    public VetoDeleteProtocolDialectConfigurationPropertyException(Thesaurus thesaurus, ProtocolDialectConfigurationProperty property) {
        super(thesaurus, MessageSeeds.VETO_PROTOCOL_DIALECT_CONFIGURATION_VALUE_DELETION, property.getName(), property.getProtocolDialectConfigurationProperties().getName());
        this.set("dialectName", property.getProtocolDialectConfigurationProperties().getDeviceProtocolDialectName());
        this.set("propertyName", property.getName());
    }

}