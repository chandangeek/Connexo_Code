package com.energyict.protocolimplv2.nta.dsmr23.logbooks;

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
import com.energyict.protocol.NotInObjectListException;
import com.energyict.protocol.ProtocolException;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.nta.IOExceptionHandler;
import com.energyict.protocolimplv2.nta.dsmr23.topology.MeterTopology;
import com.energyict.smartmeterprotocolimpl.common.topology.DeviceMapping;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.eventhandling.DisconnectControlLog;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.eventhandling.EventsLog;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.eventhandling.FraudDetectionLog;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.eventhandling.MbusControlLog;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.eventhandling.MbusLog;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.eventhandling.PowerFailureLog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Dsmr23LogBookFactory implements DeviceLogBookSupport {

    private static final ObisCode STANDARD_EVENT_LOG = ObisCode.fromString("0.0.99.98.0.255");
    private static final ObisCode CONTROL_LOG = ObisCode.fromString("0.0.99.98.2.255");
    private static final ObisCode POWER_FAILURE_LOG = ObisCode.fromString("1.0.99.97.0.255");
    private static final ObisCode FRAUD_DETECTION_LOG = ObisCode.fromString("0.0.99.98.1.255");
    private static final ObisCode MBUS_EVENT_LOG = ObisCode.fromString("0.0.99.98.3.255");
    private static final ObisCode MBUS_CONTROL_LOG = ObisCode.fromString("0.1.24.5.0.255");
    private AbstractDlmsProtocol protocol;

    /**
     * List of obiscodes of the supported log books
     */
    private List<ObisCode> supportedLogBooks;

    public Dsmr23LogBookFactory(AbstractDlmsProtocol protocol) {
        this.protocol = protocol;
        supportedLogBooks = new ArrayList<>();
        supportedLogBooks.add(STANDARD_EVENT_LOG);
        supportedLogBooks.add(CONTROL_LOG);
        supportedLogBooks.add(POWER_FAILURE_LOG);
        supportedLogBooks.add(FRAUD_DETECTION_LOG);
        addMbusLogBooksIfSupported(protocol);
    }

    private void addMbusLogBooksIfSupported(AbstractDlmsProtocol protocol) {
        supportedLogBooks.add(MBUS_EVENT_LOG);

        for (DeviceMapping mbusMeter : ((MeterTopology) protocol.getMeterTopology()).getMbusMeterMap()) {
            ObisCode correctedObisCode = ProtocolTools.setObisCodeField(MBUS_CONTROL_LOG, 1, (byte) mbusMeter.getPhysicalAddress());
            supportedLogBooks.add(correctedObisCode);
        }
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        List<CollectedLogBook> result = new ArrayList<>();
        for (LogBookReader logBookReader : logBooks) {
            CollectedLogBook collectedLogBook = MdcManager.getCollectedDataFactory().createCollectedLogBook(logBookReader.getLogBookIdentifier());
            if (isSupported(logBookReader)) {
                ProfileGeneric profileGeneric = null;
                try {
                    profileGeneric = protocol.getDlmsSession().getCosemObjectFactory().getProfileGeneric(protocol.getPhysicalAddressCorrectedObisCode(logBookReader.getLogBookObisCode(), logBookReader.getMeterSerialNumber()));
                } catch (NotInObjectListException e) {
                    collectedLogBook.setFailureInformation(ResultType.InCompatible, MdcManager.getIssueFactory().createWarning(logBookReader, "logBookXissue", logBookReader.getLogBookObisCode().toString(), e.getMessage()));
                }

                if (profileGeneric != null) {
                    Calendar fromDate = getCalendar();
                    fromDate.setTime(logBookReader.getLastLogBook());
                    DataContainer dataContainer;
                    try {
                        dataContainer = profileGeneric.getBuffer(fromDate, getCalendar());
                        collectedLogBook.setCollectedMeterEvents(parseEvents(dataContainer, logBookReader.getLogBookObisCode()));
                    } catch (NotInObjectListException e) {
                        collectedLogBook.setFailureInformation(ResultType.InCompatible, MdcManager.getIssueFactory().createWarning(logBookReader, "logBookXissue", logBookReader.getLogBookObisCode().toString(), e.getMessage()));
                    } catch (IOException e) {
                        if (IOExceptionHandler.isUnexpectedResponse(e, protocol.getDlmsSession())) {
                            collectedLogBook.setFailureInformation(ResultType.NotSupported, MdcManager.getIssueFactory().createWarning(logBookReader, "logBookXnotsupported", logBookReader.getLogBookObisCode().toString()));
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

    private List<MeterProtocolEvent> parseEvents(DataContainer dataContainer, ObisCode logBookObisCode) throws ProtocolException {
        List<MeterEvent> meterEvents;
        if (logBookObisCode.equals(getMeterConfig().getEventLogObject().getObisCode())) {
            meterEvents = new EventsLog(dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(getMeterConfig().getControlLogObject().getObisCode())) {
            meterEvents = new DisconnectControlLog(dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(getMeterConfig().getPowerFailureLogObject().getObisCode())) {
            meterEvents = new PowerFailureLog(dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(getMeterConfig().getFraudDetectionLogObject().getObisCode())) {
            meterEvents = new FraudDetectionLog(dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(getMeterConfig().getMbusEventLogObject().getObisCode())) {
            meterEvents = new MbusLog(dataContainer).getMeterEvents();
        } else if (logBookObisCode.equalsIgnoreBChannel(getMeterConfig().getMbusControlLog(0).getObisCode())) {
            meterEvents = new MbusControlLog(dataContainer).getMeterEvents();
        } else {
            return new ArrayList<>();
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