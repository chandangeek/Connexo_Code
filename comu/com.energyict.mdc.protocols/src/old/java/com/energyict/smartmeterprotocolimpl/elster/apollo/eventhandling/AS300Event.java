/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.elster.apollo.eventhandling;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Logger;

public class AS300Event extends Structure {

    public static final int MINIMUM_SIZE = 2;

    private static final int EVENT_TIME_INDEX = 0;
    private static final int EVENT_CODE_INDEX = 1;
    private static final int EVENT_NUMBER_INDEX = 2;

    private final Logger logger;

    public AS300Event(Structure eventStructure, Logger logger) throws IOException {
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

    public MeterEvent getMeterEvent(final int logbookId) throws IOException {
        AS300EventDefinitions as300Event = AS300EventDefinitions.getEventFromDeviceCode(getEventCode());
        return new MeterEvent(getEventTime(), as300Event.getEiserverCode(), as300Event.getDeviceCode(), as300Event.getDescription(), logbookId, getEventNumber());
    }

    private Date getEventTime() throws IOException {
        OctetString eventDateString = getDataType(EVENT_TIME_INDEX).getOctetString();
        return new AXDRDateTime(eventDateString).getValue().getTime();
    }

    private int getEventCode() {
        return getDataType(EVENT_CODE_INDEX).intValue();
    }

    private int getEventNumber() {
        if(nrOfDataTypes() >= EVENT_NUMBER_INDEX+1){
            return getDataType(EVENT_NUMBER_INDEX).intValue();
        } else {
            return 0;
        }
    }
}
