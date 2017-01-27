package com.energyict.mdc.upl.meterdata;

import com.energyict.obis.ObisCode;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.time.temporal.TemporalAmount;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
public interface LoadProfileType {

    /**
     * Returns a description for the LoadProfileType.
     *
     * @return the description
     */
    @XmlAttribute
    String getDescription();

    /**
     * Returns the ObisCode for the LoadProfileType.
     *
     * @return the ObisCode (referring to a generic collection of channels having the same interval)
     */
    @XmlAttribute
    ObisCode getObisCode();

    /**
     * Returns the LoadProfile integration period.
     *
     * @return the integration period.
     */
    @XmlAttribute
    TemporalAmount interval();

    // The element below is only used during JSON xml (un)marshalling.
    @XmlElement(name = "type")
    String getXmlType();

    void setXmlType(String ignore);

}