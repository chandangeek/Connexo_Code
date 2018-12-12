package com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel;

import com.energyict.mdc.protocol.api.security.DeviceAccessLevel;
import com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel;

/**
 * @author khe
 * @since 3/01/2017 - 16:05
 */
public class CXOAuthenticationLevelAdapter extends CXODeviceAccessLevelAdapter implements AuthenticationDeviceAccessLevel {

    public static AuthenticationDeviceAccessLevel adaptTo(DeviceAccessLevel connexoDeviceAccessLevel) {
        if (connexoDeviceAccessLevel instanceof UPLAuthenticationLevelAdapter) {
            return (AuthenticationDeviceAccessLevel) ((UPLAuthenticationLevelAdapter) connexoDeviceAccessLevel).getUplDeviceAccessLevel();
        } else {
            return new CXOAuthenticationLevelAdapter(connexoDeviceAccessLevel);
        }
    }

    private CXOAuthenticationLevelAdapter(DeviceAccessLevel uplDeviceAccessLevel) {
        super(uplDeviceAccessLevel);
    }
}