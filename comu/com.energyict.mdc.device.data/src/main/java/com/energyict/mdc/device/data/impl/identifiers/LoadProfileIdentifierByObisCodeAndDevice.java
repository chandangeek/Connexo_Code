package com.energyict.mdc.device.data.impl.identifiers;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.exceptions.CanNotFindForIdentifier;
import com.energyict.mdc.device.data.impl.MessageSeeds;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifierType;
import com.energyict.obis.ObisCode;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

/**
 * Implementation of a {@link com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifier} that uniquely identifies a LoadProfile
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

    private LoadProfile loadProfile;

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
    public LoadProfile getLoadProfile() {
        if (loadProfile == null) {
            Device device = (Device) deviceIdentifier.findDevice(); //Downcast to the Connexo Device
            this.loadProfile = device.getLoadProfiles()
                                    .stream()
                                    .filter(loadProfile -> loadProfile.getDeviceObisCode().equals(loadProfileObisCode))
                                    .findFirst()
                                    .orElseThrow(() -> CanNotFindForIdentifier.loadProfile(this, MessageSeeds.CAN_NOT_FIND_FOR_LOADPROFILE_IDENTIFIER));
        }
        return loadProfile;
    }

    @Override
    public ObisCode getProfileObisCode() {
        return loadProfileObisCode;
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
    public List<Object> getParts() {
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
        return MessageFormat.format("load profile having OBIS code {0} on device with deviceIdentifier ''{1}''", loadProfileObisCode, deviceIdentifier);
    }

}