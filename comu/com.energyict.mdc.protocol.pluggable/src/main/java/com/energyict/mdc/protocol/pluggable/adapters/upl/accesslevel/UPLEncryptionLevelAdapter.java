package com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel;

import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.upl.security.DeviceAccessLevel;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 3/01/2017 - 16:05
 */
public class UPLEncryptionLevelAdapter extends UPLDeviceAccessLevelAdapter implements EncryptionDeviceAccessLevel {

    public UPLEncryptionLevelAdapter(DeviceAccessLevel uplDeviceAccessLevel) {
        super(uplDeviceAccessLevel);
    }
}