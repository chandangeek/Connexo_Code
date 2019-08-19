/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.exceptions;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.data.ProtocolDialectProperties;
import com.energyict.mdc.common.protocol.ProtocolDialectConfigurationProperties;

/**
 * Models the exceptional situation that occurs when an attempt
 * is made to create a {@link ProtocolDialectProperties}
 * without existing or matching {@link ProtocolDialectConfigurationProperties}
 * on the {@link DeviceConfiguration}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-09 (10:55)
 */
public class ProtocolDialectConfigurationPropertiesIsRequiredException extends LocalizedFieldValidationException {

    public ProtocolDialectConfigurationPropertiesIsRequiredException(MessageSeed messageSeed) {
        super(messageSeed, "configurationProperties");
    }

}