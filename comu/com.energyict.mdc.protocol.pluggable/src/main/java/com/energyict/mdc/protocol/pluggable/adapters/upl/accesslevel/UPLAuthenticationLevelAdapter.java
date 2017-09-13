package com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel;

import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.upl.security.DeviceAccessLevel;

/**
 * @author khe
 * @since 3/01/2017 - 16:05
 */
public class UPLAuthenticationLevelAdapter extends UPLDeviceAccessLevelAdapter implements AuthenticationDeviceAccessLevel {

    public static AuthenticationDeviceAccessLevel adaptTo(DeviceAccessLevel uplDeviceAccessLevel) {
        if (uplDeviceAccessLevel instanceof CXOAuthenticationLevelAdapter) {
            return (AuthenticationDeviceAccessLevel) ((CXOAuthenticationLevelAdapter) uplDeviceAccessLevel).getConnexoDeviceAccessLevel();
        } else {
            return new UPLAuthenticationLevelAdapter(uplDeviceAccessLevel);
        }
    }

    public UPLAuthenticationLevelAdapter(DeviceAccessLevel uplDeviceAccessLevel) {
        super(uplDeviceAccessLevel);
    }

}