package com.energyict.protocolimplv2.dlms.idis.hs3300.events;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.DataStructure;
import com.energyict.dlms.aso.SecurityContext;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.mdc.upl.NotInObjectListException;
import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.tasks.support.DeviceLogBookSupport;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MeterProtocolEvent;
import com.energyict.protocolimpl.utils.ProtocolUtils;
import com.energyict.protocolimplv2.dlms.idis.hs3300.HS3300;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;

public class HS3300LogBookFactory implements DeviceLogBookSupport {

    private static ObisCode STANDARD_EVENT_LOG        = ObisCode.fromString("0.0.99.98.0.255");
    private static ObisCode FRAUD_DETECTION_LOG       = ObisCode.fromString("0.0.99.98.1.255");
    private static ObisCode DISCONNECT_CONTROL_LOG    = ObisCode.fromString("0.0.99.98.2.255");
    private static ObisCode POWER_QUALITY_LOG         = ObisCode.fromString("0.0.99.98.4.255");
    private static ObisCode COMMUNICATION_EVENT_LOG   = ObisCode.fromString("0.0.99.98.5.255");
    private static ObisCode COMMUNICATION_SESSION_LOG = ObisCode.fromString("0.0.99.98.6.255");
    private static ObisCode SECURITY_EVENT_LOG        = ObisCode.fromString("0.0.99.98.9.255");

    private final HS3300 protocol;
    private final CollectedDataFactory collectedDataFactory;
    private final IssueFactory issueFactory;

    protected final List<ObisCode> supportedLogBooks;

    public HS3300LogBookFactory(HS3300 protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        this.protocol = protocol;
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;

        supportedLogBooks = new ArrayList<>();
        supportedLogBooks.add(STANDARD_EVENT_LOG);
        supportedLogBooks.add(FRAUD_DETECTION_LOG);
        supportedLogBooks.add(DISCONNECT_CONTROL_LOG);
        supportedLogBooks.add(POWER_QUALITY_LOG);
        supportedLogBooks.add(COMMUNICATION_EVENT_LOG);
        supportedLogBooks.add(COMMUNICATION_SESSION_LOG);
        supportedLogBooks.add(SECURITY_EVENT_LOG);
    }

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
                    Calendar fromDate = getCalendar();
                    fromDate.setTime(logBookReader.getLastLogBook());

