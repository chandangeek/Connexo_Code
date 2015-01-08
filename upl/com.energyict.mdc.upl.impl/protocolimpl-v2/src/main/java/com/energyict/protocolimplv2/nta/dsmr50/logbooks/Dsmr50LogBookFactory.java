package com.energyict.protocolimplv2.nta.dsmr50.logbooks;

import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.mdc.meterdata.CollectedLogBook;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdc.protocol.tasks.support.DeviceLogBookSupport;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MeterProtocolEvent;
import com.energyict.protocol.ProtocolException;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.nta.IOExceptionHandler;
import com.energyict.protocolimplv2.nta.abstractnta.AbstractDlmsProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.eventhandling.DisconnectControlLog;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.eventhandling.EventsLog;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.eventhandling.FraudDetectionLog;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.eventhandling.PowerFailureLog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Dsmr50LogBookFactory implements DeviceLogBookSupport {

    private AbstractDlmsProtocol protocol;

    /**
     * List of obiscodes of the supported log books
     */
    private List<ObisCode> supportedLogBooks;

    public Dsmr50LogBookFactory(AbstractDlmsProtocol protocol) {
        this.protocol = protocol;
        supportedLogBooks = new ArrayList<>();
        try {
            supportedLogBooks.add(getMeterConfig().getEventLogObject().getObisCode());
            supportedLogBooks.add(getMeterConfig().getControlLogObject().getObisCode());
            supportedLogBooks.add(getMeterConfig().getPowerFailureLogObject().getObisCode());
            supportedLogBooks.add(getMeterConfig().getFraudDetectionLogObject().getObisCode());
        } catch (ProtocolException e) {   //Object not found in IOL, should never happen
            throw MdcManager.getComServerExceptionFactory().createUnExpectedProtocolError(e);
        }
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        List<CollectedLogBook> result = new ArrayList<>();
        for (LogBookReader logBookReader : logBooks) {
            CollectedLogBook collectedLogBook = MdcManager.getCollectedDataFactory().createCollectedLogBook(logBookReader.getLogBookIdentifier());
            if (isSupported(logBookReader)) {
                ProfileGeneric profileGeneric;
                try {
                    profileGeneric = protocol.getDlmsSession().getCosemObjectFactory().getProfileGeneric(protocol.getPhysicalAddressCorrectedObisCode(logBookReader.getLogBookObisCode(), logBookReader.getMeterSerialNumber()));
                } catch (IOException e) {
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
        try {
            if (logBookObisCode.equals(getMeterConfig().getEventLogObject().getObisCode())) {
                meterEvents = new EventsLog(dataContainer).getMeterEvents();
            } else if (logBookObisCode.equals(getMeterConfig().getControlLogObject().getObisCode())) {
                meterEvents = new DisconnectControlLog(dataContainer).getMeterEvents();
            } else if (logBookObisCode.equals(getMeterConfig().getPowerFailureLogObject().getObisCode())) {
                meterEvents = new PowerFailureLog(dataContainer).getMeterEvents();
            } else if (logBookObisCode.equals(getMeterConfig().getFraudDetectionLogObject().getObisCode())) {
                meterEvents = new FraudDetectionLog(dataContainer).getMeterEvents();
            } else{
                return new ArrayList<>();
            }
        } catch (ProtocolException e) {
            throw MdcManager.getComServerExceptionFactory().createUnExpectedProtocolError(e);
        }
        return MeterEvent.mapMeterEventsToMeterProtocolEvents(meterEvents);
    }

    private boolean isSupported(LogBookReader logBookReader) {
        for (ObisCode supportedLogBookObisCode : supportedLogBooks) {
            if (supportedLogBookObisCode.equals(protocol.getPhysicalAddressCorrectedObisCode(logBookReader.getLogBookObisCode(), logBookReader.getMeterSerialNumber()))) {
                return true;
            }
        }
        return false;
    }

    private DLMSMeterConfig getMeterConfig() {
        return this.protocol.getDlmsSession().getMeterConfig();
    }

    private Calendar getCalendar() {
        return ProtocolUtils.getCalendar(protocol.getTimeZone());
    }
}