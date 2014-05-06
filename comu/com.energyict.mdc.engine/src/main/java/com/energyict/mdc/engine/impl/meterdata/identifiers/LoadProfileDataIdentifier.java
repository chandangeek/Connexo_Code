package com.energyict.mdc.engine.impl.meterdata.identifiers;

import com.energyict.mdc.common.NotFoundException;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.protocol.api.device.BaseLoadProfile;
import com.energyict.mdc.protocol.api.device.data.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;

import java.util.List;

/**
 * Implementation of a {@link LoadProfileIdentifier} that uniquely identifies a {@link com.energyict.mdc.protocol.api.device.BaseLoadProfile}
 * based on the ObisCode of the LoadProfile(type) and the {@link DeviceIdentifier}.<br/>
 * <b>Note: </b> we assume that it is never possible that two LoadProfiles with the same ObisCode are configured on the Device.<br/>
 * <b>Note2: </b> if the B-field of the ObisCode is marked as a wildcard, then make sure the provided loadProfileObisCode also has the wildcard!
 * <p/>
 * Copyrights EnergyICT
 * Date: 15/10/12
 * Time: 14:07
 */
public class LoadProfileDataIdentifier implements LoadProfileIdentifier {

    private final ObisCode loadProfileObisCode;
    private final DeviceIdentifier deviceIdentifier;

    private BaseLoadProfile loadProfile;

    public LoadProfileDataIdentifier(ObisCode loadProfileObisCode, DeviceIdentifier deviceIdentifier) {
        super();
        this.loadProfileObisCode = loadProfileObisCode;
        this.deviceIdentifier = deviceIdentifier;
    }

    @Override
    public BaseLoadProfile findLoadProfile () {
        if (loadProfile == null) {
            Device device = (Device) deviceIdentifier.findDevice();
            final List<LoadProfile> loadProfiles = device.getLoadProfiles();
            for (BaseLoadProfile profile : loadProfiles) {
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