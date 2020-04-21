package com.energyict.protocolimplv2.nta.esmr50.common.events;

import com.energyict.protocolimplv2.nta.esmr50.elster;
import com.energyict.protocolimplv2.nta.esmr50.itron;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.aso.SecurityContext;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MeterProtocolEvent;
import com.energyict.protocolimplv2.nta.abstractnta.profiles.AbstractNtaLogBookFactory;
import com.energyict.protocolimplv2.nta.dsmr23.eventhandling.PowerFailureLog;
import com.energyict.protocolimplv2.nta.dsmr40.eventhandling.Dsmr40LogBookFactory;
import com.energyict.protocolimplv2.nta.dsmr40.eventhandling.VoltageQualityEventLog;
import com.energyict.protocolimplv2.nta.esmr50.common.ESMR50Protocol;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import static com.energyict.protocolimplv2.nta.abstractnta.profiles.AbstractNtaLogBookFactory.MeterType.MASTER;
import static com.energyict.protocolimplv2.nta.abstractnta.profiles.AbstractNtaLogBookFactory.MeterType.SLAVE;

public final class ESMR50LogBookFactory extends AbstractNtaLogBookFactory<ESMR50Protocol> {

    public ESMR50LogBookFactory(ESMR50Protocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(protocol, collectedDataFactory, issueFactory);
    }

    @Override
    protected boolean isSupported(ObisCode obisCode, MeterType meterType) {
        if (meterType.isMaster()) {
            return  STANDARD_EVENT_LOG.equals(obisCode) ||
                    FRAUD_DETECTION_LOG.equals(obisCode) ||
                    POWER_FAILURE_LOG.equals(obisCode) ||
                    COMMUNICATION_SESSION_EVENT_LOG.equals(obisCode) ||
                    VOLTAGE_QUALITY_LOG.equals(obisCode);
        }
        else {
            return (MBUS_EVENT_LOG.equalsIgnoreBChannel(obisCode) && getUsedMBUSChannels().contains(obisCode.getB()));
        }
    }

    @Override
    protected List<MeterEvent> parseStandardEventLog(DataContainer dataContainer) throws ProtocolException {
        List<MeterEvent> meterEvents = new ESMR50StandardEventLog(dataContainer).getMeterEvents();
        // also check the frame-counter events when reading the frame-counter
        checkFrameCounterEvents(meterEvents);
        return meterEvents;
    }

    @Override
    protected List<MeterEvent> parseControlLog(DataContainer dataContainer) throws ProtocolException {
        return Collections.emptyList();
    }

    @Override
    protected List<MeterEvent> parsePowerFailureLog(DataContainer dataContainer) throws ProtocolException {
        return Collections.emptyList();
    }

    @Override
    protected List<MeterEvent> parseFraudDetectionLog(DataContainer dataContainer) throws ProtocolException {
        return new ESMR50FraudDetectionLog(dataContainer).getMeterEvents();
    }

    @Override
    protected List<MeterEvent> parseCommunicationLogEventLog(DataContainer dataContainer) throws ProtocolException {
        return new ESMR50CommunicationSessionLog(dataContainer).getMeterEvents();
    }

    @Override
    protected List<MeterEvent> parseMBUSControlLog(DataContainer dataContainer, int channel) throws ProtocolException {
        ESMR50MbusControlLog mbusControlLog = new ESMR50MbusControlLog(dataContainer, channel);
        List<MeterEvent> meterEvents = mbusControlLog.getMeterEvents();
        if (mbusControlLog.getIgnoredEvents() > 0) {
            getProtocol().journal("Ignored events: " + mbusControlLog.getIgnoredEvents());
        }
        return meterEvents;
    }

    @Override
    protected List<MeterEvent> parseMBUSEventLog(DataContainer dataContainer, int channel) throws ProtocolException {
        if( getProtocol().getProtocolDescription().equals("Itron Crypto MbusDevice DLMS (NTA ESMR5.0) V2" ) )
        {
            return new ItronMBusEventLog(dataContainer).getMeterEvents();
        }
        else if(getProtocol().getProtocolDescription().equals("Elster Crypto MbusDevice DLMS (NTA ESMR5.0) V2" ))
        {
            return new ElsterMBusEventLog(dataContainer).getMeterEvents();
        }
        else
            return new ESMR50MbusEventLog(dataContainer).getMeterEvents();
    }

    @Override
    protected List<MeterEvent> parseVoltageQualityLog(DataContainer dataContainer) throws ProtocolException {
        return new VoltageQualityEventLog(dataContainer).getMeterEvents();
    }

    @Override
    protected ObisCode getPhysicalAddressCorrectedObisCode(ObisCode obisCode, String meterSerialNumber) {
        return getProtocol().getPhysicalAddressCorrectedObisCode(obisCode, meterSerialNumber);
    }

    protected void checkFrameCounterEvents(List<MeterEvent> eventList) {
        SecurityContext securityContext = getProtocol().getDlmsSession().getAso().getSecurityContext();

        generateFrameCounterLimitEvent(securityContext.getFrameCounter(), "Frame Counter", 900, MeterEvent.SEND_FRAME_COUNTER_ABOVE_THRESHOLD, eventList);

        if (securityContext.getResponseFrameCounter() != 0) {
            generateFrameCounterLimitEvent(securityContext.getResponseFrameCounter(),"Response Frame Counter", 901, MeterEvent.RECEIVE_FRAME_COUNTER_ABOVE_THRESHOLD, eventList);
        } else {
            getProtocol().journal("Response frame counter not initialized.");
        }

    }

    protected void generateFrameCounterLimitEvent(long frameCounter, String name, int eventId, int eiCode, List<MeterEvent> eventList) {
        try {
            long frameCounterLimit = this.getProtocol().getDlmsSessionProperties().getFrameCounterLimit();

            if (frameCounterLimit == 0){
                getProtocol().journal("Frame counter threshold not configured. FYI the current " + name + " is " + frameCounter);
            } else if (frameCounter > frameCounterLimit) {
                getProtocol().journal(name+": " + frameCounter + " is above the threshold (" + frameCounterLimit + ") - will create an event");
                MeterEvent frameCounterEvent = new MeterEvent(new Date(), eiCode, eventId, name+" above threshold: " + frameCounter);
                eventList.add(frameCounterEvent);
            } else {
                getProtocol().journal(name + " below configured threshold: " + frameCounter + " < "+frameCounterLimit);
            }
        } catch (Exception e) {
            getProtocol().journal(Level.WARNING, "Error getting  " + name + e.getMessage(), e);
        }
    }

}
