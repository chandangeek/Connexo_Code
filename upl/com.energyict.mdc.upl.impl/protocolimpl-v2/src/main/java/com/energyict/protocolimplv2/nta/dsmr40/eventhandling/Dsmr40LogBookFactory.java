package com.energyict.protocolimplv2.nta.dsmr40.eventhandling;

import com.energyict.dlms.DataContainer;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.nta.abstractnta.AbstractSmartNtaProtocol;
import com.energyict.protocolimplv2.nta.abstractnta.profiles.AbstractNtaLogBookFactory;
import com.energyict.protocolimplv2.nta.dsmr23.eventhandling.DisconnectControlLog;
import com.energyict.protocolimplv2.nta.dsmr23.eventhandling.MbusControlLog;
import com.energyict.protocolimplv2.nta.dsmr23.eventhandling.PowerFailureLog;
import com.energyict.protocolimplv2.nta.esmr50.common.events.ESMR50CommunicationSessionLog;

import java.util.Collections;
import java.util.List;

import static com.energyict.protocolimplv2.nta.abstractnta.profiles.AbstractNtaLogBookFactory.MeterType.MASTER;
import static com.energyict.protocolimplv2.nta.abstractnta.profiles.AbstractNtaLogBookFactory.MeterType.SLAVE;

public class Dsmr40LogBookFactory extends AbstractNtaLogBookFactory<AbstractSmartNtaProtocol> {

    public Dsmr40LogBookFactory(AbstractSmartNtaProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(protocol, collectedDataFactory, issueFactory);
    }

    @Override
    protected boolean isSupported(ObisCode obisCode, MeterType meterType) {
        if (meterType.isMaster()) {
            return  STANDARD_EVENT_LOG.equals(obisCode) ||
                    FRAUD_DETECTION_LOG.equals(obisCode) ||
                    POWER_FAILURE_LOG.equals(obisCode) ||
                    COMMUNICATION_SESSION_EVENT_LOG.equals(obisCode);
        }
        else {
            return MBUS_EVENT_LOG.equals(obisCode);
        }
    }

    @Override
    protected List<MeterEvent> parseStandardEventLog(DataContainer dataContainer) throws ProtocolException {
        return new StandardEventLog(dataContainer).getMeterEvents();
    }

    @Override
    protected List<MeterEvent> parseControlLog(DataContainer dataContainer) throws ProtocolException {
        return new DisconnectControlLog(dataContainer).getMeterEvents();
    }

    @Override
    protected List<MeterEvent> parsePowerFailureLog(DataContainer dataContainer) throws ProtocolException {
        return  new PowerFailureLog(dataContainer).getMeterEvents();
    }

    @Override
    protected List<MeterEvent> parseFraudDetectionLog(DataContainer dataContainer) throws ProtocolException {
        return new FraudDetectionLog(dataContainer).getMeterEvents();
    }

    @Override
    protected List<MeterEvent> parseCommunicationLogEventLog(DataContainer dataContainer) throws ProtocolException {
        return new ESMR50CommunicationSessionLog(dataContainer).getMeterEvents();
    }

    @Override
    protected List<MeterEvent> parseMBUSControlLog(DataContainer dataContainer, int channel) throws ProtocolException {
        return new MbusControlLog(dataContainer).getMeterEvents();
    }

    @Override
    protected List<MeterEvent> parseMBUSEventLog(DataContainer dataContainer, int channel) throws ProtocolException {
        DSMR40MbusEventLog mbusEventLog = new DSMR40MbusEventLog(dataContainer, channel);
        List <MeterEvent> meterEvents = mbusEventLog.getMeterEvents();
        if (mbusEventLog.getIgnoredEvents() > 0 ) {
            getProtocol().journal("WARNING: There are " + mbusEventLog.getIgnoredEvents() + " ignored events on MBus event log on channel " + channel);
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