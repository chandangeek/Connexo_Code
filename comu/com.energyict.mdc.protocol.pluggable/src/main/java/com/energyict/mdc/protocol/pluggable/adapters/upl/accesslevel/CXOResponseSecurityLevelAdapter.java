/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel;

import com.energyict.mdc.protocol.api.security.DeviceAccessLevel;
import com.energyict.mdc.upl.security.ResponseSecurityLevel;

/**
 *
 *
 * @author khe
 * @since 3/01/2017 - 16:05
 */
public class CXOResponseSecurityLevelAdapter extends CXODeviceAccessLevelAdapter implements ResponseSecurityLevel {

    public static ResponseSecurityLevel adaptTo(DeviceAccessLevel connexoDeviceAccessLevel) {
        if (connexoDeviceAccessLevel instanceof UPLResponseSecurityLevelAdapter) {
            return (ResponseSecurityLevel) ((UPLResponseSecurityLevelAdapter) connexoDeviceAccessLevel).getUplDeviceAccessLevel();
        } else {
            return new CXOResponseSecurityLevelAdapter(connexoDeviceAccessLevel);
        }
    }

    private CXOResponseSecurityLevelAdapter(DeviceAccessLevel connexoDeviceAccessLevel) {
        super(connexoDeviceAccessLevel);
    }
}