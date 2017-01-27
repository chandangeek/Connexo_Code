package com.energyict.mdc.device.data.impl.identifiers;

import com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifier;

import com.energyict.obis.ObisCode;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
    private final ObisCode profileObisCode;

    /**
     * Constructor only to be used by JSON (de)marshalling.
     */
    @SuppressWarnings("unused")
    public LoadProfileIdentifierById() {
        super();
        this.profileObisCode = null;
    }

    public LoadProfileIdentifierById(Long id, ObisCode obisCode) {
        this.id = id;
        this.profileObisCode = obisCode;
    }

    @Override
    public ObisCode getProfileObisCode() {
        return profileObisCode;
    }

    @XmlAttribute
    public Long getId() {
        return id;
    }

    @Override
    public com.energyict.mdc.upl.meterdata.identifiers.Introspector forIntrospection() {
        return new Introspector();
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
        public Set<String> getRoles() {
            return new HashSet<>(Collections.singletonList("databaseValue"));
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