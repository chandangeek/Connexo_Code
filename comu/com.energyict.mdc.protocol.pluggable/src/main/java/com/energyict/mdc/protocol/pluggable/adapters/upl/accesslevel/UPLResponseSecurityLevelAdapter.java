package com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.protocol.api.security.ResponseSecurityLevel;
import com.energyict.mdc.upl.security.DeviceAccessLevel;

/**
 * @author khe
 * @since 3/01/2017 - 16:05
 */
public class UPLResponseSecurityLevelAdapter extends UPLDeviceAccessLevelAdapter implements ResponseSecurityLevel {

    public static ResponseSecurityLevel adaptTo(DeviceAccessLevel uplDeviceAccessLevel, Thesaurus thesaurus) {
        if (uplDeviceAccessLevel instanceof CXOResponseSecurityLevelAdapter) {
            return (ResponseSecurityLevel) ((CXOResponseSecurityLevelAdapter) uplDeviceAccessLevel).getConnexoDeviceAccessLevel();
        } else {
            return new UPLResponseSecurityLevelAdapter(uplDeviceAccessLevel, thesaurus);
        }
    }

    private UPLResponseSecurityLevelAdapter(DeviceAccessLevel uplDeviceAccessLevel, Thesaurus thesaurus) {
        super(uplDeviceAccessLevel, thesaurus);
    }
}