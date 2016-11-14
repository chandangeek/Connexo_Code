package com.energyict.mdc.upl.meterdata.identifiers;

import com.energyict.mdc.upl.meterdata.LoadProfile;

import com.energyict.obis.ObisCode;
import org.codehaus.jackson.annotate.JsonTypeInfo;

import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;
import java.util.List;

/**
 * Provides functionality to identify a specific LoadProfile of a Device.
 * <p/>
 * Copyrights EnergyICT
 * Date: 15/10/12
 * Time: 14:01
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
public interface LoadProfileIdentifier extends Serializable {

    /**
     * Returns the LoadProfile that is uniquely identified by this identifier.
     * Note that this may throw a runtime exception when the LoadProfile could
     * either not be found or multiple load profiles were found in which case
     * this identifier was not as unique as you thought it was.
     *
     * @return the LoadProfile
     */
    LoadProfile getLoadProfile();

    ObisCode getProfileObisCode();

    /**
     * The type of this identifier.
     */
    LoadProfileIdentifierType getLoadProfileIdentifierType();

    /**
     * The essential part(s) of this identifier: the database ID, deviceIdentifier and ObisCode, ...
     */
    List<Object> getParts();

    // The element below is only used during JSON xml (un)marshalling.
    @XmlElement(name = "type")
    String getXmlType();

    void setXmlType(String ignore);

}