package com.energyict.genericprotocolimpl.rtuplusserver.g3;

import com.energyict.dlms.DlmsSession;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 * Date: 22/07/11
 * Time: 11:33
 */
public class RtuPlusServerEventLog extends AbstractDlmsSessionTask {

    public static final ObisCode OBIS_CODE = ObisCode.fromString("0.0.99.98.0.255");
    public static final String NAME = "Standard event log";

    public RtuPlusServerEventLog(DlmsSession session, RtuPlusServerTask task) {
        super(session, task);
    }

    public final void readNewEvents() throws IOException {
        if (getCommunicationProfile().getReadMeterEvents()) {
            final Calendar to = Calendar.getInstance(getTimeZone());
            final Calendar from = Calendar.getInstance(getTimeZone());
            from.setTime(getTask().getLastLogBookDate());
            final List<MeterEvent> meterEvents = readMeterEvents(to, from);
            getTask().getStoreObject().add(getGateway(), meterEvents);
        } else {
            getLogger().info("Readout of meter events disabled in communication profile [" + getCommunicationProfile().displayString() + "]. Skipping.");
        }
    }

    private final List<MeterEvent> readMeterEvents(Calendar to, Calendar from) throws IOException {
        getLogger().log(Level.INFO, "Fetching EVENTS from [" + OBIS_CODE + "], [" + NAME + "] from [" + from.getTime() + "] to [" + to.getTime() + "]");
        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
        Array eventArray = getEventArray(from, to);
        for (AbstractDataType abstractEventData : eventArray.getAllDataTypes()) {
            BasicEvent basicEvent = getBasicEvent(abstractEventData);
            if (basicEvent != null) {
                meterEvents.add(basicEvent.getMeterEvent());
            }
        }
        return meterEvents;
    }

    private final BasicEvent getBasicEvent(AbstractDataType abstractEventData) throws IOException {
        if (abstractEventData.isStructure()) {
            Structure structure = abstractEventData.getStructure();
            return structure != null ? new BasicEvent(structure, getTimeZone()) : null;
        } else {
            getLogger().severe("Expected Array of Structures but one entry was a [" + abstractEventData.getClass().getName() + "]");
        }
        return null;
    }

    private final Array getEventArray(Calendar from, Calendar to) throws IOException {
        byte[] rawData = getCosemObjectFactory().getProfileGeneric(OBIS_CODE).getBufferData(from, to);
        AbstractDataType abstractData = AXDRDecoder.decode(rawData);
        if (!abstractData.isArray()) {
            throw new IOException("Expected Array of events, but received [" + abstractData.getClass().getName() + "]");
        }
        return abstractData.getArray();
    }

    private static class BasicEvent extends Structure {

        private static final int DATE_TIME_INDEX = 0;
        private static final int EIS_CODE_INDEX = 1;
        private static final int PROTOCOL_CODE_INDEX = 2;
        private static final int DESCRIPTION_INDEX = 3;

        private final TimeZone timeZone;

        public BasicEvent(Structure eventStructure, TimeZone timeZone) throws IOException {
            super(eventStructure.getBEREncodedByteArray(), 0, 0);
            this.timeZone = timeZone;
        }

        public final MeterEvent getMeterEvent() throws IOException {
            return new MeterEvent(getEventTime(), getEisCode(), getProtocolCode(), getDescription());
        }

        private final Date getEventTime() throws IOException {
            OctetString eventDateString = getDataType(DATE_TIME_INDEX).getOctetString();
            AXDRDateTime timeStamp = new AXDRDateTime(eventDateString.getBEREncodedByteArray(), 0, timeZone);
            return timeStamp.getValue().getTime();
        }

        private final int getEisCode() {
            return getDataType(EIS_CODE_INDEX).intValue();
        }

        private final int getProtocolCode() {
            return getDataType(PROTOCOL_CODE_INDEX).intValue();
        }

        private final String getDescription() {
            return getDataType(DESCRIPTION_INDEX).getOctetString().stringValue();
        }

    }

}
