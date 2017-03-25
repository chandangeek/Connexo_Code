package com.energyict.protocolimplv2.eict.rtu3.beacon3100.logbooks;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.mdc.meterdata.CollectedLogBook;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdc.protocol.tasks.support.DeviceLogBookSupport;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimplv2.MdcManager;
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

    private AbstractDlmsProtocol protocol;
    private List<ObisCode> supportedLogBooks;


    public Beacon3100LogBookFactory(AbstractDlmsProtocol protocol) {
        this.protocol = protocol;
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
            CollectedLogBook collectedLogBook = MdcManager.getCollectedDataFactory().createCollectedLogBook(logBookReader.getLogBookIdentifier());
            if (isSupported(logBookReader)) {
                ProfileGeneric profileGeneric = null;
                try {
                    profileGeneric = protocol.getDlmsSession().getCosemObjectFactory().getProfileGeneric(logBookReader.getLogBookObisCode());
                } catch (NotInObjectListException e) {
                    collectedLogBook.setFailureInformation(ResultType.InCompatible, MdcManager.getIssueFactory().createWarning(logBookReader, "logBookXissue", logBookReader.getLogBookObisCode().toString(), e.getMessage()));
                }

                if (profileGeneric != null) {
                    Calendar fromDate = getCalendar();
                    fromDate.setTime(logBookReader.getLastLogBook());

                    try {
                        DataContainer dataContainer = profileGeneric.getBuffer(fromDate, getCalendar());
                        collectedLogBook.setCollectedMeterEvents(parseEvents(dataContainer, logBookReader.getLogBookObisCode()));
                        if (PROTOCOL_LOGBOOK.equals(logBookReader.getLogBookObisCode())){
                            Beacon3100ProtocolEventLog protocolEventLog = new Beacon3100ProtocolEventLog(dataContainer, protocol.getTimeZone());
                            result.addAll(protocolEventLog.geSlaveLogBooks());
                        }
                    } catch (IOException e) {
                        if (DLMSIOExceptionHandler.isUnexpectedResponse(e, protocol.getDlmsSessionProperties().getRetries())) {
                            collectedLogBook.setFailureInformation(ResultType.InCompatible, MdcManager.getIssueFactory().createWarning(logBookReader, "logBookXissue", logBookReader.getLogBookObisCode().toString(), e.getMessage()));
                        }
                    }
                }
            } else {
                collectedLogBook.setFailureInformation(ResultType.NotSupported, MdcManager.getIssueFactory().createWarning(logBookReader, "logBookXnotsupported", logBookReader.getLogBookObisCode().toString()));
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