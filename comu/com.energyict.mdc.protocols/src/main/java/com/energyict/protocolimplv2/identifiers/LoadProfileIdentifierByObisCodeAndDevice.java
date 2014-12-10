package com.energyict.protocolimplv2.identifiers;

import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.BaseLoadProfile;
import com.energyict.mdc.protocol.api.device.LoadProfileFactory;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.protocol.api.inbound.LoadProfileIdentifierType;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Implementation of a {@link com.energyict.mdc.protocol.api.device.data.identifiers.LoadProfileIdentifier} that uniquely identifies a {@link com.energyict.mdc.protocol.api.device.BaseLoadProfile}
 * based on the ObisCode of the LoadProfile(type) and the {@link DeviceIdentifier}.<br/>
 * <b>Note: </b> we assume that it is never possible that two LoadProfiles with the same ObisCode are configured on the Device.<br/>
 * <b>Note2: </b> if the B-field of the ObisCode is marked as a wildcard, then make sure the provided loadProfileObisCode also has the wildcard!
 * <p/>
 * Copyrights EnergyICT
 * Date: 13/05/13
 * Time: 13:30
 */
@XmlRootElement
public class LoadProfileIdentifierByObisCodeAndDevice implements LoadProfileIdentifier {

    private final ObisCode loadProfileObisCode;
    private final DeviceIdentifier deviceIdentifier;

    private BaseLoadProfile loadProfile;

    /**
     * Constructor only to be used by JSON (de)marshalling
     */
    public LoadProfileIdentifierByObisCodeAndDevice() {
        this.loadProfileObisCode = null;
        this.deviceIdentifier = null;
    }

    public LoadProfileIdentifierByObisCodeAndDevice(ObisCode loadProfileObisCode, DeviceIdentifier deviceIdentifier) {
        super();
        this.loadProfileObisCode = loadProfileObisCode;
        this.deviceIdentifier = deviceIdentifier;
    }

    @Override
    public BaseLoadProfile findLoadProfile() {
        if (loadProfile == null) {
            BaseDevice device = deviceIdentifier.findDevice();
            List<LoadProfileFactory> loadProfileFactories = Environment.DEFAULT.get().getApplicationContext().getModulesImplementing(LoadProfileFactory.class);
            List<BaseLoadProfile> loadProfiles = new ArrayList<>();
            for (LoadProfileFactory factory : loadProfileFactories) {
                loadProfiles.addAll(factory.findLoadProfilesByDevice(device));
            }
            for (BaseLoadProfile profile : loadProfiles) {
                if (profile.getDeviceObisCode().equals(this.loadProfileObisCode)) {
                    this.loadProfile = profile;
                    break;
                }
            }
        }
        return loadProfile;
    }

    @XmlAttribute
    public ObisCode getLoadProfileObisCode() {
        return loadProfileObisCode;
    }

    @XmlAttribute
    public DeviceIdentifier getDeviceIdentifier() {
        return deviceIdentifier;
    }

    @Override
    public LoadProfileIdentifierType getLoadProfileIdentifierType() {
        return LoadProfileIdentifierType.DeviceIdentifierAndObisCode;
    }

    @Override
    public List<Object> getIdentifier() {
        return Arrays.asList((Object) getDeviceIdentifier(), getLoadProfileObisCode());
    }

    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    @Override
    public void setXmlType(String ignore) {
        // For xml unmarshalling purposes only
    }

    @Override
    public String toString() {
        return "deviceIdentifier = " + deviceIdentifier + " and ObisCode = " + loadProfileObisCode;
    }

}