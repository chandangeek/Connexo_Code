package com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.protocol.api.security.RequestSecurityLevel;
import com.energyict.mdc.upl.security.DeviceAccessLevel;

/**
 * @author khe
 * @since 3/01/2017 - 16:05
 */
public class UPLRequestSecurityLevelAdapter extends UPLDeviceAccessLevelAdapter implements RequestSecurityLevel {

    public static RequestSecurityLevel adaptTo(DeviceAccessLevel uplDeviceAccessLevel, Thesaurus thesaurus) {
        if (uplDeviceAccessLevel instanceof CXORequestSecurityLevelAdapter) {
            return (RequestSecurityLevel) ((CXORequestSecurityLevelAdapter) uplDeviceAccessLevel).getConnexoDeviceAccessLevel();
        } else {
            return new UPLRequestSecurityLevelAdapter(uplDeviceAccessLevel, thesaurus);
        }
    }

    private UPLRequestSecurityLevelAdapter(DeviceAccessLevel uplDeviceAccessLevel, Thesaurus thesaurus) {
        super(uplDeviceAccessLevel, thesaurus);
    }
}