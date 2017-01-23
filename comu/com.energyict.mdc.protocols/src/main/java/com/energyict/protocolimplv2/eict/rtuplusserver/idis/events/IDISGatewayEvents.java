package com.energyict.protocolimplv2.eict.rtuplusserver.idis.events;

import com.elster.jupiter.metering.MeteringService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.LogBookReader;
import com.energyict.mdc.protocol.api.MessageSeeds;
import com.energyict.mdc.protocol.api.device.data.CollectedLogBook;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;

import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.nta.IOExceptionHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * @author sva
 * @since 15/10/2014 - 14:52
 */
public class IDISGatewayEvents {

    public static final ObisCode OBIS_CODE = ObisCode.fromString("0.0.99.98.0.255");
    private final DlmsSession dlmsSession;
    private final IssueService issueService;
    private final CollectedDataFactory collectedDataFactory;
    private final MeteringService meteringService;

    public IDISGatewayEvents(DlmsSession dlmsSession, IssueService issueService, CollectedDataFactory collectedDataFactory, MeteringService meteringService) {
        this.dlmsSession = dlmsSession;
        this.issueService = issueService;
        this.collectedDataFactory = collectedDataFactory;
        this.meteringService = meteringService;
    }

    public List<CollectedLogBook> readEvents(List<LogBookReader> logBooks) {
        List<CollectedLogBook> result = new ArrayList<>();
        for (LogBookReader logBook : logBooks) {
            CollectedLogBook collectedLogBook = this.collectedDataFactory.createCollectedLogBook(logBook.getLogBookIdentifier());
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
                    collectedLogBook.setMeterEvents(MeterEvent.mapMeterEventsToMeterProtocolEvents(meterEvents, this.meteringService));
                } catch (IOException e) {
                    if (IOExceptionHandler.isUnexpectedResponse(e, dlmsSession)) {
                        collectedLogBook.setFailureInformation(
                                ResultType.NotSupported,
                                this.issueService.newWarning(
                                        logBook,
                                        MessageSeeds.LOGBOOK_NOT_SUPPORTED,
                                        logBook.getLogBookObisCode().toString()));
                    }
                }
            } else {
                collectedLogBook.setFailureInformation(
                        ResultType.NotSupported,
                        this.issueService.newWarning(
                                logBook,
                                MessageSeeds.LOGBOOK_NOT_SUPPORTED,
                                logBook.getLogBookObisCode().toString()));
            }
            result.add(collectedLogBook);
        }
        return result;
    }

    private Array getMainLogBookBuffer(LogBookReader logBook) throws IOException {
        final Calendar from = Calendar.getInstance(dlmsSession.getTimeZone());
        final Calendar to = Calendar.getInstance(dlmsSession.getTimeZone());
        from.setTimeInMillis(logBook.getLastLogBook().toEpochMilli());
        byte[] rawData = dlmsSession.getCosemObjectFactory().getProfileGeneric(OBIS_CODE).getBufferData(from, to);
        AbstractDataType abstractData = AXDRDecoder.decode(rawData);
        return abstractData.getArray();
    }

    private BasicEvent getBasicEvent(AbstractDataType abstractEventData) throws IOException {
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

        private BasicEvent(Structure eventStructure, TimeZone timeZone) throws IOException {
            super(eventStructure.getBEREncodedByteArray(), 0, 0);
            this.timeZone = timeZone;
        }

        public final MeterEvent getMeterEvent() throws IOException {
            return new MeterEvent(getEventTime(), getEisCode(), getProtocolCode(), getDescription());
        }

        private Date getEventTime() throws IOException {
            OctetString eventDateString = getDataType(DATE_TIME_INDEX).getOctetString();
            AXDRDateTime timeStamp = new AXDRDateTime(eventDateString.getBEREncodedByteArray(), 0, timeZone);
            return timeStamp.getValue().getTime();
        }

        private int getEisCode() {
            return getDataType(EIS_CODE_INDEX).intValue();
        }

        private int getProtocolCode() {
            return getDataType(PROTOCOL_CODE_INDEX).intValue();
        }

        private String getDescription() {
            return getDataType(DESCRIPTION_INDEX).getOctetString().stringValue();
        }
    }
}