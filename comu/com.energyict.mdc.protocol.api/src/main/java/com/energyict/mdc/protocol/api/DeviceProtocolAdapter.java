/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.protocol.api.legacy.CachingProtocol;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.legacy.SmartMeterProtocol;
import com.energyict.mdc.protocol.api.tasks.support.UsesLegacyMessageConverter;
import com.energyict.mdc.upl.DeviceCachingSupport;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;
import com.energyict.protocol.HalfDuplexEnabler;

import java.util.logging.Logger;

public interface DeviceProtocolAdapter extends HHUEnabler, CachingProtocol, DeviceCachingSupport, DeviceSecuritySupport, UsesLegacyMessageConverter {

    /**
     * Initialize the logger which will be used by the legacy protocols
     *
     * @param logger the given logger
     */
    void initializeLogger(final Logger logger);

    DeviceProtocolSecurityPropertySet getLegacyTypedPropertiesAsSecurityPropertySet(TypedProperties typedProperties);

}