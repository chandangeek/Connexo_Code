package com.energyict.mdc.protocol.pluggable.impl.adapters.common.identifiers;

import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.NotFoundException;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.LoadProfile;
import com.energyict.mdc.protocol.api.device.LoadProfileFactory;
import com.energyict.mdc.protocol.api.device.data.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-15 (15:58)
 */
public class LoadProfileDataIdentifier implements LoadProfileIdentifier {

    private final ObisCode loadProfileObisCode;
    private final DeviceIdentifier deviceIdentifier;

    private LoadProfile loadProfile;

    public LoadProfileDataIdentifier(ObisCode loadProfileObisCode, DeviceIdentifier deviceIdentifier) {
        super();
        this.loadProfileObisCode = loadProfileObisCode;
        this.deviceIdentifier = deviceIdentifier;
    }

    @Override
    public LoadProfile findLoadProfile () {
        if (loadProfile == null) {
            BaseDevice device = deviceIdentifier.findDevice();
            List<LoadProfileFactory> loadProfileFactories = Environment.DEFAULT.get().getApplicationContext().getModulesImplementing(LoadProfileFactory.class);
            List<LoadProfile> loadProfiles = new ArrayList<>();
            for (LoadProfileFactory factory : loadProfileFactories) {
                loadProfiles.addAll(factory.findLoadProfilesByDevice(device));
            }
            for (LoadProfile profile : loadProfiles) {
                if (profile.getDeviceObisCode().equals(this.loadProfileObisCode)) {
                    this.loadProfile = profile;
                    break;
                }
            }
        }
        if (this.loadProfile == null) {
            throw new NotFoundException("LoadProfile with ObisCode " + loadProfileObisCode + " for Device with " + deviceIdentifier.toString() + " not found");
        }
        return loadProfile;
    }

    @Override
    public String toString() {
        return loadProfileObisCode.toString();
    }

}