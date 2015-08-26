package com.energyict.smartmeterprotocolimpl.eict.AM110R.common;

import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.protocol.MeterEvent;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 22/07/11
 * Time: 11:45
 */
public class HANDeviceEvent extends Structure {

    public static final int MINIMUM_SIZE = 4;

    private static final int EVENT_TIME_INDEX = 0;
    private static final int EVENT_CODE_INDEX = 1;
    private static final int EVENT_NUMBER_INDEX = 2;
    private static final int MAC_ADDRESS_INDEX = 3;

    private final Logger logger;

    public HANDeviceEvent(Structure eventStructure, Logger logger) throws IOException {
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
        if (!getDataType(MAC_ADDRESS_INDEX).isOctetString()) {
            throw new IOException("Expected OctetString as event MAC address but was [" + getDataType(0).getClass().getName() + "]");
        }
    }

    public Logger getLogger() {
        return logger;
    }

    public MeterEvent getMeterEvent(final int logbookId) throws IOException {
        SswgEvents.SswgEvent sswgEvent = SswgEvents.getSswgEventFromDeviceCode(getEventCode());
        return sswgEvent.toMeterEvent(getEventTime(), logbookId, getEventNumber(), getMACAddress());
    }

    private Date getEventTime() throws IOException {
        OctetString eventDateString = getDataType(EVENT_TIME_INDEX).getOctetString();
        return new AXDRDateTime(eventDateString).getValue().getTime();
    }

    private int getEventCode() {
        return getDataType(EVENT_CODE_INDEX).intValue();
    }

    private int getEventNumber() {
        return getDataType(EVENT_NUMBER_INDEX).intValue();
    }

    private String getMACAddress() {
        byte[] macAddressBytes = getDataType(MAC_ADDRESS_INDEX).getContentByteArray();
        StringBuffer mac = new StringBuffer();
        for (byte each : macAddressBytes) {
            String temp = Integer.toHexString((int) each & 0xFF).toUpperCase();
            mac.append((temp.length() < 2) ? "0" + temp : temp);
            mac.append("-");
        }

        return mac.substring(0, mac.length() - 1);
    }
}
