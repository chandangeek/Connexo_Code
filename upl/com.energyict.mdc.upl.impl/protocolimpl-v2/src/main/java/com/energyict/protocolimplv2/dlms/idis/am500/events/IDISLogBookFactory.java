package com.energyict.protocolimplv2.dlms.idis.am500.events;

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
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.dlms.idis.events.AbstractEvent;
import com.energyict.protocolimpl.dlms.idis.events.DisconnectorControlLog;
import com.energyict.protocolimpl.dlms.idis.events.FraudDetectionLog;
import com.energyict.protocolimpl.dlms.idis.events.MBusControlLog1;
import com.energyict.protocolimpl.dlms.idis.events.MBusControlLog2;
import com.energyict.protocolimpl.dlms.idis.events.MBusControlLog3;
import com.energyict.protocolimpl.dlms.idis.events.MBusControlLog4;
import com.energyict.protocolimpl.dlms.idis.events.MBusEventLog;
import com.energyict.protocolimpl.dlms.idis.events.PowerFailureEventLog;
import com.energyict.protocolimpl.dlms.idis.events.PowerQualityEventLog;
import com.energyict.protocolimpl.dlms.idis.events.StandardEventLog;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.dlms.idis.am500.AM500;
import com.energyict.protocolimplv2.nta.IOExceptionHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Copyrights EnergyICT
 * <p/>
 * Supports both the e-meter and MBus meter logbooks.
 * Note that in EIServer, they should be configured on the proper master and slave devices.
 *
 * @author khe
 * @since 6/01/2015 - 9:42
 */
public class IDISLogBookFactory implements DeviceLogBookSupport {

    protected static ObisCode DISCONNECTOR_CONTROL_LOG = ObisCode.fromString("0.0.99.98.2.255");
    protected static ObisCode STANDARD_EVENT_LOG = ObisCode.fromString("0.0.99.98.0.255");
    protected static ObisCode FRAUD_DETECTION_LOG = ObisCode.fromString("0.0.99.98.1.255");
    protected static ObisCode POWER_FAILURE_EVENT_LOG = ObisCode.fromString("1.0.99.97.0.255");
    protected static ObisCode POWER_QUALITY_LOG = ObisCode.fromString("0.0.99.98.4.255");
    protected static ObisCode MBUS_EVENT_LOG = ObisCode.fromString("0.0.99.98.3.255");    //General MBus log, describing events for all slave devices
    protected static ObisCode MBUS_CONTROL_LOG = ObisCode.fromString("0.x.24.5.0.255");   //Specific log for 1 MBus slave device
    /**
     * List of obiscodes of the supported log books
     */
    protected final List<ObisCode> supportedLogBooks;
    protected AM500 protocol;

    public IDISLogBookFactory(AM500 protocol) {
        this.protocol = protocol;
        supportedLogBooks = new ArrayList<>();
        supportedLogBooks.add(DISCONNECTOR_CONTROL_LOG);
        supportedLogBooks.add(STANDARD_EVENT_LOG);
        supportedLogBooks.add(FRAUD_DETECTION_LOG);
        supportedLogBooks.add(POWER_FAILURE_EVENT_LOG);
        supportedLogBooks.add(POWER_QUALITY_LOG);
        supportedLogBooks.add(MBUS_EVENT_LOG);
        supportedLogBooks.add(MBUS_CONTROL_LOG);
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        List<CollectedLogBook> result = new ArrayList<>();
        for (LogBookReader logBookReader : logBooks) {
            CollectedLogBook collectedLogBook = MdcManager.getCollectedDataFactory().createCollectedLogBook(logBookReader.getLogBookIdentifier());
            if (isSupported(logBookReader)) {
                ProfileGeneric profileGeneric = null;
                try {
                    profileGeneric = protocol.getDlmsSession().getCosemObjectFactory().getProfileGeneric(logBookReader.getLogBookObisCode());
                    profileGeneric.setDsmr4SelectiveAccessFormat(true);
                } catch (NotInObjectListException e) {
                    collectedLogBook.setFailureInformation(ResultType.InCompatible, MdcManager.getIssueFactory().createWarning(logBookReader, "logBookXissue", logBookReader.getLogBookObisCode().toString(), e.getMessage()));
                }

                if (profileGeneric != null) {
                    Calendar fromDate = getCalendar();
                    fromDate.setTime(logBookReader.getLastLogBook());

                    try {
                        if (protocol.getPhysicalAddressFromSerialNumber(logBookReader.getMeterSerialNumber()) > 0) {
                            //MBus slave logbook
                            List<MeterEvent> mbusEvents = getMBusControlLog(fromDate, getCalendar(), logBookReader);
                            collectedLogBook.setCollectedMeterEvents(MeterEvent.mapMeterEventsToMeterProtocolEvents(mbusEvents));
                        } else {
                            //E-meter logbook
                            DataContainer dataContainer = profileGeneric.getBuffer(fromDate, getCalendar());
                            collectedLogBook.setCollectedMeterEvents(parseEvents(dataContainer, logBookReader.getLogBookObisCode()));
                        }
                    } catch (IOException e) {
                        if (IOExceptionHandler.isUnexpectedResponse(e, protocol.getDlmsSession())) {
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

    protected List<MeterProtocolEvent> parseEvents(DataContainer dataContainer, ObisCode logBookObisCode) {
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

    protected List<MeterEvent> getMBusControlLog(Calendar fromCal, Calendar toCal, LogBookReader logBookReader) throws IOException {
        ObisCode mBusControlLogObisCode = getMBusControlLogObisCode(logBookReader.getMeterSerialNumber());
        ProfileGeneric profileGeneric = protocol.getDlmsSession().getCosemObjectFactory().getProfileGeneric(mBusControlLogObisCode);
        profileGeneric.setDsmr4SelectiveAccessFormat(true);
        DataContainer mBusControlLogDC = profileGeneric.getBuffer(fromCal, toCal);
        AbstractEvent mBusControlLog;
        switch (protocol.getPhysicalAddressFromSerialNumber(logBookReader.getMeterSerialNumber())) {
            case 1:
                mBusControlLog = new MBusControlLog1(protocol.getTimeZone(), mBusControlLogDC);
                break;
            case 2:
                mBusControlLog = new MBusControlLog2(protocol.getTimeZone(), mBusControlLogDC);
                break;
            case 3:
                mBusControlLog = new MBusControlLog3(protocol.getTimeZone(), mBusControlLogDC);
                break;
            case 4:
                mBusControlLog = new MBusControlLog4(protocol.getTimeZone(), mBusControlLogDC);
                break;
            default:
                return new ArrayList<>();
        }
        return mBusControlLog.getMeterEvents();
    }

    protected ObisCode getMBusControlLogObisCode(String serialNumber) {
        return protocol.getPhysicalAddressCorrectedObisCode(MBUS_CONTROL_LOG, serialNumber);
    }

    private boolean isSupported(LogBookReader logBookReader) {
        for (ObisCode supportedLogBook : supportedLogBooks) {
            if (supportedLogBook.equalsIgnoreBChannel(logBookReader.getLogBookObisCode())) {
                return true;
            }
        }
        return false;
    }

    private Calendar getCalendar() {
        return ProtocolUtils.getCalendar(protocol.getTimeZone());
    }
}