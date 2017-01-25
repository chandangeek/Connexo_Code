package com.energyict.protocolimplv2.eict.rtu3.beacon3100.logbooks;

import com.elster.jupiter.metering.MeteringService;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.LogBookReader;
import com.energyict.mdc.protocol.api.MessageSeeds;
import com.energyict.mdc.protocol.api.NotInObjectListException;
import com.energyict.mdc.protocol.api.ProtocolException;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedLogBook;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.mdc.protocol.api.device.events.MeterProtocolEvent;
import com.energyict.mdc.protocol.api.tasks.support.DeviceLogBookSupport;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
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

    private final CollectedDataFactory collectedDataFactory;
    private final IssueService issueService;
    private final MeteringService meteringService;

    private AbstractDlmsProtocol protocol;
    private List<ObisCode> supportedLogBooks;


    public Beacon3100LogBookFactory(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueService issueService, MeteringService meteringService) {
        this.protocol = protocol;
        this.issueService = issueService;
        this.meteringService = meteringService;
        supportedLogBooks = new ArrayList<>();
        supportedLogBooks.add(MAIN_LOGBOOK);
        supportedLogBooks.add(SECURITY_LOGBOOK);
        supportedLogBooks.add(COVER_LOGBOOK);
        supportedLogBooks.add(COMMUNICATION_LOGBOOK);
        supportedLogBooks.add(VOLTAGE_LOGBOOK);
        supportedLogBooks.add(PROTOCOL_LOGBOOK);
        this.collectedDataFactory = collectedDataFactory;

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
                    collectedLogBook.setFailureInformation(ResultType.InCompatible,
                            issueService.newWarning(logBookReader, MessageSeeds.LOGBOOK_NOT_SUPPORTED, logBookReader.getLogBookObisCode().toString(), e.getMessage()));
                }

                if (profileGeneric != null) {
                    Calendar fromDate = getCalendar();
                    fromDate.setTimeInMillis(logBookReader.getLastLogBook().toEpochMilli());

                    try {
                        /*DataContainer dataContainer = profileGeneric.getBuffer(fromDate, getCalendar());*/
                        DataContainer dataContainer = profileGeneric.getBuffer();
                        collectedLogBook.setMeterEvents(parseEvents(dataContainer, logBookReader.getLogBookObisCode()));
                    } catch (IOException e) {
                        if (DLMSIOExceptionHandler.isUnexpectedResponse(e, protocol.getDlmsProperties().getRetries())) {
                            collectedLogBook.setFailureInformation(ResultType.InCompatible, this.issueService.newWarning(logBookReader, MessageSeeds.LOGBOOK_NOT_SUPPORTED, logBookReader.getLogBookObisCode().toString(), e.getMessage()));
                        }
                    }
                }
            } else {
                collectedLogBook.setFailureInformation(ResultType.NotSupported, this.issueService.newWarning(logBookReader, MessageSeeds.LOGBOOK_NOT_SUPPORTED, logBookReader.getLogBookObisCode().toString()));
            }
            result.add(collectedLogBook);
        }
        return result;

    }

    private List<MeterProtocolEvent> parseEvents(DataContainer dataContainer, ObisCode logBookObisCode) throws ProtocolException,IOException {
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
        return MeterEvent.mapMeterEventsToMeterProtocolEvents(meterEvents, meteringService);
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