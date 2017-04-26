package com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel;

import com.energyict.mdc.protocol.api.security.RequestSecurityLevel;
import com.energyict.mdc.upl.security.DeviceAccessLevel;

/**
 * @author khe
 * @since 3/01/2017 - 16:05
 */
public class UPLRequestSecurityLevelAdapter extends UPLDeviceAccessLevelAdapter implements RequestSecurityLevel {

    public UPLRequestSecurityLevelAdapter(DeviceAccessLevel uplDeviceAccessLevel) {
        super(uplDeviceAccessLevel);
    }
}