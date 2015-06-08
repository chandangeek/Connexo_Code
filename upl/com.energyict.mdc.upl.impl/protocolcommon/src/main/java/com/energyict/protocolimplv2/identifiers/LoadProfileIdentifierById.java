package com.energyict.protocolimplv2.identifiers;

import com.energyict.cbo.NotFoundException;
import com.energyict.util.Collections;
import com.energyict.mdc.meterdata.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.meterdata.identifiers.LoadProfileIdentifierType;
import com.energyict.mdw.core.LoadProfile;
import com.energyict.mdw.core.LoadProfileFactory;
import com.energyict.mdw.core.LoadProfileFactoryProvider;
import com.energyict.obis.ObisCode;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

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
            throw new NotFoundException("LoadProfile with id " + this.loadProfileId + " not found");
        } else {
            return loadProfile;
        }
    }

    @XmlAttribute
    public int getLoadProfileId() {
        return loadProfileId;
    }

    @Override
    public LoadProfileIdentifierType getLoadProfileIdentifierType() {
        return LoadProfileIdentifierType.DataBaseId;
    }

    @Override
    public List<Object> getIdentifier() {
        return Collections.toList((Object) getLoadProfileId());
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