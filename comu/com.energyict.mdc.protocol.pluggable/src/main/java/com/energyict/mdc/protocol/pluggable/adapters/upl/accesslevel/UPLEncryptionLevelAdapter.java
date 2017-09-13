package com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel;

import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.upl.security.DeviceAccessLevel;

/**
 *
 *
 * @author khe
 * @since 3/01/2017 - 16:05
 */
public class UPLEncryptionLevelAdapter extends UPLDeviceAccessLevelAdapter implements EncryptionDeviceAccessLevel {

    public static EncryptionDeviceAccessLevel adaptTo(DeviceAccessLevel uplDeviceAccessLevel) {
        if (uplDeviceAccessLevel instanceof CXOEncryptionLevelAdapter) {
            return (EncryptionDeviceAccessLevel) ((CXOEncryptionLevelAdapter) uplDeviceAccessLevel).getConnexoDeviceAccessLevel();
        } else {
            return new UPLEncryptionLevelAdapter(uplDeviceAccessLevel);
        }
    }

    private UPLEncryptionLevelAdapter(DeviceAccessLevel uplDeviceAccessLevel) {
        super(uplDeviceAccessLevel);
    }
}