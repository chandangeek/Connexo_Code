package com.energyict.protocolimplv2.nta.esmr50.common.events;

import com.energyict.dlms.DataContainer;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MeterProtocolEvent;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.nta.dsmr23.eventhandling.PowerFailureLog;
import com.energyict.protocolimplv2.nta.dsmr40.eventhandling.Dsmr40LogBookFactory;
import com.energyict.protocolimplv2.nta.dsmr40.eventhandling.VoltageQualityEventLog;

import java.util.ArrayList;
import java.util.List;

public class Esmr50LogBookFactory extends Dsmr40LogBookFactory {
    private static final ObisCode COMMS_EVENT_LOG = ObisCode.fromString("0.0.99.98.4.255");

    public Esmr50LogBookFactory(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(protocol, collectedDataFactory, issueFactory);
    }

    @Override
    public void initializeSupportedLogBooks() {
        supportedLogBooks = new ArrayList<>();
        super.initializeSupportedLogBooks();
        supportedLogBooks.add(COMMS_EVENT_LOG);
    }

    @Override
    protected List<MeterProtocolEvent> parseEvents(DataContainer dataContainer, LogBookReader logBookReader) throws
            ProtocolException {
        ObisCode logBookObisCode = logBookReader.getLogBookObisCode();
        getProtocol().journal("Parsing logbook "+logBookObisCode);
        List<MeterEvent> meterEvents;
        if (logBookObisCode.equals(STANDARD_EVENT_LOG)) {
            getProtocol().journal("Parsing as standard event log");
            meterEvents = new ESMR50StandardEventLog(dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(POWER_FAILURE_LOG)) {
            getProtocol().journal("Parsing as power failure log");
            meterEvents = new PowerFailureLog(dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(FRAUD_DETECTION_LOG)) {
            getProtocol().journal("Parsing as fraud detection log");
            meterEvents = new ESMR50FraudDetectionLog(dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(COMMS_EVENT_LOG)) {
            getProtocol().journal("Parsing as communication sessions log");
            meterEvents = new ESMR50CommunicationSessionLog(dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(VOLTAGE_QUALITY_LOG)) {
            getProtocol().journal("Parsing as voltage quality log");
            meterEvents = new VoltageQualityEventLog(dataContainer).getMeterEvents();
        } else if (logBookObisCode.equalsIgnoreBChannel(MBUS_CONTROL_LOG)) {
            int channel = this.getProtocol().getPhysicalAddressFromSerialNumber(logBookReader.getMeterSerialNumber());
            getProtocol().journal("Parsing as MBus control log on channel "+channel);
            meterEvents = new ESMR50MbusControlLog(dataContainer, channel).getMeterEvents();
        } else if (logBookObisCode.equalsIgnoreBChannel(MBUS_EVENT_LOG)) {
            int channel = this.getProtocol().getPhysicalAddressFromSerialNumber(logBookReader.getMeterSerialNumber());
            getProtocol().journal("Parsing as MBus event log on channel "+channel);
            meterEvents = new ESMR50MbusEventLog(dataContainer, channel).getMeterEvents();
        } else{
            getProtocol().journal("Logbook " + logBookObisCode + " not supported by protocol");
            return new ArrayList<>();
        }
        getProtocol().journal("Decoded "+meterEvents.size()+" events from "+logBookObisCode);
        return MeterEvent.mapMeterEventsToMeterProtocolEvents(meterEvents);
    }
}
