/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.device.events;

import com.elster.jupiter.metering.events.EndDeviceEventType;

import java.io.Serializable;
import java.util.Date;

public class MeterProtocolEvent implements Serializable, Comparable {

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

    public int compareTo(Object o) {
        return (time.compareTo(((MeterEvent) o).getTime()));
    }

    public Date getTime() {
        return time;
    }

    public int getEiCode() {
        return eiCode;
    }

    public int getProtocolCode() {
        return protocolCode;
    }

    public EndDeviceEventType getEventType() {
        return eventType;
    }

    public String getMessage() {
        return message;
    }

    public int getEventLogId() {
        return eventLogId;
    }

    public int getDeviceEventId() {
        return deviceEventId;
    }
}
