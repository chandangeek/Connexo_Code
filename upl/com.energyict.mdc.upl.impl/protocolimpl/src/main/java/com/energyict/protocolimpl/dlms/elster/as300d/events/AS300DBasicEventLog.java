package com.energyict.protocolimpl.dlms.elster.as300d.events;

import com.energyict.dlms.DlmsSession;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.genericprotocolimpl.elster.AM100R.Apollo.eventhandling.ApolloEvents;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimpl.base.DefaultMeterEventMapper;
import com.energyict.protocolimpl.base.MeterEventMapper;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 * Date: 22/07/11
 * Time: 11:33
 */
public class AS300DBasicEventLog {

    private final ObisCode obisCode;
    private final MeterEventMapper eventMapper;
    private final DlmsSession session;
    private final String name;

    public AS300DBasicEventLog(DlmsSession session, ObisCode obisCode, int eventGroup, String name) {
        this.session = session;
        this.obisCode = obisCode;
        this.eventMapper = new AS300EventLogMapper(eventGroup);
        this.name = name == null ? "" : name;
    }

    public List<MeterEvent> getEvents(Calendar from, Calendar to) throws IOException {
        session.getLogger().log(Level.INFO, "Fetching EVENTS from [" + obisCode + "], [" + name + "] from [" + from.getTime() + "] to [" + to.getTime() + "]");
        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
        Array eventArray = getEventArray(from, to);
        for (AbstractDataType abstractEventData : eventArray.getAllDataTypes()) {
            BasicEvent basicEvent = getBasicEvent(abstractEventData);
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
        AbstractDataType abstractData = AXDRDecoder.decode(rawData);
        if (!abstractData.isArray()) {
            throw new IOException("Expected Array of events, but received [" + abstractData.getClass().getName() + "]");
        }
        return abstractData.getArray();
    }

    private static class BasicEvent extends Structure {

        private final TimeZone timeZone;

        public BasicEvent(Structure eventStructure, TimeZone timeZone) throws IOException {
            super(eventStructure.getBEREncodedByteArray(), 0, 0);
            this.timeZone = timeZone;
        }

        public MeterEvent getMeterEvent(MeterEventMapper mapper) throws IOException {
            return mapper.getMeterEvent(getEventTime(), getEventCode());
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

    private static class AS300EventLogMapper extends DefaultMeterEventMapper {

        private final int eventGroup;

        public AS300EventLogMapper(int eventGroup) {
            this.eventGroup = eventGroup;
        }

        public int getEisEventCode(int meterEventCode) {
            return ApolloEvents.find(meterEventCode, eventGroup).getEIServerCode();
        }

        public String getEventMessage(int meterEventCode) {
            return ApolloEvents.find(meterEventCode, eventGroup).getDescription();
        }

    }

}
