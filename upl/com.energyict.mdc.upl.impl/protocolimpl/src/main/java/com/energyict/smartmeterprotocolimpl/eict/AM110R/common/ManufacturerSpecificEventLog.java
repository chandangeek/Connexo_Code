package com.energyict.smartmeterprotocolimpl.eict.AM110R.common;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
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
public class ManufacturerSpecificEventLog extends BasicEventLog{


    public ManufacturerSpecificEventLog(ObisCode obisCode, CosemObjectFactory cosemObjectFactory, Logger logger) {
        super(obisCode, cosemObjectFactory, logger);
    }

    public List<MeterEvent> getEvents(Calendar from) throws IOException {
        getLogger().log(Level.INFO, "Fetching EVENTS from [" + getObisCode() + "] from [" + from.getTime() + "]");
        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();

        Array eventArray = getEventArray(from);
        for (AbstractDataType abstractEventData : eventArray.getAllDataTypes()) {
            BasicEvent basicEvent = getBasicEvent(abstractEventData);
            if (basicEvent != null) {
                meterEvents.add(basicEvent.getManufactuerSpecificEvent(getObisCode().getE()));
            }
        }

        return meterEvents;
    }
}
