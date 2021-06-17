package com.energyict.protocolimplv2.dlms.a2.events;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.cosem.DataAccessResultException;
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
import com.energyict.protocolimplv2.dlms.a2.A2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class A2LogBookFactory implements DeviceLogBookSupport {

    protected static final ObisCode STANDARD_EVENT_LOG = ObisCode.fromString("7.0.99.98.0.255");
    protected static final ObisCode METROLOGICAL_LOG = ObisCode.fromString("7.0.99.98.1.255");
    protected static final ObisCode PARAMETER_MONITOR_LOG = ObisCode.fromString("7.0.99.16.0.255");

    private A2 protocol;
    private final CollectedDataFactory collectedDataFactory;
    private final IssueFactory issueFactory;
    /**
     * List of obiscodes of the supported log books
     */
    protected final List<ObisCode> supportedLogBooks;

    public A2LogBookFactory(A2 protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        this.protocol = protocol;
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;
        supportedLogBooks = new ArrayList<>();
        supportedLogBooks.add(STANDARD_EVENT_LOG);
        supportedLogBooks.add(METROLOGICAL_LOG);
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        List<CollectedLogBook> result = new ArrayList<>();
        for (LogBookReader logBookReader : logBooks) {
            CollectedLogBook collectedLogBook = collectedDataFactory.createCollectedLogBook(logBookReader.getLogBookIdentifier());
            if (isSupported(logBookReader)) {
                ProfileGeneric profileGeneric = null;
                try {
                    profileGeneric = protocol.getDlmsSession().getCosemObjectFactory().getProfileGeneric(protocol.getPhysicalAddressCorrectedObisCode(logBookReader.getLogBookObisCode(), logBookReader.getMeterSerialNumber()));
                } catch (NotInObjectListException e) {
                    collectedLogBook.setFailureInformation(ResultType.InCompatible, issueFactory.createWarning(logBookReader, "logBookXissue", logBookReader.getLogBookObisCode().toString(), e.getMessage()));
                }
                if (profileGeneric != null) {
                    Calendar fromDate = getCalendar();
                    fromDate.setTime(logBookReader.getLastLogBook());
                    try {
                        DataContainer buffer = profileGeneric.getBuffer(fromDate, getCalendar());
                        collectedLogBook.setCollectedMeterEvents(parseEvents(buffer, logBookReader.getLogBookObisCode()));
                    } catch (NotInObjectListException e) {
                        collectedLogBook.setFailureInformation(ResultType.InCompatible, issueFactory.createWarning(logBookReader, "logBookXissue", logBookReader.getLogBookObisCode().toString(), e.getMessage()));
                    } catch (DataAccessResultException e) {
                        // this can happen when the load profile is read twice in the same time window (day for daily lp), than the data block is not accessible. It could also happen when the load profile is not configured properly.
                        if (DLMSIOExceptionHandler.isUnexpectedResponse(e, protocol.getDlmsSessionProperties().getRetries() + 1)) {
                            String message = "Logbook " + logBookReader.getLogBookObisCode() + " was probably already read today, try modifying the 'last reading' date in the logbook properties. " + e.getMessage();
                            Issue problem = issueFactory.createWarning(logBookReader, message, logBookReader.getLogBookObisCode().toString());
                            collectedLogBook.setFailureInformation(ResultType.DataIncomplete, problem);
                        }
                    } catch (IOException e) {
                        if (DLMSIOExceptionHandler.isUnexpectedResponse(e, protocol.getDlmsSession().getProperties().getRetries() + 1)) {
                            collectedLogBook.setFailureInformation(ResultType.Other, issueFactory.createWarning(logBookReader, "logBookXissue", logBookReader.getLogBookObisCode().toString(), e.getMessage()));
                        } else {
                            collectedLogBook.setFailureInformation(ResultType.Other, issueFactory.createWarning(logBookReader, "unexpectedError", logBookReader.getLogBookObisCode().toString(), e.getMessage()));
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

    protected List<MeterProtocolEvent> parseEvents(DataContainer dataContainer, ObisCode logBookObisCode) {
        List<MeterEvent> meterEvents;
        if (logBookObisCode.equals(METROLOGICAL_LOG)) {
            meterEvents = new A2MetrologicalEventLog(protocol.getTimeZone(), dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(STANDARD_EVENT_LOG)) {
            meterEvents = new A2StandardEventLog(protocol.getTimeZone(), dataContainer).getMeterEvents();
        } else {
            return new ArrayList<>();
        }
        return MeterEvent.mapMeterEventsToMeterProtocolEvents(meterEvents);
    }

    private Calendar getCalendar() {
        return ProtocolUtils.getCalendar(protocol.getTimeZone());
    }

    private boolean isSupported(LogBookReader logBookReader) {
        for (ObisCode supportedLogBook : supportedLogBooks) {
            if (supportedLogBook.equalsIgnoreBChannel(logBookReader.getLogBookObisCode())) {
                return true;
            }
        }
        return false;
    }

    public A2 getProtocol() {
        return protocol;
    }
}
