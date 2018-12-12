package com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel;

import com.energyict.mdc.protocol.api.security.DeviceAccessLevel;
import com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel;

/**
 *
 *
 * @author khe
 * @since 3/01/2017 - 16:05
 */
public class CXOEncryptionLevelAdapter extends CXODeviceAccessLevelAdapter implements EncryptionDeviceAccessLevel {

    public static EncryptionDeviceAccessLevel adaptTo(DeviceAccessLevel connexoDeviceAccessLevel) {
        if (connexoDeviceAccessLevel instanceof UPLEncryptionLevelAdapter) {
            return (EncryptionDeviceAccessLevel) ((UPLEncryptionLevelAdapter) connexoDeviceAccessLevel).getUplDeviceAccessLevel();
        } else {
            return new CXOEncryptionLevelAdapter(connexoDeviceAccessLevel);
        }
    }

    private CXOEncryptionLevelAdapter(DeviceAccessLevel uplDeviceAccessLevel) {
        super(uplDeviceAccessLevel);
    }
}