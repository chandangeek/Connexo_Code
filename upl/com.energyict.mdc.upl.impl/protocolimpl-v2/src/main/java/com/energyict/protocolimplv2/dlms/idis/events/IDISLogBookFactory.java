package com.energyict.protocolimplv2.dlms.idis.events;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.mdc.meterdata.CollectedLogBook;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdc.protocol.tasks.support.DeviceLogBookSupport;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.dlms.idis.events.*;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.nta.IOExceptionHandler;
import com.energyict.protocolimplv2.nta.abstractnta.AbstractDlmsProtocol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 6/01/2015 - 9:42
 */
public class IDISLogBookFactory implements DeviceLogBookSupport {

    private AbstractDlmsProtocol protocol;

    private static ObisCode DISCONNECTOR_CONTROL_LOG = ObisCode.fromString("0.0.99.98.2.255");
    private static ObisCode STANDARD_EVENT_LOG = ObisCode.fromString("0.0.99.98.0.255");
    private static ObisCode FRAUD_DETECTION_LOG = ObisCode.fromString("0.0.99.98.1.255");
    private static ObisCode POWER_FAILURE_EVENT_LOG = ObisCode.fromString("1.0.99.97.0.255");
    private static ObisCode POWER_QUALITY_LOG = ObisCode.fromString("0.0.99.98.4.255");
    private static ObisCode MBUS_EVENT_LOG = ObisCode.fromString("0.0.99.98.3.255");    //General MBus log, describing events for all slave meters

    /**
     * List of obiscodes of the supported log books
     */
    private final List<ObisCode> supportedLogBooks;

    public IDISLogBookFactory(AbstractDlmsProtocol protocol) {
        this.protocol = protocol;
        supportedLogBooks = new ArrayList<>();
        supportedLogBooks.add(DISCONNECTOR_CONTROL_LOG);
        supportedLogBooks.add(STANDARD_EVENT_LOG);
        supportedLogBooks.add(FRAUD_DETECTION_LOG);
        supportedLogBooks.add(POWER_FAILURE_EVENT_LOG);
        supportedLogBooks.add(POWER_QUALITY_LOG);
        supportedLogBooks.add(MBUS_EVENT_LOG);
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        List<CollectedLogBook> result = new ArrayList<>();
        for (LogBookReader logBookReader : logBooks) {
            CollectedLogBook collectedLogBook = MdcManager.getCollectedDataFactory().createCollectedLogBook(logBookReader.getLogBookIdentifier());
            if (isSupported(logBookReader)) {
                ProfileGeneric profileGeneric;
                try {
                    profileGeneric = protocol.getDlmsSession().getCosemObjectFactory().getProfileGeneric(logBookReader.getLogBookObisCode());
                } catch (ProtocolException e) {
                    throw MdcManager.getComServerExceptionFactory().createUnExpectedProtocolError(e);
                }
                Calendar fromDate = getCalendar();
                fromDate.setTime(logBookReader.getLastLogBook());
                DataContainer dataContainer;
                try {
                    dataContainer = profileGeneric.getBuffer(fromDate, getCalendar());
                    collectedLogBook.setCollectedMeterEvents(parseEvents(dataContainer, logBookReader.getLogBookObisCode()));
                } catch (IOException e) {
                    if (IOExceptionHandler.isUnexpectedResponse(e, protocol.getDlmsSession())) {
                        collectedLogBook.setFailureInformation(ResultType.NotSupported, MdcManager.getIssueCollector().addWarning(logBookReader, "logBookXnotsupported", logBookReader.getLogBookObisCode().toString()));
                    }
                }
            } else {
                collectedLogBook.setFailureInformation(ResultType.NotSupported, MdcManager.getIssueCollector().addWarning(logBookReader, "logBookXnotsupported", logBookReader.getLogBookObisCode().toString()));
            }
            result.add(collectedLogBook);
        }
        return result;
    }

    private List<MeterProtocolEvent> parseEvents(DataContainer dataContainer, ObisCode logBookObisCode) {
        List<MeterEvent> meterEvents;
        if (logBookObisCode.equals(POWER_QUALITY_LOG)) {
            meterEvents = new PowerQualityEventLog(protocol.getTimeZone(), dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(POWER_FAILURE_EVENT_LOG)) {
            meterEvents = new PowerFailureEventLog(protocol.getTimeZone(), dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(DISCONNECTOR_CONTROL_LOG)) {
            meterEvents = new DisconnectorControlLog(protocol.getTimeZone(), dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(FRAUD_DETECTION_LOG)) {
            meterEvents = new FraudDetectionLog(protocol.getTimeZone(), dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(STANDARD_EVENT_LOG)) {
            meterEvents = new StandardEventLog(protocol.getTimeZone(), dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(MBUS_EVENT_LOG)) {
            meterEvents = new MBusEventLog(protocol.getTimeZone(), dataContainer).getMeterEvents();
        } else {
            return new ArrayList<>();
        }
        return MeterEvent.mapMeterEventsToMeterProtocolEvents(meterEvents);
    }

    private boolean isSupported(LogBookReader logBookReader) {
        return supportedLogBooks.contains(logBookReader.getLogBookObisCode());
    }

    private Calendar getCalendar() {
        return ProtocolUtils.getCalendar(protocol.getTimeZone());
    }

}