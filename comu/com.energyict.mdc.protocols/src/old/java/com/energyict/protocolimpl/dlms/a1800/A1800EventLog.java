package com.energyict.protocolimpl.dlms.a1800;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.dlms.DlmsSession;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.obis.ObisCode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;

/**
 * Event log of a1800
 * <p/>
 * Created by heuckeg on 13.06.2014.
 */
@SuppressWarnings("unused")
public class A1800EventLog {

    public static final ObisCode EVENT_LOG = ObisCode.fromString("1.1.99.98.0.255");

    private final ObisCode obisCode;
    private final DlmsSession session;

    public A1800EventLog(DlmsSession session, ObisCode obisCode) {
        this.session = session;
        this.obisCode = obisCode;
    }

    protected DlmsSession getSession() {
        return session;
    }

    public List<MeterEvent> getEvents(Date from, Date to) throws IOException {
        MeterEventMapper eventMapper = new MeterEventMapper();
        Calendar fromC = getCalendar(from);
        Calendar toC = getCalendar(to);
        session.getLogger().log(Level.INFO, "Fetching EVENTS from " + obisCode.toString() + " from [" + fromC.getTime() + "] to [" + toC.getTime() + "]");
        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
        Array eventArray = getEventArray(fromC, toC);
        for (AbstractDataType abstractEventData : eventArray.getAllDataTypes()) {
            BasicEvent basicEvent = getBasicEvent(abstractEventData);
            if (basicEvent != null) {
                MeterEvent me = basicEvent.getMeterEvent(eventMapper);
                if (me != null) {
                    meterEvents.add(me);
                }
            }
        }
        return meterEvents;
    }

    private BasicEvent getBasicEvent(AbstractDataType abstractEventData) throws IOException {
        if (abstractEventData.isStructure()) {
            Structure structure = abstractEventData.getStructure();
            return structure != null ? new BasicEvent(structure, session.getTimeZone()) : null;
        } else {
            session.getLogger().severe("Expected Array of Structures but one entry was a [" + abstractEventData.getClass().getName() + "]");
        }
        return null;
    }

    private Array getEventArray(Calendar from, Calendar to) throws IOException {
        byte[] rawData = session.getCosemObjectFactory().getProfileGeneric(obisCode).getBufferData(0, 0, 0, 0);
        AbstractDataType abstractData = AXDRDecoder.decode(rawData);
        if (!abstractData.isArray()) {
            throw new IOException("Expected Array of events, but received [" + abstractData.getClass().getName() + "]");
        }
        return abstractData.getArray();
    }

    protected final Calendar getCalendar(Date date) {
        Calendar calendar = Calendar.getInstance(getSession().getTimeZone());
        calendar.setTime(date);
        return calendar;
    }

    private static final class BasicEvent extends Structure {

        private final TimeZone timeZone;
        private final int eventCode;
        private final Date eventTime;

        public BasicEvent(Structure eventStructure, TimeZone timeZone) throws IOException {
            super(eventStructure.getBEREncodedByteArray(), 0, 0);

            OctetString eventDateString = getDataType(0).getOctetString();
            AXDRDateTime timeStamp = new AXDRDateTime(eventDateString.getBEREncodedByteArray(), 0, timeZone);

            this.eventTime = timeStamp.getValue().getTime();
            this.eventCode = getDataType(3).intValue();
            this.timeZone = timeZone;
        }

        public MeterEvent getMeterEvent(MeterEventMapper mapper) throws IOException {
            return mapper.getMeterEvent(getEventTime(), getEventCode());
        }

        private Date getEventTime() throws IOException {
            return this.eventTime;
        }

        private int getEventCode() {
            return this.eventCode;
        }
    }

    private static class MeterEventMapper {

        public MeterEvent getMeterEvent(Date eventTime, int eventCode) {
            int eisEvent;
            switch (eventCode) {
                case 1:
                    eisEvent = MeterEvent.POWERDOWN;
                    break;
                case 2:
                    eisEvent = MeterEvent.POWERUP;
                    break;
                case 3:
                    eisEvent = MeterEvent.SETCLOCK_BEFORE;
                    break;
                case 4:
                    eisEvent = MeterEvent.SETCLOCK_AFTER;
                    break;
                case 11:
                    eisEvent = MeterEvent.CONFIGURATIONCHANGE;
                    break;
                case 18:
                    eisEvent = MeterEvent.EVENT_LOG_CLEARED;
                    break;
                case 20:
                    eisEvent = MeterEvent.MAXIMUM_DEMAND_RESET;
                    break;
                case 21: // self read
                    eisEvent = MeterEvent.OTHER;
                    break;
                case 32: // test mode
                    eisEvent = MeterEvent.OTHER;
                    break;
                case 33: // test mode stopped
                    eisEvent = MeterEvent.OTHER;
                    break;
                case 2048: // enter tier override
                    eisEvent = MeterEvent.OTHER;
                    break;
                case 2049: // exit tier override
                    eisEvent = MeterEvent.OTHER;
                    break;
                case 2050: // terminal cover tamper
                    eisEvent = MeterEvent.TAMPER;
                    break;
                case 2051: // main cover tamper
                    eisEvent = MeterEvent.COVER_OPENED;
                    break;
                case 2052: // external event 0
                    eisEvent = MeterEvent.OTHER;
                    break;
                case 2053: // external event 1
                    eisEvent = MeterEvent.OTHER;
                    break;
                case 2054: // external event 2
                    eisEvent = MeterEvent.OTHER;
                    break;
                case 2055: // external event 3
                    eisEvent = MeterEvent.OTHER;
                    break;
                case 2056: // phase a off
                    eisEvent = MeterEvent.PHASE_FAILURE;
                    break;
                case 2057: // phase a on
                    eisEvent = MeterEvent.OTHER;
                    break;
                case 2058: // phase b off
                    eisEvent = MeterEvent.PHASE_FAILURE;
                    break;
                case 2059: // phase b on
                    eisEvent = MeterEvent.OTHER;
                    break;
                case 2060: // phase c off
                    eisEvent = MeterEvent.PHASE_FAILURE;
                    break;
                case 2061: // phase c on
                    eisEvent = MeterEvent.OTHER;
                    break;
                case 2062: // remote flash failed
                    eisEvent = MeterEvent.NV_MEMORY_ERROR;
                    break;
                case 2063: // rwp event
                    eisEvent = MeterEvent.OTHER;
                    break;
                default:
                    eisEvent = MeterEvent.OTHER;
            }
            if (eisEvent == 0) {
                return null;
            }

            return new MeterEvent(eventTime, eisEvent, eventCode);
        }
    }
}
