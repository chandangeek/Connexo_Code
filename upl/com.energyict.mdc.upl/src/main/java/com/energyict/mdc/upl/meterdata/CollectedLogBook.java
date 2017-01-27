package com.energyict.mdc.upl.meterdata;

import com.energyict.mdc.upl.meterdata.identifiers.LogBookIdentifier;

import com.energyict.protocol.MeterProtocolEvent;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.List;

/**
 * A CollectedLogBook identifies a {@link LogBook} (by {@link #getLogBookIdentifier()}),
 * and the respective collected {@link com.energyict.protocol.MeterEvent}s starting from
 * the {@link LogBook#getLastReading()}.
 */
@JsonTypeInfo(
            use = JsonTypeInfo.Id.CLASS,
            include = JsonTypeInfo.As.PROPERTY,
            property = "type")
public interface CollectedLogBook extends CollectedData {

    /**
     *
     * @return a List of collected {@link MeterProtocolEvent}s
     */
    @XmlAttribute
    List<MeterProtocolEvent> getCollectedMeterEvents();

    /**
     * Gets the object that uniquely identify the requested LogBook.
     *
     * @return the {@link LogBookIdentifier logBookIdentifier}
     *         of the BusinessObject which is actionHolder of the request
     */
    @XmlAttribute
    LogBookIdentifier getLogBookIdentifier();

    void setCollectedMeterEvents(List<MeterProtocolEvent> meterEvents);

    void addCollectedMeterEvents(List<MeterProtocolEvent> meterEvents);

    // The element below is only used during JSON xml (un)marshalling.
    @XmlElement(name = "type")
    String getXmlType();

    void setXmlType(String ignore);

}