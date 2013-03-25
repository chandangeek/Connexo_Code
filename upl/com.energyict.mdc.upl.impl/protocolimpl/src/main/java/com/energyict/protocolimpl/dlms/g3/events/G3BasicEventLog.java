package com.energyict.protocolimpl.dlms.g3.events;

import com.energyict.dlms.DlmsSession;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimpl.dlms.DLMSMeterEventMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 * Date: 22/07/11
 * Time: 11:33
 */
public class G3BasicEventLog implements EventLog {

    private final ObisCode obisCode;
    private final DLMSMeterEventMapper eventMapper;
    private final DlmsSession session;
    private final String name;

    public G3BasicEventLog(DlmsSession session, ObisCode obisCode, String name, DLMSMeterEventMapper eventMapper) {
        this.session = session;
        this.obisCode = obisCode;
        this.eventMapper = eventMapper;
        this.name = name == null ? "" : name;
    }

    public List<MeterEvent> getEvents(Calendar from, Calendar to) throws IOException {
        session.getLogger().log(Level.INFO, "Fetching EVENTS from [" + obisCode + "], [" + name + "] from [" + from.getTime() + "] to [" + to.getTime() + "]");
        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();

        final Array eventArray = getEventArray(from, to);
        if (eventArray == null) {
            throw new IOException("Array of AXDR events received from the meter is 'null'!");
        }

        for (final AbstractDataType abstractEventData : eventArray.getAllDataTypes()) {
            final BasicEvent basicEvent = getBasicEvent(abstractEventData);
            if (basicEvent != null) {
                meterEvents.add(basicEvent.getMeterEvent(eventMapper));
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
        byte[] rawData = session.getCosemObjectFactory().getProfileGeneric(obisCode).getBufferData(from, to);
        return AXDRDecoder.decode(rawData, Array.class);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("G3BasicEventLog");
        sb.append("{obisCode=").append(obisCode);
        sb.append(", name='").append(name).append('\'');
        sb.append('}');
        return sb.toString();
    }

    private class BasicEvent extends Structure {

        private final TimeZone timeZone;

        public BasicEvent(Structure eventStructure, TimeZone timeZone) throws IOException {
            super(eventStructure.getBEREncodedByteArray(), 0, 0);
            this.timeZone = timeZone;
        }

        public MeterEvent getMeterEvent(DLMSMeterEventMapper mapper) throws IOException {
            return mapper.getMeterEvent(
                    getEventTime(),
                    getEventCode(),
                    G3BasicEventLog.this.obisCode.getE(),
                    Collections.<AbstractDataType>emptyList()
            );
        }

        private Date getEventTime() throws IOException {
            OctetString eventDateString = getDataType(0).getOctetString();
            AXDRDateTime timeStamp = new AXDRDateTime(eventDateString.getBEREncodedByteArray(), 0, timeZone);
            return timeStamp.getValue().getTime();
        }

        private int getEventCode() {
            return getDataType(1).intValue();
        }
    }
}