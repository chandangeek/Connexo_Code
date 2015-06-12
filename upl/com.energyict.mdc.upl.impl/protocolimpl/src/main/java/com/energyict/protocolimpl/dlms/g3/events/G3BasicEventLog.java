package com.energyict.protocolimpl.dlms.g3.events;

import com.energyict.dlms.DlmsSession;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimpl.dlms.DLMSMeterEventMapper;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 22/07/11
 * Time: 11:33
 */
public class G3BasicEventLog implements EventLog {

    private final ObisCode obisCode;
    private final DLMSMeterEventMapper eventMapper;
    private final String name;
    private final CosemObjectFactory cosemObjectFactory;

    private final Logger logger;
    private final TimeZone timeZone;

    public G3BasicEventLog(DlmsSession dlmsSession, ObisCode obisCode, String name, DLMSMeterEventMapper eventMapper) {
        this.cosemObjectFactory = dlmsSession.getCosemObjectFactory();
        this.logger = dlmsSession.getLogger();
        this.timeZone = dlmsSession.getTimeZone();
        this.obisCode = obisCode;
        this.eventMapper = eventMapper;
        this.name = name == null ? "" : name;
    }

    public G3BasicEventLog(CosemObjectFactory cosemObjectFactory, ObisCode obisCode, DLMSMeterEventMapper eventMapper, Logger logger, TimeZone timeZone) {
        this.cosemObjectFactory = cosemObjectFactory;
        this.logger = logger;
        this.timeZone = timeZone;
        this.obisCode = obisCode;
        this.eventMapper = eventMapper;
        this.name = "";
    }

    public ObisCode getObisCode() {
        return obisCode;
    }

    public List<MeterEvent> getEvents(Calendar from, Calendar to) throws IOException {
        getLogger().log(Level.INFO, "Fetching EVENTS from [" + obisCode + "], [" + name + "] from [" + from.getTime() + "] to [" + to.getTime() + "]");

        final Array eventArray = getEventArray(from, to);
        return parseEvents(eventArray);
    }

    public List<MeterEvent> parseEvents(AbstractDataType eventArray) throws IOException {
        if (eventArray == null) {
            throw new IOException("Array of AXDR events received from the meter is 'null'!");
        }

        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
        for (final AbstractDataType abstractEventData : ((Array) eventArray).getAllDataTypes()) {
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
            return structure != null ? new BasicEvent(structure, getTimeZone()) : null;
        } else {
            getLogger().severe("Expected Array of Structures but one entry was a [" + abstractEventData.getClass().getName() + "]");
        }
        return null;
    }

    private Logger getLogger() {
        return logger;
    }

    private TimeZone getTimeZone() {
        return timeZone;
    }

    public CosemObjectFactory getCosemObjectFactory() {
        return cosemObjectFactory;
    }

    private Array getEventArray(Calendar from, Calendar to) throws IOException {
        byte[] rawData = getCosemObjectFactory().getProfileGeneric(obisCode).getBufferData(from, to);
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