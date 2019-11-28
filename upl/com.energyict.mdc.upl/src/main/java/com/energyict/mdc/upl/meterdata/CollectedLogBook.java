package com.energyict.mdc.upl.meterdata;

import com.energyict.mdc.identifiers.LogBookIdentifierByDeviceAndObisCode;
import com.energyict.mdc.identifiers.LogBookIdentifierById;
import com.energyict.mdc.identifiers.LogBookIdentifierByObisCodeAndDevice;
import com.energyict.mdc.identifiers.LogBookIdentifierForAlreadyKnowLogBook;
import com.energyict.mdc.upl.meterdata.identifiers.LogBookIdentifier;

import com.energyict.protocol.MeterProtocolEvent;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import java.util.List;

/**
 * A CollectedLogBook identifies a {@link LogBook} (by {@link #getLogBookIdentifier()}),
 * and the respective collected {@link com.energyict.protocol.MeterEvent}s starting from
 * the {@link LogBook#getLastReading()}.
 */
public interface CollectedLogBook extends CollectedData {

    /**
     *
     * @return a List of collected {@link MeterProtocolEvent}s
     */
    @XmlAttribute
    List<MeterProtocolEvent> getCollectedMeterEvents();

    /**
     * @return true when the protocol is pushing events on inbound connection
     */
    @XmlAttribute
    public boolean isAwareOfPushedEvents();

    /**
     * Gets the object that uniquely identify the requested LogBook.
     *
     * @return the {@link LogBookIdentifier logBookIdentifier}
     *         of the BusinessObject which is actionHolder of the request
     */
    @XmlElements( {
            @XmlElement(type = LogBookIdentifierById.class),
            @XmlElement(type = LogBookIdentifierByObisCodeAndDevice.class),
            @XmlElement(type = LogBookIdentifierByDeviceAndObisCode.class),
            @XmlElement(type = LogBookIdentifierForAlreadyKnowLogBook.class),
    })
    LogBookIdentifier getLogBookIdentifier();

    void setCollectedMeterEvents(List<MeterProtocolEvent> meterEvents);

    void addCollectedMeterEvents(List<MeterProtocolEvent> meterEvents);

}