/*
 * Copyright (c) 2016 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocol;

import com.energyict.cim.EndDeviceEventType;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@XmlRootElement
public class MeterProtocolEvent implements Serializable, Comparable<MeterEvent> {

    /**
     * The time the Event was recorded (this is not the time the event was read from the device)
     */
    private final Date time;
    /**
     * The EIServer code of this event
     */
    private final int eiCode;
    /**
     * The event code we received from the device
     */
    private final int protocolCode;
    /**
     * The CIM {@link EndDeviceEventType}
     */
    private final EndDeviceEventType eventType;
    /**
     * The text that may clarify the event
     */
    private final String message;
    /**
     * Identifies the <i>LogBook</i> ID of the device (CIM logbook id)
     * <i>(don't confuse with our own LogBook.getId())</i>
     */
    private final int eventLogId;
    /**
     * Identifies the (sequential) ID of the event in the particular logbook of the Device.
     */
    private final int deviceEventId;

    /**
     * Keeps track of additional information regarding this MeterProtocolEvent.
     */
    private Map<String, String> additionalInformation = new HashMap<>();

    /**
     * Constructor only to be used by JSON (de)marshalling
     */
    private MeterProtocolEvent() {
        this.time = null;
        this.eiCode = -1;
        this.protocolCode = -1;
        this.eventType = null;
        this.message = null;
        this.eventLogId = -1;
        this.deviceEventId = -1;
    }

    /**
     * Create a new MeterProtocolEvent with the given arguments
     *
     * @param time            the time the Event was recorded (this is not the time the event was read from the device)
     * @param eiCode          the EIServer code of this event
     * @param protocolCode    the event code we received from the device
     * @param eventType       the CIM {@link EndDeviceEventType}
     * @param message         the text that may clarify the event
     * @param meterEventLogId identifies the <i>LogBook</i> ID of the device (CIM logbook id) <i>(don't confuse with our own LogBook.getId())</i>
     * @param deviceEventId   identifies the (sequential) ID of the event in the particular logbook of the Device.
     */
    public MeterProtocolEvent(Date time, int eiCode, int protocolCode, EndDeviceEventType eventType, String message, int meterEventLogId, int deviceEventId) {
        this.time = time;
        this.eiCode = eiCode;
        this.protocolCode = protocolCode;
        this.eventType = eventType;
        this.message = message;
        this.eventLogId = meterEventLogId;
        this.deviceEventId = deviceEventId;
    }

    public int compareTo(MeterEvent o) {
        return (time.compareTo(o.getTime()));
    }

    @XmlAttribute
    public Date getTime() {
        return time;
    }

    @XmlAttribute
    public int getEiCode() {
        return eiCode;
    }

    @XmlAttribute
    public int getProtocolCode() {
        return protocolCode;
    }

    @XmlAttribute
    public EndDeviceEventType getEventType() {
        return eventType;
    }

    @XmlAttribute
    public String getMessage() {
        return message;
    }

    @XmlAttribute
    public int getEventLogId() {
        return eventLogId;
    }

    @XmlAttribute
    public int getDeviceEventId() {
        return deviceEventId;
    }

    /**
     * Provides a list of additional information regarding this meterevent.
     * It will provide a list of key-value pairs which are provided by the meterprotocol
     *
     * @return list of key-value pairs, giving more information about this event.
     */
    public Map<String, String> getAdditionalInformation() {
        return Collections.unmodifiableMap(this.additionalInformation);
    }

    /**
     * Adds additional information to this event.
     *
     * @param key the key of the info
     * @param value the value of the info
     */
    public void addAdditionalInformation(String key, String value) {
        this.additionalInformation.put(key, value);
    }

}