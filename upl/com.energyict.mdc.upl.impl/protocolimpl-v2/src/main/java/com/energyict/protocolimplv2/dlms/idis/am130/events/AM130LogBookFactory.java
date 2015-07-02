package com.energyict.protocolimplv2.dlms.idis.am130.events;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MeterProtocolEvent;
import com.energyict.protocolimpl.dlms.idis.events.AbstractEvent;
import com.energyict.protocolimpl.dlms.idis.events.DisconnectorControlLog;
import com.energyict.protocolimpl.dlms.idis.events.PowerFailureEventLog;
import com.energyict.protocolimplv2.dlms.idis.am130.AM130;
import com.energyict.protocolimplv2.dlms.idis.am500.events.IDISLogBookFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 9/02/2015 - 14:33
 */
public class AM130LogBookFactory extends IDISLogBookFactory {

    private static ObisCode COMMUNICATION_LOG = ObisCode.fromString("0.0.99.98.5.255");

    public AM130LogBookFactory(AM130 protocol) {
        super(protocol);
        supportedLogBooks.add(COMMUNICATION_LOG);
    }

    protected List<MeterProtocolEvent> parseEvents(DataContainer dataContainer, ObisCode logBookObisCode) {
        List<MeterEvent> meterEvents;
        if (logBookObisCode.equals(POWER_QUALITY_LOG)) {
            meterEvents = new AM130PowerQualityEventLog(protocol.getTimeZone(), dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(POWER_FAILURE_EVENT_LOG)) {
            meterEvents = new PowerFailureEventLog(protocol.getTimeZone(), dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(DISCONNECTOR_CONTROL_LOG)) {
            meterEvents = new DisconnectorControlLog(protocol.getTimeZone(), dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(FRAUD_DETECTION_LOG)) {
            meterEvents = new AM130FraudDetectionLog(protocol.getTimeZone(), dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(STANDARD_EVENT_LOG)) {
            meterEvents = new AM130StandardEventLog(protocol.getTimeZone(), dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(COMMUNICATION_LOG)) {
            meterEvents = new AM130CommunicationLog(protocol.getTimeZone(), dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(MBUS_EVENT_LOG)) {
            meterEvents = new AM130MBusEventLog(protocol.getTimeZone(), dataContainer).getMeterEvents();
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
                mBusControlLog = new AM130MBusControlLog1(protocol.getTimeZone(), mBusControlLogDC);
                break;
            case 2:
                mBusControlLog = new AM130MBusControlLog2(protocol.getTimeZone(), mBusControlLogDC);
                break;
            case 3:
                mBusControlLog = new AM130MBusControlLog3(protocol.getTimeZone(), mBusControlLogDC);
                break;
            case 4:
                mBusControlLog = new AM130MBusControlLog4(protocol.getTimeZone(), mBusControlLogDC);
                break;
            case 5:
                mBusControlLog = new AM130MBusControlLog5(protocol.getTimeZone(), mBusControlLogDC);
                break;
            case 6:
                mBusControlLog = new AM130MBusControlLog6(protocol.getTimeZone(), mBusControlLogDC);
                break;
            default:
                return new ArrayList<>();
        }
        return mBusControlLog.getMeterEvents();
    }
}