package com.energyict.protocolimplv2.nta.dsmr50.elster.am540.logbooks;

import com.elster.jupiter.metering.MeteringService;
import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.axrdencoding.util.AXDRDateTimeDeviationType;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.LogBookReader;
import com.energyict.mdc.protocol.api.NotInObjectListException;
import com.energyict.mdc.protocol.api.ProtocolException;
import com.energyict.mdc.protocol.api.device.data.CollectedLogBook;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.mdc.protocol.api.device.events.MeterProtocolEvent;
import com.energyict.mdc.protocol.api.tasks.support.DeviceLogBookSupport;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.nta.IOExceptionHandler;
import com.energyict.protocolimplv2.nta.dsmr50.elster.am540.AM540;
import com.energyict.protocols.util.ProtocolUtils;
import com.energyict.smartmeterprotocolimpl.common.topology.DeviceMapping;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.eventhandling.DisconnectControlLog;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.eventhandling.PowerFailureLog;
import com.energyict.smartmeterprotocolimpl.nta.dsmr50.elster.am540.events.AM540FraudDetectionLog;
import com.energyict.smartmeterprotocolimpl.nta.dsmr50.elster.am540.events.AM540StandardEventLog;

import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 29.09.15
 * Time: 10:46
 */
public class Dsmr50LogBookFactory implements DeviceLogBookSupport {

    private static final ObisCode MBUS_EVENT_LOG = ObisCode.fromString("0.0.99.98.3.255");
    private static final ObisCode MBUS_CONTROL_LOG = ObisCode.fromString("0.1.24.5.0.255");
    protected static ObisCode STANDARD_EVENT_LOG = ObisCode.fromString("0.0.99.98.0.255");
    protected static ObisCode FRAUD_DETECTION_LOG = ObisCode.fromString("0.0.99.98.1.255");
    protected static ObisCode DISCONNECTOR_CONTROL_LOG = ObisCode.fromString("0.0.99.98.2.255");
    protected static ObisCode POWER_FAILURE_EVENT_LOG = ObisCode.fromString("1.0.99.97.0.255");
    private final CollectedDataFactory collectedDataFactory;
    private final IssueService issueService;
    private final MeteringService meteringService;
    private AM540 protocol;

    /**
     * List of obiscodes of the supported log books
     */
    private List<ObisCode> supportedLogBooks;

    public Dsmr50LogBookFactory(CollectedDataFactory collectedDataFactory, IssueService issueService, MeteringService meteringService, AM540 protocol) {
        this.collectedDataFactory = collectedDataFactory;
        this.issueService = issueService;
        this.meteringService = meteringService;
        this.protocol = protocol;
        initializeSupportedLogBooks(protocol);
    }

    public void initializeSupportedLogBooks(AM540 protocol) {
        supportedLogBooks = new ArrayList<>();
        supportedLogBooks.add(STANDARD_EVENT_LOG);
        supportedLogBooks.add(FRAUD_DETECTION_LOG);
        supportedLogBooks.add(DISCONNECTOR_CONTROL_LOG);
        supportedLogBooks.add(POWER_FAILURE_EVENT_LOG);

        // MBus related event logs
        supportedLogBooks.add(MBUS_EVENT_LOG);
        for (DeviceMapping mbusMeter : protocol.getMeterTopology().getMbusMeterMap()) {
            ObisCode correctedObisCode = ProtocolTools.setObisCodeField(MBUS_CONTROL_LOG, 1, (byte) mbusMeter.getPhysicalAddress());
            supportedLogBooks.add(correctedObisCode);
        }
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        List<CollectedLogBook> result = new ArrayList<>();
        for (LogBookReader logBookReader : logBooks) {
            CollectedLogBook collectedLogBook = this.collectedDataFactory.createCollectedLogBook(logBookReader.getLogBookIdentifier());
            if (isSupported(logBookReader)) {
                ProfileGeneric profileGeneric = null;
                try {
                    profileGeneric = protocol.getDlmsSession().getCosemObjectFactory().getProfileGeneric(protocol.getPhysicalAddressCorrectedObisCode(logBookReader.getLogBookObisCode(), logBookReader.getDeviceIdentifier().getIdentifier()));
                } catch (ProtocolException e) {
                    collectedLogBook.setFailureInformation(ResultType.InCompatible, this.issueService.newIssueCollector().addWarning(logBookReader, "logBookXissue", logBookReader.getLogBookObisCode().toString(), e.getMessage()));
                }
                if (profileGeneric != null) {
                    Calendar fromDate = getCalendar();
                    fromDate.setTime(Date.from(logBookReader.getLastLogBook()));
                    DataContainer dataContainer;
                    try {
                        dataContainer = profileGeneric.getBuffer(fromDate, getCalendar());
                        collectedLogBook.setMeterEvents(parseEvents(dataContainer, logBookReader.getLogBookObisCode()));
                    } catch (NotInObjectListException e) {
                        collectedLogBook.setFailureInformation(ResultType.InCompatible, this.issueService.newIssueCollector().addWarning(logBookReader, "logBookXissue", logBookReader.getLogBookObisCode().toString(), e.getMessage()));
                    } catch (IOException e) {
                        if (IOExceptionHandler.isUnexpectedResponse(e, protocol.getDlmsSession())) {
                            collectedLogBook.setFailureInformation(ResultType.NotSupported, this.issueService.newIssueCollector().addWarning(logBookReader, "logBookXnotsupported", logBookReader.getLogBookObisCode().toString()));
                        }
                    }
                }
            } else {
                collectedLogBook.setFailureInformation(ResultType.NotSupported, this.issueService.newIssueCollector().addWarning(logBookReader, "logBookXnotsupported", logBookReader.getLogBookObisCode().toString()));
            }
            result.add(collectedLogBook);
        }
        return result;
    }

    private List<MeterProtocolEvent> parseEvents(DataContainer dataContainer, ObisCode logBookObisCode) throws ProtocolException {
        List<MeterEvent> meterEvents;
        if (logBookObisCode.equals(getMeterConfig().getEventLogObject().getObisCode())) {
            meterEvents = new AM540StandardEventLog(dataContainer, AXDRDateTimeDeviationType.Negative).getMeterEvents();
        } else if (logBookObisCode.equals(getMeterConfig().getControlLogObject().getObisCode())) {
            meterEvents = new DisconnectControlLog(dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(getMeterConfig().getPowerFailureLogObject().getObisCode())) {
            meterEvents = new PowerFailureLog(dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(getMeterConfig().getFraudDetectionLogObject().getObisCode())) {
            meterEvents = new AM540FraudDetectionLog(dataContainer, AXDRDateTimeDeviationType.Negative).getMeterEvents();
        } else if (logBookObisCode.equals(getMeterConfig().getMbusEventLogObject().getObisCode())) {
            meterEvents = new AM540MbusLog(protocol.getTimeZone(), dataContainer).getMeterEvents();
        } else if (logBookObisCode.equalsIgnoreBChannel(getMeterConfig().getMbusControlLog(0).getObisCode())) {
            meterEvents = new AM540MbusControlLog(dataContainer).getMeterEvents();
        } else {
            return new ArrayList<>();
        }
        return MeterEvent.mapMeterEventsToMeterProtocolEvents(meterEvents, meteringService);
    }

    private boolean isSupported(LogBookReader logBookReader) {
        for (ObisCode supportedLogBookObisCode : supportedLogBooks) {
            if (supportedLogBookObisCode.equals(protocol.getPhysicalAddressCorrectedObisCode(logBookReader.getLogBookObisCode(), logBookReader.getDeviceIdentifier().getIdentifier()))) {
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