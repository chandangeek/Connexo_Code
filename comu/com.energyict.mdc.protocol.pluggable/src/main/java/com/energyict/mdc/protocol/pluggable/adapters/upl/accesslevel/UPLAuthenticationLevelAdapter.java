package com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel;

import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.upl.security.DeviceAccessLevel;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 3/01/2017 - 16:05
 */
public class UPLAuthenticationLevelAdapter extends UPLDeviceAccessLevelAdapter implements AuthenticationDeviceAccessLevel {

    public UPLAuthenticationLevelAdapter(DeviceAccessLevel uplDeviceAccessLevel) {
        super(uplDeviceAccessLevel);
    }
}