package com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.upl.security.DeviceAccessLevel;

/**
 *
 *
 * @author khe
 * @since 3/01/2017 - 16:05
 */
public class UPLEncryptionLevelAdapter extends UPLDeviceAccessLevelAdapter implements EncryptionDeviceAccessLevel {

    public static EncryptionDeviceAccessLevel adaptTo(DeviceAccessLevel uplDeviceAccessLevel, Thesaurus thesaurus) {
        if (uplDeviceAccessLevel instanceof CXOEncryptionLevelAdapter) {
            return (EncryptionDeviceAccessLevel) ((CXOEncryptionLevelAdapter) uplDeviceAccessLevel).getConnexoDeviceAccessLevel();
        } else {
            return new UPLEncryptionLevelAdapter(uplDeviceAccessLevel, thesaurus);
        }
    }

    private UPLEncryptionLevelAdapter(DeviceAccessLevel uplDeviceAccessLevel, Thesaurus thesaurus) {
        super(uplDeviceAccessLevel, thesaurus);
    }
}