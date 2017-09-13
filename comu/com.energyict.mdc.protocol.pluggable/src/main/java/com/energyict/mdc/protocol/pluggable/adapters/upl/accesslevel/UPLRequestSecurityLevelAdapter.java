package com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel;

import com.energyict.mdc.protocol.api.security.RequestSecurityLevel;
import com.energyict.mdc.upl.security.DeviceAccessLevel;

/**
 * @author khe
 * @since 3/01/2017 - 16:05
 */
public class UPLRequestSecurityLevelAdapter extends UPLDeviceAccessLevelAdapter implements RequestSecurityLevel {

    public static RequestSecurityLevel adaptTo(DeviceAccessLevel uplDeviceAccessLevel) {
        if (uplDeviceAccessLevel instanceof CXORequestSecurityLevelAdapter) {
            return (RequestSecurityLevel) ((CXORequestSecurityLevelAdapter) uplDeviceAccessLevel).getConnexoDeviceAccessLevel();
        } else {
            return new UPLRequestSecurityLevelAdapter(uplDeviceAccessLevel);
        }
    }

    private UPLRequestSecurityLevelAdapter(DeviceAccessLevel uplDeviceAccessLevel) {
        super(uplDeviceAccessLevel);
    }
}