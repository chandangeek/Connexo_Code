package com.energyict.protocolimplv2.eict.eiweb;

import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifierType;

import com.energyict.mdw.core.Device;
import com.energyict.mdw.core.LoadProfile;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.exceptions.identifier.NotFoundException;
import com.energyict.util.Collections;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Provides an implementation for the {@link LoadProfileIdentifier}
 * that returns the first {@link LoadProfile} that is found
 * on a {@link Device}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-07-02 (11:52)
 */
@XmlRootElement
public class FirstLoadProfileOnDevice implements LoadProfileIdentifier {

    private final ObisCode profileObisCode;
    private final DeviceIdentifier deviceIdentifier;

    /**
     * Constructor only to be used by JSON (de)marshalling
     */
    private FirstLoadProfileOnDevice() {
        this.profileObisCode = null;
        this.deviceIdentifier = null;
    }

    public FirstLoadProfileOnDevice(DeviceIdentifier deviceIdentifier, ObisCode profileObisCode) {
        super();
        this.deviceIdentifier = deviceIdentifier;
        this.profileObisCode = profileObisCode;
    }

    @Override
    @XmlAttribute
    public ObisCode getProfileObisCode() {
        return profileObisCode;
    }

    @Override
    public LoadProfile getLoadProfile() {
        Device device = this.deviceIdentifier.findDevice();
        List<LoadProfile> loadProfiles = device.getLoadProfiles();
        if (loadProfiles.isEmpty()) {
            throw NotFoundException.notFound(LoadProfile.class, this.toString());
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
    public List<Object> getParts() {
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
        return "first load profile on device having deviceIdentifier = " + deviceIdentifier;
    }

}