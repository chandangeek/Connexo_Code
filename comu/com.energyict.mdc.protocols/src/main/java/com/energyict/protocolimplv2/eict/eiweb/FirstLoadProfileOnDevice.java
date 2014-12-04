package com.energyict.protocolimplv2.eict.eiweb;

import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.BaseLoadProfile;
import com.energyict.mdc.protocol.api.device.data.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;

import java.util.List;


import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Provides an implementation for the {@link LoadProfileIdentifier}
 * that returns the first {@link com.energyict.mdc.protocol.api.device.BaseLoadProfile} that is found
 * on a {@link com.energyict.mdc.protocol.api.device.BaseDevice}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-07-02 (11:52)
 */
@XmlRootElement
public class FirstLoadProfileOnDevice implements LoadProfileIdentifier {

    private DeviceIdentifier deviceIdentifier;

    /**
     * Constructor only to be used by JSON (de)marshalling
     */
    public FirstLoadProfileOnDevice() {
    }

    public FirstLoadProfileOnDevice (DeviceIdentifier deviceIdentifier) {
        super();
        this.deviceIdentifier = deviceIdentifier;
    }

    @Override
    public LoadProfile getLoadProfile() {
        Device device = this.deviceIdentifier.findDevice();
        List<LoadProfile> loadProfiles = device.getLoadProfiles();
        if (loadProfiles.isEmpty()) {
            throw new NotFoundException("Device with " + deviceIdentifier.toString() + " has no load profiles");
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
        return Collections.toList((Object) getDeviceIdentifier());
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