                    try {
                        DataContainer dataContainer = profileGeneric.getBuffer(fromDate, getCalendar());
                        List<MeterProtocolEvent> meterProtocolEvents = parseEventsToMeterProtocolEvents(dataContainer, logBookReader.getLogBookObisCode())
                                .stream().map(
                                    item -> {
                                        item.getEventType().setType(protocol.getTypeMeter());
                                        return item;
                                    }
                            ).collect(Collectors.toList());

                        collectedLogBook.setCollectedMeterEvents(meterProtocolEvents);
                    } catch (IOException e) {
                        if (DLMSIOExceptionHandler.isUnexpectedResponse(e, protocol.getDlmsSessionProperties().getRetries())) {
                            collectedLogBook.setFailureInformation(ResultType.InCompatible, issueFactory.createWarning(logBookReader, "logBookXissue", logBookReader.getLogBookObisCode().toString(), e.getMessage()));
                        }
                    }
                }
            } else {
                final Issue warning = issueFactory.createWarning(logBookReader, "Logbook " + logBookReader.getLogBookObisCode() + " is not supported.", logBookReader.getLogBookObisCode().toString());
                collectedLogBook.setFailureInformation(ResultType.NotSupported, warning);
            }
            result.add(collectedLogBook);
        }
        return result;
    }

    private List<MeterProtocolEvent> parseEventsToMeterProtocolEvents(DataContainer dataContainer, ObisCode logBookObisCode) {
        List<MeterProtocolEvent> meterProtocolEvents = new ArrayList<>();
        TimeZone timeZone = protocol.getTimeZone();
        int numberOfEvents = dataContainer.getRoot().getNrOfElements();

        for (int i = 0; i < numberOfEvents; i++) {
            DataStructure dataStructure = dataContainer.getRoot().getStructure(i);
            // Event timestamp is at index 0
            Date eventTimeStamp = dataStructure.getOctetString(0).toDate(timeZone);
            // EventID is at index 1
            int eventID = (int) dataStructure.getValue(1) & 0xFF; // To prevent negative values

            MeterProtocolEvent meterProtocolEvent = getMeterProtocolEvent(eventTimeStamp, eventID, logBookObisCode);

            if (meterProtocolEvent != null) {
                meterProtocolEvents.add(meterProtocolEvent);
            }
        }

        if (logBookObisCode.equals(STANDARD_EVENT_LOG)) {
            meterProtocolEvents.addAll(MeterEvent.mapMeterEventsToMeterProtocolEvents(getFrameCounterEvents()));
        }

        return meterProtocolEvents;
    }

    private static MeterProtocolEvent getMeterProtocolEvent(Date timeStamp, int eventID, ObisCode logBookObisCode) {
        MeterProtocolEvent meterProtocolEvent = null;
        if (logBookObisCode.equals(STANDARD_EVENT_LOG)) {
            meterProtocolEvent = StandardEventLog.buildMeterEvent(timeStamp, eventID);
        }
        else if (logBookObisCode.equals(FRAUD_DETECTION_LOG)) {
            meterProtocolEvent = FraudDetectionEventLog.buildMeterEvent(timeStamp, eventID);
        }
        else if (logBookObisCode.equals(DISCONNECT_CONTROL_LOG)) {
            meterProtocolEvent = DisconnectorControlEventLog.buildMeterEvent(timeStamp, eventID);
        }
        else if (logBookObisCode.equals(POWER_QUALITY_LOG)) {
            meterProtocolEvent = PowerQualityEventLog.buildMeterEvent(timeStamp, eventID);
        }
        else if (logBookObisCode.equals(COMMUNICATION_EVENT_LOG)) {
            meterProtocolEvent = CommunicationEventLog.buildMeterEvent(timeStamp, eventID);
        }
        else if (logBookObisCode.equals(COMMUNICATION_SESSION_LOG)) {
            meterProtocolEvent = CommunicationSessionEventLog.buildMeterEvent(timeStamp, eventID);
        }
        else if (logBookObisCode.equals(SECURITY_EVENT_LOG)) {
            meterProtocolEvent = SecurityEventLog.buildMeterEvent(timeStamp, eventID);
        }
        return meterProtocolEvent;
    }

    protected List<MeterEvent> getFrameCounterEvents() {
        final SecurityContext securityContext = protocol.getDlmsSession().getAso().getSecurityContext();
        List<MeterEvent> result = new ArrayList<>();

        final Optional<MeterEvent> frameCounterEvent = generateFrameCounterLimitEvent(securityContext.getFrameCounter(),
                "Frame Counter", 900, MeterEvent.SEND_FRAME_COUNTER_ABOVE_THRESHOLD);
        frameCounterEvent.ifPresent(result::add);

        if (securityContext.getResponseFrameCounter() != 0) {
            final Optional<MeterEvent> responseFrameCounterEvent = generateFrameCounterLimitEvent(securityContext.getResponseFrameCounter(),
                    "Response Frame Counter", 901, MeterEvent.RECEIVE_FRAME_COUNTER_ABOVE_THRESHOLD);
            responseFrameCounterEvent.ifPresent(result::add);
        } else {
            protocol.journal("Response frame counter not initialized.");
        }

        return result;
    }

    private Optional<MeterEvent> generateFrameCounterLimitEvent(long frameCounter, String name, int eventId, int eiCode) {
        try {
            long frameCounterLimit = protocol.getDlmsSessionProperties().getFrameCounterLimit();

            if (frameCounterLimit == 0) {
                protocol.journal("Frame counter threshold not configured. FYI the current " + name + " is " + frameCounter);
            } else if (frameCounter > frameCounterLimit) {
                protocol.journal(name + ": " + frameCounter + " is above the threshold (" + frameCounterLimit + ") - will create an event");
                MeterEvent frameCounterEvent = new MeterEvent(new Date(), eiCode, eventId, name + " above threshold: " + frameCounter);
                return Optional.of(frameCounterEvent);
            } else {
                protocol.journal(name + " below configured threshold: " + frameCounter + " < " + frameCounterLimit);
            }
        } catch (Exception e) {
            protocol.journal("Error getting  " + name + e.getMessage());
        }
        return Optional.empty();
    }

    private boolean isSupported(LogBookReader logBookReader) {
        return supportedLogBooks.stream().anyMatch(e -> e.equalsIgnoreBChannel(logBookReader.getLogBookObisCode()));
    }

    private Calendar getCalendar() {
        return ProtocolUtils.getCalendar(protocol.getTimeZone());
    }

}

