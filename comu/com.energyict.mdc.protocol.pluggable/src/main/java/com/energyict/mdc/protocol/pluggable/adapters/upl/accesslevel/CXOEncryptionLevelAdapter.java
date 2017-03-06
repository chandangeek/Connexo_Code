package com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel;

import com.energyict.mdc.protocol.api.security.DeviceAccessLevel;
import com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 3/01/2017 - 16:05
 */
public class CXOEncryptionLevelAdapter extends CXODeviceAccessLevelAdapter implements EncryptionDeviceAccessLevel {

    public CXOEncryptionLevelAdapter(DeviceAccessLevel uplDeviceAccessLevel) {
        super(uplDeviceAccessLevel);
    }
}