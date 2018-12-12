/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;

public class ProtocolDialectConfigurationPropertiesCannotDropRequiredProperty extends LocalizedException {

    protected ProtocolDialectConfigurationPropertiesCannotDropRequiredProperty(Thesaurus thesaurus, ProtocolDialectConfigurationProperties properties, String propertyName) {
        super(thesaurus, MessageSeeds.PROTOCOLDIALECT_CONF_PROPS_CANT_DROP_REQUIRED, properties.getName(), propertyName);
    }
}
