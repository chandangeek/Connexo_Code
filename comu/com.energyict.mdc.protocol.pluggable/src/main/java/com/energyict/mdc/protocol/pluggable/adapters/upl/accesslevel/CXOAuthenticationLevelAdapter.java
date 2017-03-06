package com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel;

import com.energyict.mdc.protocol.api.security.DeviceAccessLevel;
import com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 3/01/2017 - 16:05
 */
public class CXOAuthenticationLevelAdapter extends CXODeviceAccessLevelAdapter implements AuthenticationDeviceAccessLevel {

    public CXOAuthenticationLevelAdapter(DeviceAccessLevel uplDeviceAccessLevel) {
        super(uplDeviceAccessLevel);
    }
}