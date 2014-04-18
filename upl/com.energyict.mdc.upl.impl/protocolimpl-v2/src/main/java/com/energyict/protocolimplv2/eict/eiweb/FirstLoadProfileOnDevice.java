package com.energyict.protocolimplv2.eict.eiweb;

import com.energyict.cbo.NotFoundException;
import com.energyict.mdc.meterdata.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.protocol.inbound.DeviceIdentifier;
import com.energyict.mdw.core.Device;
import com.energyict.mdw.core.LoadProfile;

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
    public LoadProfile getLoadProfile () {
        Device device = this.deviceIdentifier.findDevice();
        List<LoadProfile> loadProfiles = device.getLoadProfiles();
        if (loadProfiles.isEmpty()) {
            throw new NotFoundException("Device with " + deviceIdentifier.toString() + " has no load profiles");
        }
        else {
            return loadProfiles.get(0);
        }
    }

    @XmlAttribute
    public DeviceIdentifier getDeviceIdentifier() {
        return deviceIdentifier;
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