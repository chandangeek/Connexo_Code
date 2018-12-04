package com.energyict.protocolimplv2.nta.dsmr40.eventhandling;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;

import com.energyict.dlms.DataContainer;
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

    private static final ObisCode VOLTAGE_QUALITY_LOG = ObisCode.fromString("0.0.99.98.5.255");

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
        List<MeterEvent> meterEvents;
        if (logBookObisCode.equals(getMeterConfig().getEventLogObject().getObisCode())) {
            meterEvents = new StandardEventLog(dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(getMeterConfig().getControlLogObject().getObisCode())) {
            meterEvents = new DisconnectControlLog(dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(getMeterConfig().getPowerFailureLogObject().getObisCode())) {
            meterEvents = new PowerFailureLog(dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(getMeterConfig().getFraudDetectionLogObject().getObisCode())) {
            meterEvents = new FraudDetectionLog(dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(getMeterConfig().getMbusEventLogObject().getObisCode())) {
            int channel = protocol.getPhysicalAddressFromSerialNumber(logBookReader.getMeterSerialNumber());
            meterEvents = new MbusEventLog(dataContainer, channel).getMeterEvents();
        } else if (logBookObisCode.equalsIgnoreBChannel(getMeterConfig().getMbusControlLog(0).getObisCode())) {
            meterEvents = new MbusControlLog(dataContainer).getMeterEvents();
        } else if (logBookObisCode.equalsIgnoreBChannel(getMeterConfig().getVoltageQualityLogObject().getObisCode())) {
            meterEvents = new VoltageQualityEventLog(dataContainer).getMeterEvents();
        } else {
            return new ArrayList<>();
        }
        return MeterEvent.mapMeterEventsToMeterProtocolEvents(meterEvents);
    }
}