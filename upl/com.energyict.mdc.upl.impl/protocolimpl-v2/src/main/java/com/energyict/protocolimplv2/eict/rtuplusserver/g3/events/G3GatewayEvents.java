package com.energyict.protocolimplv2.eict.rtuplusserver.g3.events;

import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.meterdata.CollectedLogBook;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.nta.IOExceptionHandler;

import java.io.IOException;
import java.util.*;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 11/06/2014 - 16:29
 */
public class G3GatewayEvents {

    public static final ObisCode OBIS_CODE = ObisCode.fromString("0.0.99.98.0.255");
    private final DlmsSession dlmsSession;

    public G3GatewayEvents(DlmsSession dlmsSession) {
        this.dlmsSession = dlmsSession;
    }

    public List<CollectedLogBook> readEvents(List<LogBookReader> logBooks) {
        List<CollectedLogBook> result = new ArrayList<>();
        for (LogBookReader logBook : logBooks) {
            CollectedLogBook collectedLogBook = MdcManager.getCollectedDataFactory().createCollectedLogBook(logBook.getLogBookIdentifier());
            if (logBook.getLogBookObisCode().equals(OBIS_CODE)) {
                try {
                    Array eventArray = getMainLogBookBuffer(logBook);
                    List<MeterEvent> meterEvents = new ArrayList<>();
                    for (AbstractDataType abstractEventData : eventArray.getAllDataTypes()) {
                        BasicEvent basicEvent = getBasicEvent(abstractEventData);
                        if (basicEvent != null) {
                            meterEvents.add(basicEvent.getMeterEvent());
                        }
                    }
                    collectedLogBook.setCollectedMeterEvents(MeterEvent.mapMeterEventsToMeterProtocolEvents(meterEvents));
                } catch (IOException e) {
                    if (IOExceptionHandler.isUnexpectedResponse(e, dlmsSession)) {
                        collectedLogBook.setFailureInformation(ResultType.NotSupported, MdcManager.getIssueCollector().addWarning(logBook, "logBookXnotsupported", logBook.getLogBookObisCode().toString()));
                    }
                }
            } else {
                collectedLogBook.setFailureInformation(ResultType.NotSupported, MdcManager.getIssueCollector().addWarning(logBook, "logBookXnotsupported", logBook.getLogBookObisCode().toString()));
            }
            result.add(collectedLogBook);
        }
        return result;
    }

    private Array getMainLogBookBuffer(LogBookReader logBook) throws IOException {
        final Calendar from = Calendar.getInstance(dlmsSession.getTimeZone());
        final Calendar to = Calendar.getInstance(dlmsSession.getTimeZone());
        from.setTime(logBook.getLastLogBook());
        byte[] rawData = dlmsSession.getCosemObjectFactory().getProfileGeneric(OBIS_CODE).getBufferData(from, to);
        AbstractDataType abstractData = AXDRDecoder.decode(rawData);
        return abstractData.getArray();
    }

    private final BasicEvent getBasicEvent(AbstractDataType abstractEventData) throws IOException {
        if (abstractEventData.isStructure()) {
            Structure structure = abstractEventData.getStructure();
            return structure != null ? new BasicEvent(structure, dlmsSession.getTimeZone()) : null;
        } else {
            dlmsSession.getLogger().severe("Expected Array of Structures but one entry was a [" + abstractEventData.getClass().getName() + "]");
        }
        return null;
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