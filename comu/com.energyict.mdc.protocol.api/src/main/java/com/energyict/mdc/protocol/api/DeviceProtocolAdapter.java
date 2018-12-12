/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api;

import com.energyict.mdc.protocol.api.tasks.support.UsesLegacyMessageConverter;
import com.energyict.mdc.upl.DeviceCachingSupport;
import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.upl.cache.CachingProtocol;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.upl.tasks.support.DeviceProtocolConnectionFunctionSupport;

import java.util.logging.Logger;

public interface DeviceProtocolAdapter extends com.energyict.protocol.HHUEnabler, CachingProtocol, DeviceCachingSupport, DeviceSecuritySupport, DeviceProtocolConnectionFunctionSupport, UsesLegacyMessageConverter {

    /**
     * Initialize the logger which will be used by the legacy protocols
     *
     * @param logger the given logger
     */
    void initializeLogger(final Logger logger);

    DeviceProtocolSecurityPropertySet getLegacyTypedPropertiesAsSecurityPropertySet(TypedProperties typedProperties);

}