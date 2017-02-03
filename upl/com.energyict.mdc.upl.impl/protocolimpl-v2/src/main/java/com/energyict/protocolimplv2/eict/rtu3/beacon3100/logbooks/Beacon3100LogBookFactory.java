package com.energyict.protocolimplv2.eict.rtu3.beacon3100.logbooks;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.cosem.ProfileGeneric;
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
import com.energyict.protocol.MeterProtocolEvent;
import com.energyict.protocolimpl.utils.ProtocolUtils;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Copyrights EnergyICT
 */
public class Beacon3100LogBookFactory implements DeviceLogBookSupport {

    public static final ObisCode MAIN_LOGBOOK           = ObisCode.fromString("0.0.99.98.1.255");
    public static final ObisCode SECURITY_LOGBOOK       = ObisCode.fromString("0.0.99.98.2.255");
    public static final ObisCode COVER_LOGBOOK          = ObisCode.fromString("0.0.99.98.3.255");
    public static final ObisCode COMMUNICATION_LOGBOOK  = ObisCode.fromString("0.0.99.98.4.255");
    public static final ObisCode VOLTAGE_LOGBOOK        = ObisCode.fromString("0.0.99.98.5.255");
    public static final ObisCode PROTOCOL_LOGBOOK       = ObisCode.fromString("0.128.99.98.0.255");

    private final AbstractDlmsProtocol protocol;
    private final List<ObisCode> supportedLogBooks;
    private final CollectedDataFactory collectedDataFactory;
    private final IssueFactory issueFactory;

    public Beacon3100LogBookFactory(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        this.protocol = protocol;
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;
        supportedLogBooks = new ArrayList<>();
        supportedLogBooks.add(MAIN_LOGBOOK);
        supportedLogBooks.add(SECURITY_LOGBOOK);
        supportedLogBooks.add(COVER_LOGBOOK);
        supportedLogBooks.add(COMMUNICATION_LOGBOOK);
        supportedLogBooks.add(VOLTAGE_LOGBOOK);
        supportedLogBooks.add(PROTOCOL_LOGBOOK);
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBookReaders) {
        List<CollectedLogBook> result = new ArrayList<>();
        for (LogBookReader logBookReader : logBookReaders) {
            CollectedLogBook collectedLogBook = this.collectedDataFactory.createCollectedLogBook(logBookReader.getLogBookIdentifier());
            if (isSupported(logBookReader)) {
                ProfileGeneric profileGeneric = null;
                try {
                    profileGeneric = protocol.getDlmsSession().getCosemObjectFactory().getProfileGeneric(logBookReader.getLogBookObisCode());
                } catch (NotInObjectListException e) {
                    collectedLogBook.setFailureInformation(ResultType.InCompatible, this.issueFactory.createWarning(logBookReader, "logBookXissue", logBookReader.getLogBookObisCode().toString(), e.getMessage()));
                }

                if (profileGeneric != null) {
                    Calendar fromDate = getCalendar();
                    fromDate.setTime(logBookReader.getLastLogBook());

                    try {
                        /*DataContainer dataContainer = profileGeneric.getBuffer(fromDate, getCalendar());*/
                        DataContainer dataContainer = profileGeneric.getBuffer();
                        collectedLogBook.setCollectedMeterEvents(parseEvents(dataContainer, logBookReader.getLogBookObisCode()));
                    } catch (IOException e) {
                        if (DLMSIOExceptionHandler.isUnexpectedResponse(e, protocol.getDlmsSessionProperties().getRetries())) {
                            collectedLogBook.setFailureInformation(ResultType.InCompatible, this.issueFactory.createWarning(logBookReader, "logBookXissue", logBookReader.getLogBookObisCode().toString(), e.getMessage()));
                        }
                    }
                }
            } else {
                collectedLogBook.setFailureInformation(ResultType.NotSupported, this.issueFactory.createWarning(logBookReader, "logBookXnotsupported", logBookReader.getLogBookObisCode().toString()));
            }
            result.add(collectedLogBook);
        }
        return result;

    }

    private List<MeterProtocolEvent> parseEvents(DataContainer dataContainer, ObisCode logBookObisCode) throws IOException {
        List<MeterEvent> meterEvents;
        if (logBookObisCode.equals(MAIN_LOGBOOK)) {
            meterEvents = new Beacon3100StandardEventLog(dataContainer, protocol.getTimeZone()).getMeterEvents();
        } else if (logBookObisCode.equals(SECURITY_LOGBOOK)) {
            meterEvents = new Beacon3100SecurityEventLog(dataContainer, protocol.getTimeZone()).getMeterEvents();
        } else if (logBookObisCode.equals(COVER_LOGBOOK)) {
            meterEvents = new Beacon3100CoverEventLog(dataContainer, protocol.getTimeZone()).getMeterEvents();
        } else if (logBookObisCode.equals(COMMUNICATION_LOGBOOK)) {
            meterEvents = new Beacon3100CommunicationEventLog(dataContainer, protocol.getTimeZone()).getMeterEvents();
        } else if (logBookObisCode.equals(VOLTAGE_LOGBOOK)) {
            meterEvents = new Beacon3100VoltageEventLog(dataContainer, protocol.getTimeZone()).getMeterEvents();
        } else if (logBookObisCode.equals(PROTOCOL_LOGBOOK)) {
            meterEvents = new Beacon3100ProtocolEventLog(dataContainer, protocol.getTimeZone()).getMeterEvents();
        } else {
            return new ArrayList<>();
        }
        return MeterEvent.mapMeterEventsToMeterProtocolEvents(meterEvents);
    }

    private boolean isSupported(LogBookReader logBookReader) {
        for (ObisCode supportedLogBookObisCode : supportedLogBooks) {
            if (supportedLogBookObisCode.equals(logBookReader.getLogBookObisCode())) {
                return true;
            }
        }
        return false;
    }

    private Calendar getCalendar() {
        return ProtocolUtils.getCalendar(protocol.getTimeZone());
    }
}