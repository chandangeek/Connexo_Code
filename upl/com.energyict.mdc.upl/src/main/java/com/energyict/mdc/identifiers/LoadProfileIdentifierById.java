package com.energyict.mdc.identifiers;

import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifier;

import com.energyict.obis.ObisCode;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Implementation of a {@link LoadProfileIdentifier} that uniquely identifies
 * a {@link com.energyict.mdc.upl.meterdata.LoadProfile}
 * based on the id of the LoadProfile.
 *
 * @author sva
 * @since 09/07/2014 - 13:54
 */
@XmlRootElement
public class LoadProfileIdentifierById implements LoadProfileIdentifier {

    private final long loadProfileId;
    private final ObisCode loadProfileObisCode;
    private final DeviceIdentifier deviceIdentifier;

    // For JSON serialization only
    @SuppressWarnings("unused")
    public LoadProfileIdentifierById() {
        this.loadProfileId = 0;
        this.loadProfileObisCode = null;
        this.deviceIdentifier = null;
    }

    public LoadProfileIdentifierById(long loadProfileId, ObisCode profileObisCode, DeviceIdentifier deviceIdentifier) {
        super();
        this.loadProfileId = loadProfileId;
        this.loadProfileObisCode = profileObisCode;
        this.deviceIdentifier = deviceIdentifier;
    }

    @XmlElements( {
            @XmlElement(type = DeviceIdentifierById.class),
            @XmlElement(type = DeviceIdentifierBySerialNumber.class),
            @XmlElement(type = DeviceIdentifierByMRID.class),
            @XmlElement(type = DeviceIdentifierForAlreadyKnownDevice.class),
            @XmlElement(type = DeviceIdentifierByDeviceName.class),
    })
    @Override
    public DeviceIdentifier getDeviceIdentifier() {
        return deviceIdentifier;
    }

    @Override
    @XmlAttribute
    public ObisCode getLoadProfileObisCode() {
        return loadProfileObisCode;
    }

    @XmlAttribute
    public long getLoadProfileId() {
        return loadProfileId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LoadProfileIdentifierById that = (LoadProfileIdentifierById) o;
        return loadProfileId == that.loadProfileId;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.loadProfileId);
    }

    @Override
    public com.energyict.mdc.upl.meterdata.identifiers.Introspector forIntrospection() {
        return new Introspector();
    }

    @Override
    public String toString() {
        return "id = " + this.loadProfileId;
    }

    private class Introspector implements com.energyict.mdc.upl.meterdata.identifiers.Introspector {
        @Override
        public String getTypeName() {
            return "DatabaseId";
        }

        @Override
        public Set<String> getRoles() {
            return new HashSet<>(Arrays.asList("databaseValue", "device", "obisCode"));
        }

        @Override
        public Object getValue(String role) {
            switch (role) {
                case "databaseValue":
                    return loadProfileId;
                case "device":
                    return getDeviceIdentifier();
                case "obisCode":
                    return getLoadProfileObisCode();
                default:
                    throw new IllegalArgumentException("Role '" + role + "' is not supported by identifier of type " + getTypeName());
            }
        }
    }

}