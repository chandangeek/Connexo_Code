package com.energyict.mdc.device.data.impl.identifiers;

import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.LoadProfileService;
import com.energyict.mdc.device.data.exceptions.CanNotFindForIdentifier;
import com.energyict.mdc.device.data.impl.MessageSeeds;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifier;

import com.energyict.obis.ObisCode;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Implementation of a LoadProfileIdentifier that uniquely identifies a LoadProfile with its database ID.
 *
 * Copyrights EnergyICT
 * Date: 13/05/13
 * Time: 13:30
 */
@XmlRootElement
public final class LoadProfileIdentifierById implements LoadProfileIdentifier {

    private Long id;
    private LoadProfileService loadProfileService;
    private final ObisCode profileObisCode;

    private LoadProfile loadProfile;

    /**
     * Constructor only to be used by JSON (de)marshalling.
     */
    @SuppressWarnings("unused")
    public LoadProfileIdentifierById() {
        super();
        this.profileObisCode = null;
    }

    public LoadProfileIdentifierById(Long id, LoadProfileService loadProfileService, ObisCode obisCode) {
        this.id = id;
        this.loadProfileService = loadProfileService;
        this.profileObisCode = obisCode;
    }

    @Override
    public ObisCode getProfileObisCode() {
        return profileObisCode;
    }

    @Override
    public LoadProfile getLoadProfile() {
        if (loadProfile == null) {
            this.loadProfile = this.loadProfileService.findById(id).orElseThrow(() -> CanNotFindForIdentifier.loadProfile(this, MessageSeeds.CAN_NOT_FIND_FOR_LOADPROFILE_IDENTIFIER));
        }
        return loadProfile;
    }

    @XmlAttribute
    public Long getId() {
        return id;
    }

    @Override
    public com.energyict.mdc.upl.meterdata.identifiers.Introspector forIntrospection() {
        return new Introspector();
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
    public DeviceIdentifier getDeviceIdentifier() {
        return new DeviceIdentifierByLoadProfile(this);
    }

    @Override
    public String toString() {
        return "load profile having id " + this.id;
    }

    private class Introspector implements com.energyict.mdc.upl.meterdata.identifiers.Introspector {
        @Override
        public String getTypeName() {
            return "DatabaseId";
        }

        @Override
        public Object getValue(String role) {
            if ("databaseValue".equals(role)) {
                return getId();
            } else {
                throw new IllegalArgumentException("Role '" + role + "' is not supported by identifier of type " + getTypeName());
            }
        }
    }

}