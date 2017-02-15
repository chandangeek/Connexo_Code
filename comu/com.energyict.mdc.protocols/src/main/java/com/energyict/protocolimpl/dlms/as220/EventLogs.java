/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.as220;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.dlms.DataContainer;
import com.energyict.protocolimpl.dlms.as220.plc.events.PLCLog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

/**
 * @author jme
 */
public class EventLogs {

    private static final ObisCode STANDARD_EVENTLOG_OBISCODE = ObisCode.fromString("0.0.99.98.0.255");
    private static final ObisCode DISCONNEC_EVENTLOG_OBISCODE = ObisCode.fromString("0.0.99.98.2.255");
    private static final ObisCode PLC_EVENTLOG_OBISCODE = ObisCode.fromString("0.0.128.0.0.255");

    private final DLMSSNAS220 as220;

    /**
     * @param as220
     */
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
     * Read all the events between the given fromCalendar and toCalendar from
     * all the logbooks available in the AM500. Returns a list of MeterEvents
     *
     * @param fromCalendar
     * @param toCalendar
     * @return
     * @throws IOException
     */
    public List<MeterEvent> getEventLog(Calendar fromCalendar, Calendar toCalendar) {
        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
        meterEvents.addAll(readLogbook(STANDARD_EVENTLOG_OBISCODE, fromCalendar, toCalendar));
        meterEvents.addAll(readLogbook(DISCONNEC_EVENTLOG_OBISCODE, fromCalendar, toCalendar));
        meterEvents.addAll(readPlcLog(fromCalendar, toCalendar));
        return meterEvents;
    }

    private List<MeterEvent> readPlcLog(Calendar fromCalendar, Calendar toCalendar) {
        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
        if (getAs220().isReadPlcLogbook()) {
            if (getAs220().getMeterConfig().isObisCodeInObjectList(PLC_EVENTLOG_OBISCODE)) {
                try {
                    byte[] bufferData = getAs220().getCosemObjectFactory().getProfileGeneric(PLC_EVENTLOG_OBISCODE).getBufferData(fromCalendar, toCalendar);
                    PLCLog plcLog = new PLCLog(bufferData, getAs220().getTimeZone());
                    meterEvents.addAll(plcLog.getMeterEvents());
                } catch (IOException e) {
                    getAs220().getLogger().severe("Unable to read the PLC logbook: " + e.getMessage());
                }
            } else {
                getAs220().getLogger().severe("PLC logbook with obis [" + PLC_EVENTLOG_OBISCODE + "] not found in object list.");
            }
        }
        return meterEvents;
    }

    /**
     * Read the logbook from the meter, and return them as a list of meterEvents.
     * If an error occures, or the logbook does not exist, an empty list will be returned
     *
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
                if ((dc != null) && (dc.getRoot() != null)) {
                    for (int i = 0; i < dc.getRoot().getNrOfElements(); i++) {
                        Date dateTime = dc.getRoot().getStructure(i).getOctetString(0).toDate(getAs220().getTimeZone());
                        if (dateTime != null) {
                            int id = dc.getRoot().getStructure(i).getInteger(1);
                            MeterEvent meterEvent = EventNumber.toMeterEvent(id, dateTime);
                            if (meterEvent != null) {
                                meterEvents.add(meterEvent);
                            }
                        } else {
                            StringBuilder sb = new StringBuilder();
                            sb.append("Skipping event from logbook [").append(obisCode.toString()).append("]. Timestamp was null!");
                            getAs220().getLogger().log(Level.WARNING, sb.toString());
                        }
                    }
                } else {
                    StringBuilder sb = new StringBuilder();
                    sb.append("DataContainer was null while reading logbook [").append(obisCode.toString()).append("] ");
                    sb.append("from [").append(fromCalendar != null ? fromCalendar.getTime() : "null").append("] ");
                    sb.append("to [").append(toCalendar != null ? toCalendar.getTime() : "null").append("].");
                    getAs220().getLogger().log(Level.WARNING, sb.toString());
                }
            } catch (IOException e) {
                StringBuilder sb = new StringBuilder();
                sb.append("IOException while reading the logbook [").append(obisCode.toString()).append("]: ").append(e.getMessage());
                getAs220().getLogger().log(Level.SEVERE, sb.toString());
            }
        }
        return meterEvents;
    }

}
