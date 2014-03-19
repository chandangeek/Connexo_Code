package com.energyict.protocolimplv2.eict.eiweb;

import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.BaseLoadProfile;
import com.energyict.mdc.protocol.api.device.data.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;

import java.util.List;

/**
 * Provides an implementation for the {@link LoadProfileIdentifier}
 * that returns the first {@link com.energyict.mdc.protocol.api.device.BaseLoadProfile} that is found
 * on a {@link com.energyict.mdc.protocol.api.device.BaseDevice}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-07-02 (11:52)
 */
public class FirstLoadProfileOnDevice implements LoadProfileIdentifier {

    private DeviceIdentifier deviceIdentifier;

    public FirstLoadProfileOnDevice (DeviceIdentifier deviceIdentifier) {
        super();
        this.deviceIdentifier = deviceIdentifier;
    }

    @Override
    public BaseLoadProfile findLoadProfile () {
        BaseDevice device = this.deviceIdentifier.findDevice();
        List<BaseLoadProfile> loadProfiles = device.getLoadProfiles();
        if (loadProfiles.isEmpty()) {
            return null;
        }
        else {
            return loadProfiles.get(0);
        }
    }

}