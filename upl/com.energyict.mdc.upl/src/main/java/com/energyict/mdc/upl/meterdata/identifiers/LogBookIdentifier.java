package com.energyict.mdc.upl.meterdata.identifiers;

import com.energyict.mdc.upl.meterdata.LogBook;

import com.energyict.obis.ObisCode;
import org.codehaus.jackson.annotate.JsonTypeInfo;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;
import java.util.List;

/**
 * Provides functionality to identify a specific {@link LogBook} of a Device.
 * <p/>
 * Copyrights EnergyICT
 * Date: 16/10/12
 * Time: 8:32
 */
@JsonTypeInfo(
            use = JsonTypeInfo.Id.CLASS,
            include = JsonTypeInfo.As.PROPERTY,
            property = "type")
public interface LogBookIdentifier extends Serializable {

    /**
     * Returns the LogBook that is uniquely identified by this identifier.
     * Note that this may throw a runtime exception when the LogBook could
     * either not be found or multiple log books were found in which case
     * this identifier was not as unique as you thought it was.
     *
     * @return the referenced LogBook
     */
    LogBook getLogBook();

    /**
     * Returns the ObisCode of the LogBook referenced by this identifier.
     *
     * @return The ObisCode
     */
    @XmlAttribute
    ObisCode getLogBookObisCode();

    /**
     * The type of this identifier.
     */
    LogBookIdentifierType getLogBookIdentifierType();

    /**
     * The essential part(s) of this identifier: the database ID, deviceIdentifier and ObisCode, ...
     */
    List<Object> getParts();

    // The element below is only used during JSON xml (un)marshalling.
    @XmlElement(name = "type")
    String getXmlType();

    void setXmlType(String ignore);

}