package com.energyict.protocolimplv2.dlms.a1860.events;

import com.energyict.mdc.upl.NotInObjectListException;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.tasks.support.DeviceLogBookSupport;

import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocolimplv2.dlms.a1860.A1860;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class A1860LogBookFactory implements DeviceLogBookSupport {

    private static ObisCode EVENT_LOG = ObisCode.fromString("1.1.99.98.0.255");

    private final List<ObisCode> supportedLogBooks;
    private A1860 protocol;
    private final CollectedDataFactory collectedDataFactory;
    private final IssueFactory issueFactory;

    public A1860LogBookFactory(A1860 protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory){
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;
        this.protocol = protocol;
        supportedLogBooks = new ArrayList<>();
        supportedLogBooks.add(EVENT_LOG);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        List<CollectedLogBook> result = new ArrayList<>();
        for (LogBookReader logBookReader : logBooks) {
            CollectedLogBook collectedLogBook = collectedDataFactory.createCollectedLogBook(logBookReader.getLogBookIdentifier());
            if (isSupported(logBookReader)) {
                ProfileGeneric profileGeneric = null;
                try {
                    profileGeneric = protocol.getDlmsSession().getCosemObjectFactory().getProfileGeneric(logBookReader.getLogBookObisCode());
                } catch (NotInObjectListException e) {
                    collectedLogBook.setFailureInformation(ResultType.InCompatible, issueFactory.createWarning(logBookReader, "logBookXissue", logBookReader.getLogBookObisCode().toString(), e.getMessage()));
                }

                if (profileGeneric != null) {
                    try {
                        List<BasicEvent> basicEvents = new ArrayList<>();
                        protocol.getLogger().info("Fetching EVENTS from " + logBookReader.getLogBookObisCode().toString());
                        byte[] rawData = profileGeneric.getBufferData(0, 0, 0, 0);
                        AbstractDataType abstractData = AXDRDecoder.decode(rawData);
                        if (!abstractData.isArray()) {
                            throw new IOException("Expected Array of events, but received [" + abstractData.getClass().getName() + "]");
                        }

                        Array eventArray = abstractData.getArray();
                        for (AbstractDataType abstractEventData : eventArray.getAllDataTypes()) {
                            BasicEvent basicEvent = getBasicEvent(abstractEventData);
                            basicEvents.add(basicEvent);
                        }

                        collectedLogBook.setCollectedMeterEvents(new StandardEventLog(basicEvents).buildMeterEvent());
                    } catch (IOException e) {
                        if (DLMSIOExceptionHandler.isUnexpectedResponse(e, protocol.getDlmsSessionProperties().getRetries())) {
                            collectedLogBook.setFailureInformation(ResultType.InCompatible, issueFactory.createWarning(logBookReader, "logBookXissue", logBookReader.getLogBookObisCode().toString(), e.getMessage()));
                        }
                    }
                }
            } else {
                collectedLogBook.setFailureInformation(ResultType.NotSupported, issueFactory.createWarning(logBookReader, "logBookXnotsupported", logBookReader.getLogBookObisCode().toString()));
            }
            result.add(collectedLogBook);
        }
        return result;
    }

    private BasicEvent getBasicEvent(AbstractDataType abstractEventData) throws IOException {
        if (abstractEventData.isStructure()) {
            Structure structure = abstractEventData.getStructure();
            return structure != null ? new BasicEvent(structure, protocol.getTimeZone()) : null;
        } else {
            protocol.getLogger().severe("Expected Array of Structures but one entry was a [" + abstractEventData.getClass().getName() + "]");
        }
        return null;
    }

    static final class BasicEvent extends Structure {

        private final int eventCode;
        private final Date eventTime;

        BasicEvent(Structure eventStructure, TimeZone timeZone) throws IOException {
            super(eventStructure.getBEREncodedByteArray(), 0, 0);

            OctetString eventDateString = getDataType(0).getOctetString();
            AXDRDateTime timeStamp = new AXDRDateTime(eventDateString.getBEREncodedByteArray(), 0, timeZone);

            this.eventTime = timeStamp.getValue().getTime();
            this.eventCode = getDataType(3).intValue();
        }

        public Date getEventTime() throws IOException {
            return this.eventTime;
        }

        public int getEventCode() {
            return this.eventCode;
        }
    }

    private boolean isSupported(LogBookReader logBookReader) {
        for (ObisCode supportedLogBook : supportedLogBooks) {
            if (supportedLogBook.equalsIgnoreBChannel(logBookReader.getLogBookObisCode())) {
                return true;
            }
        }
        return false;
    }
}