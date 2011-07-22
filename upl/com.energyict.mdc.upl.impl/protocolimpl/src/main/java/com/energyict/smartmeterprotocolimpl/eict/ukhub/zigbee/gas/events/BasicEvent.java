package com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.gas.events;

import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.protocol.MeterEvent;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.common.SswgEvents;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 22/07/11
 * Time: 11:45
 */
public class BasicEvent extends Structure {

    public static final int MINIMUM_SIZE = 2;

    private static final int EVENT_TIME_INDEX = 0;
    private static final int EVENT_CODE_INDEX = 1;

    private final Logger logger;

    public BasicEvent(Structure eventStructure, Logger logger) throws IOException {
        super(eventStructure.getBEREncodedByteArray(), 0, 0);
        this.logger = logger;
        validate();
    }

    private void validate() throws IOException {
        if (nrOfDataTypes() < MINIMUM_SIZE) {
            throw new IOException("Event structure should have at least [" + MINIMUM_SIZE + "] fields but has only [" + nrOfDataTypes() + "].");
        }
        if (!getDataType(EVENT_TIME_INDEX).isOctetString()) {
            throw new IOException("Expected OctetString as event time but was [" + getDataType(0).getClass().getName() + "]");
        }
    }

    public Logger getLogger() {
        return logger;
    }

    public MeterEvent getMeterEvent() throws IOException {
        SswgEvents.SswgEvent sswgEvent = SswgEvents.getSswgEventFromDeviceCode(getEventCode());
        return sswgEvent.toMeterEvent(getEventTime());
    }

    private Date getEventTime() throws IOException {
        OctetString eventDateString = getDataType(EVENT_TIME_INDEX).getOctetString();
        return new AXDRDateTime(eventDateString).getValue().getTime();
    }

    private int getEventCode() {
        return getDataType(EVENT_CODE_INDEX).intValue();
    }

}
