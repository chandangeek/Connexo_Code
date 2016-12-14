package com.energyict.protocolimplv2.identifiers;

import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifier;

import com.energyict.mdw.core.LoadProfile;
import com.energyict.mdw.core.LoadProfileFactory;
import com.energyict.mdw.core.LoadProfileFactoryProvider;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.exceptions.identifier.NotFoundException;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Implementation of a {@link LoadProfileIdentifier} that uniquely identifies a {@link LoadProfile}
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

    private final ObisCode profileObisCode;
    private final DeviceIdentifier deviceIdentifier;

    private LoadProfile loadProfile;

    /**
     * Constructor only to be used by JSON (de)marshalling
     */
    private LoadProfileIdentifierByObisCodeAndDevice() {
        this.profileObisCode = null;
        this.deviceIdentifier = null;
    }

    @Override
    @XmlAttribute
    public ObisCode getProfileObisCode() {
        return profileObisCode;
    }

    public LoadProfileIdentifierByObisCodeAndDevice(ObisCode profileObisCode, DeviceIdentifier deviceIdentifier) {
        super();
        this.profileObisCode = profileObisCode;
        this.deviceIdentifier = deviceIdentifier;
    }

    @Override
    public LoadProfile getLoadProfile() {
        if (loadProfile == null) {
            final List<LoadProfile> loadProfiles = getLoadProfileFactory().findByDevice(deviceIdentifier.findDevice());
            for (LoadProfile profile : loadProfiles) {
                if (profile.getDeviceObisCode().equals(this.profileObisCode)) {
                    this.loadProfile = profile;
                    return loadProfile;
                }
            }
            throw NotFoundException.notFound(LoadProfile.class, this.toString());
        }
        return loadProfile;
    }

    @XmlAttribute
    public DeviceIdentifier getDeviceIdentifier() {
        return deviceIdentifier;
    }

    @Override
    public com.energyict.mdc.upl.meterdata.identifiers.Introspector forIntrospection() {
        return new Introspector();
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
        return "deviceIdentifier = " + deviceIdentifier + " and ObisCode = " + profileObisCode;
    }

    private LoadProfileFactory getLoadProfileFactory() {
        return LoadProfileFactoryProvider.instance.get().getLoadProfileFactory();
    }

    private class Introspector implements com.energyict.mdc.upl.meterdata.identifiers.Introspector {
        @Override
        public String getTypeName() {
            return "DeviceIdentifierAndObisCode";
        }

        @Override
        public Object getValue(String role) {
            switch (role) {
                case "device": {
                    return getDeviceIdentifier();
                }
                case "obisCode": {
                    return getProfileObisCode();
                }
                default: {
                    throw new IllegalArgumentException("Role '" + role + "' is not supported by identifier of type " + getTypeName());
                }
            }
        }
    }

}