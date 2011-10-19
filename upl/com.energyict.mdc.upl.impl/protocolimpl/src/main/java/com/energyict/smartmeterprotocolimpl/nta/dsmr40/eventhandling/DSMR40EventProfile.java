package com.energyict.smartmeterprotocolimpl.nta.dsmr40.eventhandling;

import com.energyict.dlms.DataContainer;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.smartmeterprotocolimpl.common.topology.DeviceMapping;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractSmartNtaProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.eventhandling.*;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.profiles.EventProfile;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * Extends functionality from the DSMR 2.3 EventProfile, but uses the specific DSMR4.0 EventLogbooks
 */
public class DSMR40EventProfile extends EventProfile {

    public DSMR40EventProfile(AbstractSmartNtaProtocol protocol) {
        super(protocol);
    }

    @Override
    public List<MeterEvent> getEvents(Date fromDate) throws IOException {
        List<MeterEvent> eventList = new ArrayList<MeterEvent>();

        Calendar fromCal = ProtocolUtils.getCleanCalendar(getTimeZone());
        if (fromDate == null) {
            fromDate = ProtocolUtils.getClearLastMonthDate(getTimeZone());
        }
        fromCal.setTime(fromDate);
        protocol.getLogger().log(Level.INFO, "Reading EVENTS from meter with serialnumber " + protocol.getSerialNumber() + ".");
        DataContainer dcEvent = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getEventLogObject().getObisCode()).getBuffer(fromCal, getToCalendar());
        DataContainer dcControlLog = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getControlLogObject().getObisCode()).getBuffer(fromCal, getToCalendar());
        DataContainer dcPowerFailure = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getPowerFailureLogObject().getObisCode()).getBuffer(fromCal, getToCalendar());
        DataContainer dcFraudDetection = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getFraudDetectionLogObject().getObisCode()).getBuffer(fromCal, getToCalendar());
        DataContainer dcMbusEventLog = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getMbusEventLogObject().getObisCode()).getBuffer(fromCal, getToCalendar());

        StandardEventLog standardEvents = new StandardEventLog(getTimeZone(), dcEvent);
        FraudDetectionLog fraudDetectionEvents = new FraudDetectionLog(getTimeZone(), dcFraudDetection);
        DisconnectControlLog disconnectControl = new DisconnectControlLog(getTimeZone(), dcControlLog);
        MbusEventLog mbusLogs = new MbusEventLog(getTimeZone(), dcMbusEventLog);
        PowerFailureLog powerFailure = new PowerFailureLog(getTimeZone(), dcPowerFailure);

        eventList.addAll(standardEvents.getMeterEvents());
        eventList.addAll(fraudDetectionEvents.getMeterEvents());
        eventList.addAll(disconnectControl.getMeterEvents());
        eventList.addAll(mbusLogs.getMeterEvents());
        eventList.addAll(powerFailure.getMeterEvents());

        DataContainer dcMbusControlLog;
        MbusControlLog mbusControlLog;
        for (DeviceMapping mbusDevices : this.protocol.getMeterTopology().getMbusMeterMap()) {
            dcMbusControlLog = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getMbusControlLog(mbusDevices.getPhysicalAddress()).getObisCode()).getBuffer(fromCal, getToCalendar());
            mbusControlLog = new MbusControlLog(getTimeZone(), dcMbusControlLog);
            eventList.addAll(mbusControlLog.getMeterEvents());
        }
        return eventList;
    }
}
