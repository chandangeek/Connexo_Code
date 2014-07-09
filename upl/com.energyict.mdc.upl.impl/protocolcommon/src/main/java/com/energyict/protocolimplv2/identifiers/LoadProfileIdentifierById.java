package com.energyict.protocolimplv2.identifiers;

import com.energyict.cbo.NotFoundException;
import com.energyict.mdc.meterdata.identifiers.LoadProfileIdentifier;
import com.energyict.mdw.core.LoadProfile;
import com.energyict.mdw.core.LoadProfileFactory;
import com.energyict.mdw.core.LoadProfileFactoryProvider;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Implementation of a {@link com.energyict.mdc.meterdata.identifiers.LoadProfileIdentifier} that uniquely identifies a {@link com.energyict.mdw.core.LoadProfile}
 * based on the id of the LoadProfile
 *
 * @author sva
 * @since 09/07/2014 - 13:54
 */
@XmlRootElement
public class LoadProfileIdentifierById implements LoadProfileIdentifier {

    private final int loadProfileId;

    /**
     * Constructor only to be used by JSON (de)marshalling
     */
    public LoadProfileIdentifierById() {
        this.loadProfileId = 0;
    }

    public LoadProfileIdentifierById(int loadProfileId) {
        super();
        this.loadProfileId = loadProfileId;
    }

    @Override
    public LoadProfile getLoadProfile() {
        LoadProfile loadProfile = getLoadProfileFactory().find(this.loadProfileId);
        if (loadProfile == null) {
            throw new NotFoundException("LoadProfile with id " + this.loadProfileId + " not found");
        } else {
            return loadProfile;
        }
    }

    @XmlAttribute
    public int getLoadProfileId() {
        return loadProfileId;
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
}