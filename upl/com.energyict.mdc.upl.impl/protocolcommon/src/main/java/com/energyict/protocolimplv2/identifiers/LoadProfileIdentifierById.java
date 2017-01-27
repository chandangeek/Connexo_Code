package com.energyict.protocolimplv2.identifiers;

import com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifier;

import com.energyict.obis.ObisCode;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Implementation of a {@link LoadProfileIdentifier} that uniquely identifies a {@link com.energyict.mdw.core.LoadProfile}
 * based on the id of the LoadProfile
 *
 * @author sva
 * @since 09/07/2014 - 13:54
 */
@XmlRootElement
public class LoadProfileIdentifierById implements LoadProfileIdentifier {

    private final int loadProfileId;
    private final ObisCode profileObisCode;

    /**
     * Constructor only to be used by JSON (de)marshalling
     */
    private LoadProfileIdentifierById() {
        this.loadProfileId = 0;
        this.profileObisCode = null;
    }

    public LoadProfileIdentifierById(int loadProfileId, ObisCode profileObisCode) {
        super();
        this.loadProfileId = loadProfileId;
        this.profileObisCode = profileObisCode;
    }

    @Override
    @XmlAttribute
    public ObisCode getProfileObisCode() {
        return profileObisCode;
    }

    @XmlAttribute
    public int getLoadProfileId() {
        return loadProfileId;
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
            return new HashSet<>(Collections.singletonList("databaseValue"));
        }

        @Override
        public Object getValue(String role) {
            if ("databaseValue".equals(role)) {
                return loadProfileId;
            } else {
                throw new IllegalArgumentException("Role '" + role + "' is not supported by identifier of type " + getTypeName());
            }
        }
    }

}