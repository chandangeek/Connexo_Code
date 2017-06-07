/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel;

import com.energyict.mdc.protocol.api.security.DeviceAccessLevel;
import com.energyict.mdc.upl.security.RequestSecurityLevel;

/**
 *
 *
 * @author khe
 * @since 3/01/2017 - 16:05
 */
public class CXORequestSecurityLevelAdapter extends CXODeviceAccessLevelAdapter implements RequestSecurityLevel {

    public CXORequestSecurityLevelAdapter(DeviceAccessLevel uplDeviceAccessLevel) {
        super(uplDeviceAccessLevel);
    }
}