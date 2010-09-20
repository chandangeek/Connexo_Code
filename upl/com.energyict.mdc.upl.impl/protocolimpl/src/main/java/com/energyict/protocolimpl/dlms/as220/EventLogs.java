package com.energyict.protocolimpl.dlms.as220;

import com.energyict.dlms.DataContainer;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterEvent;

import java.io.IOException;
import java.util.*;

/**
 * @author jme
 */
public class EventLogs {

    private static final ObisCode STANDARD_EVENTLOG_OBISCODE = ObisCode.fromString("0.0.99.98.0.255");
    private static final ObisCode DISCONNEC_EVENTLOG_OBISCODE = ObisCode.fromString("0.0.99.98.2.255");

    private final DLMSSNAS220 as220;

    public EventLogs(DLMSSNAS220 as220) {
        this.as220 = as220;
    }

    /**
     * Getter for the as220 protocol
     *
     * @return
     */
    private DLMSSNAS220 getAs220() {
        return as220;
    }

    /**
     * @param fromCalendar
     * @param toCalendar
     * @return
     * @throws IOException
     */
    public List<MeterEvent> getEventLog(Calendar fromCalendar, Calendar toCalendar) {
        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
        meterEvents.addAll(readLogbook(STANDARD_EVENTLOG_OBISCODE, fromCalendar, toCalendar));
        meterEvents.addAll(readLogbook(DISCONNEC_EVENTLOG_OBISCODE, fromCalendar, toCalendar));
        return meterEvents;
    }

    /**
     * @param obisCode
     * @param fromCalendar
     * @param toCalendar
     * @return
     * @throws IOException
     */
    private List<MeterEvent> readLogbook(ObisCode obisCode, Calendar fromCalendar, Calendar toCalendar) {
        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
        if (getAs220().getMeterConfig().isObisCodeInObjectList(obisCode)) {
            try {
                DataContainer dc = getAs220().getCosemObjectFactory().getProfileGeneric(obisCode).getBuffer(fromCalendar, toCalendar);
                for (int i = 0; i < dc.getRoot().getNrOfElements(); i++) {
                    Date dateTime = dc.getRoot().getStructure(i).getOctetString(0).toDate(getAs220().getTimeZone());
                    int id = dc.getRoot().getStructure(i).getInteger(1);
                    MeterEvent meterEvent = EventNumber.toMeterEvent(id, dateTime);
                    if (meterEvent != null) {
                        meterEvents.add(meterEvent);
                    }
                }
            } catch (IOException e) {
                // Absorb
            }
        }
        return meterEvents;
	}

}
