package com.energyict.protocolimplv2.eict.eiweb;

import com.energyict.mdc.meterdata.identifiers.CanFindDevice;
import com.energyict.mdc.meterdata.identifiers.CanFindLoadProfile;
import com.energyict.mdc.protocol.api.device.data.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;
import com.energyict.mdw.core.Device;
import com.energyict.mdw.core.LoadProfile;

import java.util.List;

/**
 * Provides an implementation for the {@link LoadProfileIdentifier}
 * that returns the first {@link LoadProfile} that is found
 * on a {@link Device}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-07-02 (11:52)
 */
public class FirstLoadProfileOnDevice implements CanFindLoadProfile {

    private CanFindDevice deviceIdentifier;

    public FirstLoadProfileOnDevice (DeviceIdentifier deviceIdentifier) {
        super();
        this.deviceIdentifier = (CanFindDevice) deviceIdentifier;
    }

    @Override
    public LoadProfile findLoadProfile () {
        Device device = this.deviceIdentifier.findDevice();
        List<LoadProfile> loadProfiles = device.getLoadProfiles();
        if (loadProfiles.isEmpty()) {
            return null;
        }
        else {
            return loadProfiles.get(0);
        }
    }

}