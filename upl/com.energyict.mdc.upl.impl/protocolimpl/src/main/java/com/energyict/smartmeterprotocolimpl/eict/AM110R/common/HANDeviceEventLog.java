package com.energyict.smartmeterprotocolimpl.eict.AM110R.common;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author sva
 * @since 14/05/13 - 13:23
 */
public class HANDeviceEventLog extends BasicEventLog{

    public HANDeviceEventLog(ObisCode obisCode, CosemObjectFactory cosemObjectFactory, Logger logger) {
        super(obisCode, cosemObjectFactory, logger);
    }

    public List<MeterEvent> getEvents(Calendar from) throws IOException {
        getLogger().log(Level.INFO, "Fetching EVENTS from [" + getObisCode() + "] from [" + from.getTime() + "]");
        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();

        Array eventArray = getEventArray(from);
        for (AbstractDataType abstractEventData : eventArray.getAllDataTypes()) {
            HANDeviceEvent hanDeviceEvent = getHANDeviceEvent(abstractEventData);
            if (hanDeviceEvent != null) {
                meterEvents.add(hanDeviceEvent.getMeterEvent(getObisCode().getE()));
            }
        }

        return meterEvents;
    }

    private HANDeviceEvent getHANDeviceEvent(AbstractDataType abstractEventData) throws IOException {
        try {
            if (abstractEventData.isStructure()) {
                Structure structure = abstractEventData.getStructure();
                return structure != null ? new HANDeviceEvent(structure, getLogger()) : null;
            } else {
                throw new IOException("Expected Array of Structures but one entry was a [" + abstractEventData.getClass().getName() + "]");
            }
        } catch (IOException e) {
            throw new IOException("Unable to parse single event entry: " + e.getMessage());
        }
    }
}
