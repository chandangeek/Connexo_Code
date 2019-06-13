package com.energyict.protocolimplv2.nta.dsmr40.eventhandling;

import com.energyict.dlms.DataContainer;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MeterProtocolEvent;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.nta.dsmr23.eventhandling.DisconnectControlLog;
import com.energyict.protocolimplv2.nta.dsmr23.eventhandling.MbusControlLog;
import com.energyict.protocolimplv2.nta.dsmr23.eventhandling.PowerFailureLog;
import com.energyict.protocolimplv2.nta.dsmr23.profiles.Dsmr23LogBookFactory;

import java.util.ArrayList;
import java.util.List;

public class Dsmr40LogBookFactory extends Dsmr23LogBookFactory {

    protected static final ObisCode VOLTAGE_QUALITY_LOG = ObisCode.fromString("0.0.99.98.5.255");

    public Dsmr40LogBookFactory(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(protocol, collectedDataFactory, issueFactory);
    }

    @Override
    public void initializeSupportedLogBooks() {
        super.initializeSupportedLogBooks();
        supportedLogBooks.add(VOLTAGE_QUALITY_LOG);
    }

    @Override
    protected List<MeterProtocolEvent> parseEvents(DataContainer dataContainer, LogBookReader logBookReader) throws ProtocolException {
        ObisCode logBookObisCode = logBookReader.getLogBookObisCode();
        getProtocol().journal("Parsing logbook "+logBookObisCode);
        List<MeterEvent> meterEvents;
        if (logBookObisCode.equals(STANDARD_EVENT_LOG)) {
            getProtocol().journal("Parsing as standard event log");
            meterEvents = new StandardEventLog(dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(CONTROL_LOG)) {
            getProtocol().journal("Parsing as disconnect control log");
            meterEvents = new DisconnectControlLog(dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(POWER_FAILURE_LOG)) {
            getProtocol().journal("Parsing as power failure log");
            meterEvents = new PowerFailureLog(dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(FRAUD_DETECTION_LOG)) {
            getProtocol().journal("Parsing as fraud detection log");
            meterEvents = new FraudDetectionLog(dataContainer).getMeterEvents();
        } else if (logBookObisCode.equalsIgnoreBChannel(MBUS_EVENT_LOG)) {
            int channel = this.getProtocol().getPhysicalAddressFromSerialNumber(logBookReader.getMeterSerialNumber());
            getProtocol().journal("Parsing as MBus event log on channel "+channel);
            meterEvents = new MbusEventLog(dataContainer, channel).getMeterEvents();
        } else if (logBookObisCode.equalsIgnoreBChannel(MBUS_CONTROL_LOG)) {
            getProtocol().journal("Parsing as MBus control log");
            meterEvents = new MbusControlLog(dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(VOLTAGE_QUALITY_LOG)) {
            getProtocol().journal("Parsing as MBus voltage quality log");
            meterEvents = new VoltageQualityEventLog(dataContainer).getMeterEvents();
        } else {
            getProtocol().journal("Logbook " + logBookObisCode + " not supported by protocol");
            return new ArrayList<>();
        }

        getProtocol().journal("Decoded "+meterEvents.size()+" events from "+logBookObisCode);
        return MeterEvent.mapMeterEventsToMeterProtocolEvents(meterEvents);
    }
}