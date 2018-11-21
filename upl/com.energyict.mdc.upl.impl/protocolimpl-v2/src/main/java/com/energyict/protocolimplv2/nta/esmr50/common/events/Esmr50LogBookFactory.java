package com.energyict.protocolimplv2.nta.esmr50.common.events;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;

import com.energyict.dlms.DataContainer;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MeterProtocolEvent;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.nta.dsmr23.eventhandling.PowerFailureLog;
import com.energyict.protocolimplv2.nta.dsmr40.eventhandling.Dsmr40LogBookFactory;
import com.energyict.protocolimplv2.nta.dsmr40.eventhandling.VoltageQualityEventLog;

import java.util.ArrayList;
import java.util.List;

public class Esmr50LogBookFactory extends Dsmr40LogBookFactory {
    private static final ObisCode STANDARD_EVENT_LOG =  ObisCode.fromString("0.0.99.98.0.255");
    private static final ObisCode POWER_FAILURE_LOG =   ObisCode.fromString("1.0.99.97.0.255");
    private static final ObisCode FRAUD_DETECTION_LOG = ObisCode.fromString("0.0.99.98.1.255");
    private static final ObisCode VOLTAGE_QUALITY_LOG = ObisCode.fromString("0.0.99.98.5.255");
    private static final ObisCode COMMS_EVENT_LOG = ObisCode.fromString("0.0.99.98.4.255");

    private static final ObisCode MBUS_EVENT_LOG =  ObisCode.fromString("0.x.99.98.3.255");

    public Esmr50LogBookFactory(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(protocol, collectedDataFactory, issueFactory);
    }

    @Override
    public void initializeSupportedLogBooks() {
        supportedLogBooks = new ArrayList<>();
        supportedLogBooks.add(STANDARD_EVENT_LOG);
        supportedLogBooks.add(POWER_FAILURE_LOG);
        supportedLogBooks.add(FRAUD_DETECTION_LOG);
        supportedLogBooks.add(COMMS_EVENT_LOG);
        supportedLogBooks.add(VOLTAGE_QUALITY_LOG);
    }

    @Override
    protected List<MeterProtocolEvent> parseEvents(DataContainer dataContainer, ObisCode logBookObisCode) throws
            ProtocolException {
        List<MeterEvent> meterEvents;
        if (logBookObisCode.equals(getMeterConfig().getEventLogObject().getObisCode())) {
            meterEvents = new ESMR50StandardEventLog(dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(getMeterConfig().getPowerFailureLogObject().getObisCode())) {
            meterEvents = new PowerFailureLog(dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(getMeterConfig().getFraudDetectionLogObject().getObisCode())) {
            meterEvents = new ESMR50FraudDetectionLog(dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(getMeterConfig().getCommunicationSessionLogObject().getObisCode())) {
            meterEvents = new ESMR50CommunicationSessionLog(dataContainer).getMeterEvents();
        } else if (logBookObisCode.equalsIgnoreBChannel(getMeterConfig().getVoltageQualityLogObject().getObisCode())) {
            meterEvents = new VoltageQualityEventLog(dataContainer).getMeterEvents();
//        } else if (logBookObisCode.equals(getMeterConfig().getMbusEventLogObject().getObisCode())) {
//            meterEvents = new MbusEventLog(dataContainer).getMeterEvents();
//        } else if (logBookObisCode.equalsIgnoreBChannel(getMeterConfig().getMbusControlLog(0).getObisCode())) {
//            meterEvents = new MbusControlLog(dataContainer).getMeterEvents();
        } else {
            return new ArrayList<>();
        }
        return MeterEvent.mapMeterEventsToMeterProtocolEvents(meterEvents);
    }
}
