package com.energyict.protocolimplv2.nta.dsmr23.profiles;

import com.energyict.dlms.DataContainer;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.tasks.support.DeviceLogBookSupport;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.nta.abstractnta.AbstractSmartNtaProtocol;
import com.energyict.protocolimplv2.nta.abstractnta.profiles.AbstractNtaLogBookFactory;
import com.energyict.protocolimplv2.nta.dsmr23.eventhandling.DSMR23MbusEventLog;
import com.energyict.protocolimplv2.nta.dsmr23.eventhandling.DisconnectControlLog;
import com.energyict.protocolimplv2.nta.dsmr23.eventhandling.EventsLog;
import com.energyict.protocolimplv2.nta.dsmr23.eventhandling.FraudDetectionLog;
import com.energyict.protocolimplv2.nta.dsmr23.eventhandling.MbusControlLog;
import com.energyict.protocolimplv2.nta.dsmr23.eventhandling.PowerFailureLog;

import java.util.Collections;
import java.util.List;

import static com.energyict.protocolimplv2.nta.abstractnta.profiles.AbstractNtaLogBookFactory.MeterType.MASTER;
import static com.energyict.protocolimplv2.nta.abstractnta.profiles.AbstractNtaLogBookFactory.MeterType.SLAVE;

public class Dsmr23LogBookFactory extends AbstractNtaLogBookFactory<AbstractSmartNtaProtocol> implements DeviceLogBookSupport {

    public Dsmr23LogBookFactory(AbstractSmartNtaProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(protocol, collectedDataFactory, issueFactory);
    }

    @Override
    protected boolean isSupported(ObisCode obisCode, MeterType meterType) {
        if (meterType.isMaster()) {
            return  STANDARD_EVENT_LOG.equals(obisCode) ||
                    POWER_FAILURE_LOG.equals(obisCode) ||
                    FRAUD_DETECTION_LOG.equals(obisCode) ||
                    (CONTROL_LOG.equals(obisCode) && getProtocol().hasBreaker());
        }
        else {
            return  MBUS_EVENT_LOG.equals(obisCode) ||
                    (MBUS_CONTROL_LOG.equals(obisCode) && getProtocol().hasBreaker());
        }

    }

    @Override
    protected List<MeterEvent> parseStandardEventLog(DataContainer dataContainer) throws ProtocolException {
        return new EventsLog(dataContainer).getMeterEvents();
    }

    @Override
    protected List<MeterEvent> parseControlLog(DataContainer dataContainer) throws ProtocolException {
        return new DisconnectControlLog(dataContainer).getMeterEvents();
    }

    @Override
    protected List<MeterEvent> parsePowerFailureLog(DataContainer dataContainer) throws ProtocolException {
        return new PowerFailureLog(dataContainer).getMeterEvents();
    }

    @Override
    protected List<MeterEvent> parseFraudDetectionLog(DataContainer dataContainer) throws ProtocolException {
        return new FraudDetectionLog(dataContainer).getMeterEvents();
    }

    @Override
    protected List<MeterEvent> parseCommunicationLogEventLog(DataContainer dataContainer) throws ProtocolException {
        return Collections.emptyList();
    }

    @Override
    protected List<MeterEvent> parseMBUSControlLog(DataContainer dataContainer, int channel) throws ProtocolException {
        return new MbusControlLog(dataContainer).getMeterEvents();
    }

    @Override
    protected List<MeterEvent> parseMBUSEventLog(DataContainer dataContainer, int channel) throws ProtocolException {
        DSMR23MbusEventLog mBusLog = new DSMR23MbusEventLog(dataContainer, channel);
        List<MeterEvent> meterEvents = mBusLog.getMeterEvents();
        if (mBusLog.getIgnoredEvents()>0){
            getProtocol().journal("Ignored events: "+mBusLog.getIgnoredEvents());
        }
        return meterEvents;
    }

    @Override
    protected List<MeterEvent> parseVoltageQualityLog(DataContainer dataContainer) throws ProtocolException {
        return Collections.emptyList();
    }

    @Override
    protected ObisCode getPhysicalAddressCorrectedObisCode(ObisCode obisCode, String meterSerialNumber) {
        if (MBUS_EVENT_LOG.equalsIgnoreBChannel(obisCode) || MBUS_CONTROL_LOG.equalsIgnoreBChannel(obisCode)) {
            return ProtocolTools.setObisCodeField(obisCode, 1, (byte) 0);
        }
        else {
            return getProtocol().getPhysicalAddressCorrectedObisCode(obisCode, meterSerialNumber);
        }
    }

}