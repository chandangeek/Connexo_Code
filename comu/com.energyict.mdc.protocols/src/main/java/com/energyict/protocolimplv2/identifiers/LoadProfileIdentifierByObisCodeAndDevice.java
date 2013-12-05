package com.energyict.protocolimplv2.identifiers;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.meterdata.identifiers.CanFindDevice;
import com.energyict.mdc.meterdata.identifiers.CanFindLoadProfile;
import com.energyict.mdc.protocol.inbound.DeviceIdentifier;
import com.energyict.mdw.core.LoadProfile;
import com.energyict.mdw.core.MeteringWarehouse;

import java.util.List;

/**
 * Implementation of a {@link com.energyict.mdc.protocol.device.data.identifiers.LoadProfileIdentifier} that uniquely identifies a {@link LoadProfile}
 * based on the ObisCode of the LoadProfile(type) and the {@link DeviceIdentifier}.<br/>
 * <b>Note: </b> we assume that it is never possible that two LoadProfiles with the same ObisCode are configured on the Device.<br/>
 * <b>Note2: </b> if the B-field of the ObisCode is marked as a wildcard, then make sure the provided loadProfileObisCode also has the wildcard!
 * <p/>
 * Copyrights EnergyICT
 * Date: 13/05/13
 * Time: 13:30
 */
public class LoadProfileIdentifierByObisCodeAndDevice implements CanFindLoadProfile {

    private final ObisCode loadProfileObisCode;
    private final CanFindDevice deviceIdentifier;

    private LoadProfile loadProfile;

    public LoadProfileIdentifierByObisCodeAndDevice(ObisCode loadProfileObisCode, DeviceIdentifier deviceIdentifier) {
        super();
        this.loadProfileObisCode = loadProfileObisCode;
        this.deviceIdentifier = (CanFindDevice) deviceIdentifier;
    }

    @Override
    public LoadProfile findLoadProfile() {
        if(loadProfile == null){
            final List<LoadProfile> loadProfiles = MeteringWarehouse.getCurrent().getLoadProfileFactory().findByRtu(deviceIdentifier.findDevice());
            for (LoadProfile profile : loadProfiles) {
                if (profile.getLoadProfileSpec().getDeviceObisCode().equals(this.loadProfileObisCode)) {
                    this.loadProfile = profile;
                    break;
                }
            }
        }
        return loadProfile;
    }

    @Override
    public String toString() {
        return "deviceIdentifier = " + deviceIdentifier + " and ObisCode = " + loadProfileObisCode;
    }
}
