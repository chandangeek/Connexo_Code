package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.config.exceptions.MessageSeeds;

/**
 * Copyrights EnergyICT
 * Date: 9/04/2014
 * Time: 14:57
 */
public class ProtocolDialectConfigurationPropertiesCannotDropRequiredProperty extends LocalizedException {

    protected ProtocolDialectConfigurationPropertiesCannotDropRequiredProperty(Thesaurus thesaurus, ProtocolDialectConfigurationProperties properties, String propertyName) {
        super(thesaurus, MessageSeeds.PROTOCOLDIALECT_CONF_PROPS_CANT_DROP_REQUIRED, properties.getName(), propertyName);
    }
}
