package com.energyict.mdc.device.data.impl.identifiers;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.exceptions.CanNotFindForIdentifier;
import com.energyict.mdc.device.data.impl.MessageSeeds;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.LoadProfileIdentifierType;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Arrays;
import java.util.List;

/**
 * Provides an implementation for the {@link LoadProfileIdentifier}
 * that returns the first LoadProfile that is found
 * on a Device.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-07-02 (11:52)
 */
@XmlRootElement
public class LoadProfileIdentifierFirstOnDevice implements LoadProfileIdentifier<LoadProfile> {

    private DeviceIdentifier<Device> deviceIdentifier;

    /**
     * Constructor only to be used by JSON (de)marshalling
     */
    public LoadProfileIdentifierFirstOnDevice() {
    }

    public LoadProfileIdentifierFirstOnDevice(DeviceIdentifier<Device> deviceIdentifier) {
        super();
        this.deviceIdentifier = deviceIdentifier;
    }

    @Override
    public LoadProfile findLoadProfile() {
        Device device = this.deviceIdentifier.findDevice();
        List<LoadProfile> loadProfiles = device.getLoadProfiles();
        if (loadProfiles.isEmpty()) {
            throw CanNotFindForIdentifier.loadProfile(this, MessageSeeds.CAN_NOT_FIND_FOR_LOADPROFILE_IDENTIFIER);
        } else {
            return loadProfiles.get(0);
        }
    }

    @XmlAttribute
    public DeviceIdentifier getDeviceIdentifier() {
        return deviceIdentifier;
    }

    @Override
    public LoadProfileIdentifierType getLoadProfileIdentifierType() {
        return LoadProfileIdentifierType.FistLoadProfileOnDevice;
    }

    @Override
    public List<Object> getIdentifier() {
        return Arrays.asList((Object) getDeviceIdentifier());
    }

    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    public void setXmlType(String ignore) {
        // For xml unmarshalling purposes only
    }

    @Override
    public String toString() {
        return "fist load profile on device having deviceIdentifier = " + deviceIdentifier;
    }

}