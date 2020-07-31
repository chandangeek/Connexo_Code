package com.energyict.protocolimplv2.dlms.as253;

import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.dlms.cosem.TableRead;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.mdc.upl.NotInObjectListException;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.tasks.support.DeviceLogBookSupport;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimpl.elster.a1800.tables.PowerQualityMonitorLog;
import com.energyict.protocolimpl.elster.a1800.tables.SagLog;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class AS253LogBookFactory implements DeviceLogBookSupport {

    private static final Logger sLogger = Logger.getLogger(AS253LogBookFactory.class.getName());
    private static final int OCTESTS = 100;

    private static ObisCode EVENT_LOG = ObisCode.fromString("1.1.99.98.0.255");
    private static ObisCode SAG_LOG = ObisCode.fromString("1.1.99.98.2.255");
    private static ObisCode PQM_LOG = ObisCode.fromString("1.1.99.98.3.255");

    //Obis codes used for retrying the number of entries of SAG_LOG and PQM_LOG
    //DON'T use this obis codes for another purpose
    private static ObisCode PQM_TESTS = ObisCode.fromString("0.0.65.16.48.255");
    private static ObisCode SAG_LOG_CONFIGURATION = ObisCode.fromString("0.0.65.16.52.255");

    private final Map<ObisCode, ObisCode> mapObisCodeAnsiCode = Collections.unmodifiableMap(createMap());

    private final List<ObisCode> supportedLogBooks;
    private final List<ObisCode> supportedAnsiLogBooks;
    private AS253 protocol;
    private final CollectedDataFactory collectedDataFactory;
    private final IssueFactory issueFactory;

    public AS253LogBookFactory(AS253 protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory){
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;
        this.protocol = protocol;
        supportedLogBooks = new ArrayList<>();
        supportedLogBooks.add(EVENT_LOG);
        supportedAnsiLogBooks = new ArrayList<>();
        supportedAnsiLogBooks.add(SAG_LOG);
        supportedAnsiLogBooks.add(PQM_LOG);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        List<CollectedLogBook> result = new ArrayList<>();
        for (LogBookReader logBookReader : logBooks) {
            CollectedLogBook collectedLogBook = collectedDataFactory.createCollectedLogBook(logBookReader.getLogBookIdentifier());
            if (isSupportedDLMS(logBookReader)) {
                getDLMSLogBookData(logBookReader, collectedLogBook);
            } else if (isSupportedANSI(logBookReader)) {
                getANSILogBookData(logBookReader, collectedLogBook);
            } else {
                collectedLogBook.setFailureInformation(ResultType.NotSupported, issueFactory.createWarning(logBookReader, "logBookXnotsupported", logBookReader.getLogBookObisCode().toString()));
            }
            result.add(collectedLogBook);
        }
        return result;
    }

    private void getDLMSLogBookData(LogBookReader logBookReader, CollectedLogBook collectedLogBook) {
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

                //map the meter events in order to change the device type of the code to the correct device type from protocol
                collectedLogBook.setCollectedMeterEvents(new AS253StandardEventLog(basicEvents).buildMeterEvent().stream().map(item -> {item.getEventType().setType(protocol.getTypeMeter()); return item;}).collect(Collectors.toList()));
            } catch (IOException e) {
                if (DLMSIOExceptionHandler.isUnexpectedResponse(e, protocol.getDlmsSessionProperties().getRetries())) {
                    collectedLogBook.setFailureInformation(ResultType.InCompatible, issueFactory.createWarning(logBookReader, "logBookXissue", logBookReader.getLogBookObisCode().toString(), e.getMessage()));
                }
            }
        }
    }

    private void getANSILogBookData(LogBookReader logBookReader, CollectedLogBook collectedLogBook) {
        if (logBookReader.getLogBookObisCode().equals(PQM_LOG)) {
            try {
                int numberOfEntries = getNumberOfEntries(PQM_TESTS, 16, 2);

                if (numberOfEntries > 0) {
                    TableRead tableRead = protocol.getDlmsSession().getCosemObjectFactory().getTableRead(mapObisCodeAnsiCode.get(logBookReader.getLogBookObisCode()));
                    byte[] buffer = fullRead(tableRead, numberOfEntries, 11, 9);
                    PowerQualityMonitorLog powerQualityMonitorLog = new PowerQualityMonitorLog(null, true);
                    powerQualityMonitorLog.parse(buffer);
                    collectedLogBook.setCollectedMeterEvents(MeterEvent.mapMeterEventsToMeterProtocolEvents(powerQualityMonitorLog.getMeterEvents()).stream().map(item -> {item.getEventType().setType(protocol.getTypeMeter()); return item;}).collect(Collectors.toList()));
                }
            } catch (IOException e) {
                sLogger.severe("Error trying to read the table buffer for " + PQM_LOG);
            }
        }
        if (logBookReader.getLogBookObisCode().equals(SAG_LOG)) {
            try {
                int numberOfEntries = getNumberOfEntries(SAG_LOG_CONFIGURATION, 4, 2);

                if (numberOfEntries > 0) {
                    TableRead tableRead = protocol.getDlmsSession().getCosemObjectFactory().getTableRead(mapObisCodeAnsiCode.get(logBookReader.getLogBookObisCode()));
                    byte[] buffer = fullRead(tableRead, numberOfEntries, 7, 9);
                    SagLog sagLog = new SagLog(null, null);
                    sagLog.parse(buffer);
                    collectedLogBook.setCollectedMeterEvents(MeterEvent.mapMeterEventsToMeterProtocolEvents(sagLog.getMeterEvents()).stream().map(item -> {item.getEventType().setType(protocol.getTypeMeter()); return item;}).collect(Collectors.toList()));
                }
            } catch (IOException e) {
                sLogger.severe("Error trying to read the table buffer for " + SAG_LOG);
            }
        }
    }

    /**
     * Read the number of entries for the logbook from specific table.
     */
    private int getNumberOfEntries(ObisCode obisCode, int fromOffset, int length) throws IOException {
        TableRead tableRead = protocol.getDlmsSession().getCosemObjectFactory().getTableRead(obisCode);
        byte[] buffer = tableRead.getBuffer(fromOffset,  length);

        int numberOfEntries = 0;

        if (buffer.length > 2) {
            numberOfEntries = buffer[2];
        }

        return numberOfEntries;
    }

    /**
     * Read the full buffer in a cycle of 100 octets.
     */
    private byte[] fullRead(TableRead tableRead, int length, int lengthEntryOctets, int lengthHeaderOctets) {
        int numberOfOctets = lengthEntryOctets * length + lengthHeaderOctets;
        int timeToRead = numberOfOctets / OCTESTS + 1;
        int numberOfOctetsRead = 0;
        byte[] bytesRead = new byte[numberOfOctets];

        for (int i = 0 ; i < timeToRead; i++) {
            if (numberOfOctetsRead + OCTESTS < numberOfOctets) {
                try {
                    byte[] buffer = ignoreFirst2Bytes(tableRead.getBuffer(numberOfOctetsRead, OCTESTS));
                    bytesRead = constructFullBuffer(bytesRead, buffer, numberOfOctetsRead, numberOfOctetsRead + OCTESTS);
                    numberOfOctetsRead = numberOfOctetsRead + OCTESTS;
                } catch (IOException e) {
                    sLogger.severe("Error trying to read the next bytes from table buffer");
                }
            } else {
                int remainOctets = numberOfOctets - numberOfOctetsRead;
                try {
                    byte[] buffer = ignoreFirst2Bytes(tableRead.getBuffer(numberOfOctetsRead, remainOctets));
                    bytesRead = constructFullBuffer(bytesRead, buffer, numberOfOctetsRead, numberOfOctetsRead + remainOctets);
                } catch (IOException e) {
                    sLogger.severe("Error trying to read the next bytes from table buffer");
                }
            }
        }
        return bytesRead;
    }

    private byte[] constructFullBuffer(byte[] bytesRead, byte[] buffer, int oldNumberOfOctetsRead, int newNumberOfOctetsRead) {
        for (int i = oldNumberOfOctetsRead, j = 0; i < newNumberOfOctetsRead; i++) {
            bytesRead[i] = buffer[j];
            j++;
        }
        return bytesRead;
    }

    /**
     * Ignore the first 2 bytes of the response because are not necessary
     */
    private byte[] ignoreFirst2Bytes(byte[] buffer) {
        byte[] correctBuffer = new byte[buffer.length - 2];
        for (int i = 0; i < buffer.length; i++) {
            if (i != 0 && i != 1) {
                correctBuffer[i - 2] = buffer[i];
            }
        }
        return correctBuffer;
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

    private boolean isSupportedDLMS(LogBookReader logBookReader) {
        for (ObisCode supportedLogBook : supportedLogBooks) {
            if (supportedLogBook.equalsIgnoreBChannel(logBookReader.getLogBookObisCode())) {
                return true;
            }
        }
        return false;
    }

    private boolean isSupportedANSI(LogBookReader logBookReader) {
        for (ObisCode supportedLogBook : supportedAnsiLogBooks) {
            if (supportedLogBook.equalsIgnoreBChannel(logBookReader.getLogBookObisCode())) {
                return true;
            }
        }
        return false;
    }

    private Map<ObisCode, ObisCode> createMap() {
        Map<ObisCode, ObisCode> map = new HashMap<>();
        map.put(PQM_LOG, ObisCode.fromString("0.0.65.16.49.255"));
        map.put(SAG_LOG, ObisCode.fromString("0.0.65.16.53.255"));
        return map;
    }
}