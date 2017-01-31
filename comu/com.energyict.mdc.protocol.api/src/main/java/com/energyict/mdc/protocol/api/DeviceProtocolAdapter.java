/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.protocol.api.legacy.CachingProtocol;
import com.energyict.mdc.protocol.api.legacy.DeviceCachingSupport;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.tasks.support.UsesLegacyMessageConverter;

import java.util.logging.Logger;

public interface DeviceProtocolAdapter extends HHUEnabler, CachingProtocol, DeviceCachingSupport, DeviceSecuritySupport, UsesLegacyMessageConverter {

    /**
     * Initialize the logger which will be used by the legacy protocols
     *
     * @param logger the given logger
     */
    public void initializeLogger(final Logger logger);

    public DeviceProtocolSecurityPropertySet getLegacyTypedPropertiesAsSecurityPropertySet(TypedProperties typedProperties);

}