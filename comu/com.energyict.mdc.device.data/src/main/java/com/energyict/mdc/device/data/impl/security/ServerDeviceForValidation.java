/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.security;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.Device;

import java.util.Map;

public interface ServerDeviceForValidation extends Device {

    /**
     * Return a list per security property set that contains dirty properties that need to be validated
     */
    Map<SecurityPropertySet, TypedProperties> getDirtySecurityProperties();

}
