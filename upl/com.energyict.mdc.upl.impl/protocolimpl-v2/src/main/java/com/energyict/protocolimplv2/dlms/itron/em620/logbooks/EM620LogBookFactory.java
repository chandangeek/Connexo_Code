package com.energyict.protocolimplv2.dlms.itron.em620.logbooks;

import com.energyict.mdc.upl.NotInObjectListException;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.tasks.support.DeviceLogBookSupport;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MeterProtocolEvent;
import com.energyict.protocolimpl.utils.ProtocolUtils;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;

import java.io.IOException;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TreeSet;

public class EM620LogBookFactory implements DeviceLogBookSupport {

    private final static ObisCode STANDARD_EVENT_LOG = ObisCode.fromString("0.0.99.98.0.255");

    private AbstractSet<ObisCode> supportedLogBooks = new TreeSet<>();
    private AbstractDlmsProtocol protocol;
    private CollectedDataFactory collectedDataFactory;
    private IssueFactory issueFactory;

    public EM620LogBookFactory(AbstractDlmsProtocol protocol) {
        this.protocol               = protocol;
        this.collectedDataFactory   = protocol.getCollectedDataFactory();
        this.issueFactory           = protocol.getIssueFactory();

        supportedLogBooks.add(STANDARD_EVENT_LOG);
    }

    protected List<MeterProtocolEvent> parseEvents(DataContainer dataContainer, ObisCode logBookObisCode) {
        List<MeterEvent> meterEvents;
        if (logBookObisCode.equals(STANDARD_EVENT_LOG)) {
            meterEvents = new EM620StandardEventLog(protocol.getTimeZone(), dataContainer).getMeterEvents();
        } else {
            return new ArrayList<>();
        }
        return MeterEvent.mapMeterEventsToMeterProtocolEvents(meterEvents);
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        List<CollectedLogBook> result = new ArrayList<>();
        for (LogBookReader logBookReader : logBooks) {
            CollectedLogBook collectedLogBook = this.collectedDataFactory.createCollectedLogBook(logBookReader.getLogBookIdentifier());
            if (isSupported(logBookReader)) {
                ProfileGeneric profileGeneric = getProfileGeneric(logBookReader, collectedLogBook);
                if (profileGeneric != null) {
                    Calendar fromDate = getCalendar();
                    fromDate.setTime(logBookReader.getLastLogBook());
                    getEvents(logBookReader, collectedLogBook, fromDate, profileGeneric);
                }
            } else {
                collectedLogBook.setFailureInformation(
                        ResultType.NotSupported,
                        issueFactory.createWarning(
                                logBookReader,
                                String.format("Logbook with OBIS code %s is not supported by the protocol", logBookReader.getLogBookObisCode().toString()),
                                logBookReader.getLogBookObisCode().toString()));
            }
            result.add(collectedLogBook);
        }
        return result;
    }

    private boolean isSupported(LogBookReader logBookReader) {
        for (ObisCode supportedLogBook : supportedLogBooks) {
            if (supportedLogBook.equals(logBookReader.getLogBookObisCode())) {
                return true;
            }
        }
        return false;
    }

    private Calendar getCalendar() {
        return ProtocolUtils.getCalendar(protocol.getTimeZone());
    }

    private ProfileGeneric getProfileGeneric(LogBookReader logBookReader, CollectedLogBook collectedLogBook) {
        try {
            return protocol.getDlmsSession().getCosemObjectFactory().getProfileGeneric(
                            protocol.getPhysicalAddressCorrectedObisCode(logBookReader.getLogBookObisCode(),
                            logBookReader.getMeterSerialNumber()));
        } catch (NotInObjectListException e) {
            collectedLogBook.setFailureInformation(
                    ResultType.InCompatible,
                    issueFactory.createWarning(
                            logBookReader,
                            String.format("Logbook with OBIS code %s was not found it meter object list", logBookReader.getLogBookObisCode().toString()),
                            logBookReader.getLogBookObisCode().toString(),
                            e.getMessage()));
        }
        return null;
    }

    private void getEvents(LogBookReader logBookReader, CollectedLogBook collectedLogBook, Calendar fromDate, ProfileGeneric profileGeneric) {
        try {
            DataContainer dataContainer = profileGeneric.getBuffer(fromDate, getCalendar());
            collectedLogBook.setCollectedMeterEvents(parseEvents(dataContainer, logBookReader.getLogBookObisCode()));
        } catch (NotInObjectListException e) {
            collectedLogBook.setFailureInformation(
                    ResultType.InCompatible,
                    issueFactory.createWarning(
                            logBookReader,
                            String.format("Logbook with OBIS code %s was not found it meter object list", logBookReader.getLogBookObisCode().toString()),
                            logBookReader.getLogBookObisCode().toString(),
                            e.getMessage()));
        } catch (IOException e) {
            if (DLMSIOExceptionHandler.isUnexpectedResponse(e, protocol.getDlmsSession().getProperties().getRetries() + 1)) {
                collectedLogBook.setFailureInformation(
                        ResultType.NotSupported,
                        issueFactory.createWarning(
                                logBookReader,
                                String.format("IOException while reading logbook with OBIS code %s ",
                                        logBookReader.getLogBookObisCode().toString()) + e.getMessage()));
            }
        }
    }
}
