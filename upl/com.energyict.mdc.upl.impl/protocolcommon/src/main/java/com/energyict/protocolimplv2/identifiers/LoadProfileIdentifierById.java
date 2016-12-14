package com.energyict.protocolimplv2.identifiers;

import com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifier;

import com.energyict.mdw.core.LoadProfile;
import com.energyict.mdw.core.LoadProfileFactory;
import com.energyict.mdw.core.LoadProfileFactoryProvider;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.exceptions.identifier.NotFoundException;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

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

    @Override
    public LoadProfile getLoadProfile() {
        LoadProfile loadProfile = getLoadProfileFactory().find(this.loadProfileId);
        if (loadProfile == null) {
            throw NotFoundException.notFound(LoadProfile.class, this.toString());
        } else {
            return loadProfile;
        }
    }

    @XmlAttribute
    public int getLoadProfileId() {
        return loadProfileId;
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
        return "id = " + this.loadProfileId;
    }

    private LoadProfileFactory getLoadProfileFactory() {
        return LoadProfileFactoryProvider.instance.get().getLoadProfileFactory();
    }

    private class Introspector implements com.energyict.mdc.upl.meterdata.identifiers.Introspector {
        @Override
        public String getTypeName() {
            return "DatabaseId";
        }

        @Override
        public Map<String, Object> getValues() {
            return java.util.Collections.singletonMap("databaseValue", loadProfileId);
        }
    }

}