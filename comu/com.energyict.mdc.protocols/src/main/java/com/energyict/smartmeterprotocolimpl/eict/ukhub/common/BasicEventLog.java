/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.eict.ukhub.common;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.cosem.CosemObjectFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BasicEventLog {

    private ObisCode obisCode;
    private final CosemObjectFactory cosemObjectFactory;
    private final Logger logger;

    public BasicEventLog(ObisCode obisCode, CosemObjectFactory cosemObjectFactory, Logger logger) {
        this.obisCode = obisCode;
        this.cosemObjectFactory = cosemObjectFactory;
        this.logger = logger;
    }

    public List<MeterEvent> getEvents(Calendar from) throws IOException {
        getLogger().log(Level.INFO, "Fetching EVENTS from [" + getObisCode() + "] from [" + from.getTime() + "]");
        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();

        try {
            Array eventArray = getEventArray(from);
            for (AbstractDataType abstractEventData : eventArray.getAllDataTypes()) {
                BasicEvent basicEvent = getBasicEvent(abstractEventData);
                if (basicEvent != null) {
                    meterEvents.add(basicEvent.getMeterEvent(obisCode.getE()));
                }
            }
        } catch (IOException e) {
            if(e.getMessage().indexOf("ConnectionException: receiveResponse() interframe timeout error") >= 0){
                throw e;
            }
            getLogger().severe("Unable to fetch EVENTS from [" + getObisCode() + "]: " + e.getMessage());
        }

        return meterEvents;
    }

    private BasicEvent getBasicEvent(AbstractDataType abstractEventData) {
        try {
            if (abstractEventData.isStructure()) {
                Structure structure = abstractEventData.getStructure();
                return structure != null ? new BasicEvent(structure, getLogger()) : null;
            } else {
                getLogger().severe("Expected Array of Structures but one entry was a [" + abstractEventData.getClass().getName() + "]");
            }
        } catch (IOException e) {
            getLogger().severe("Unable to parse single event entry: " + e.getMessage());
        }
        return null;
    }

    private Array getEventArray(Calendar from) throws IOException {
        byte[] rawData = getCosemObjectFactory().getProfileGeneric(getObisCode()).getBufferData(from);
        AbstractDataType abstractData = AXDRDecoder.decode(rawData);
        if (!abstractData.isArray()) {
            throw new IOException("Expected Array of events, but received [" + abstractData.getClass().getName() + "]");
        }
        return abstractData.getArray();
    }


    public Logger getLogger() {
        return logger;
    }

    public CosemObjectFactory getCosemObjectFactory() {
        return cosemObjectFactory;
    }

    public ObisCode getObisCode() {
        return obisCode;
    }

}
