package com.energyict.mdc.upl.meterdata;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.xml.bind.annotation.XmlElement;

/**
 * Models a group of {@link Register}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-11-14 (16:50)
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
public interface RegisterGroup {

    // The element below is only used during JSON xml (un)marshalling.
    @XmlElement(name = "type")
    String getXmlType();

    void setXmlType(String ignore);

}