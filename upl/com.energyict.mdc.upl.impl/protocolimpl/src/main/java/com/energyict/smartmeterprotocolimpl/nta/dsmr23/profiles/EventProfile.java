package com.energyict.smartmeterprotocolimpl.nta.dsmr23.profiles;

import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.protocol.*;
import com.energyict.smartmeterprotocolimpl.common.topology.DeviceMapping;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractSmartNtaProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.eventhandling.*;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * TODO, we should think about how we can add the MbusControlLog events to the MbusDevices instead of the MASTER!!!
 *
 * @author gna
 */
public class EventProfile {

    protected AbstractSmartNtaProtocol protocol;

    public EventProfile(AbstractSmartNtaProtocol protocol) {
        this.protocol = protocol;
    }

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

        EventsLog standardEvents = new EventsLog(getTimeZone(), dcEvent);
        FraudDetectionLog fraudDetectionEvents = new FraudDetectionLog(getTimeZone(), dcFraudDetection);
        DisconnectControlLog disconnectControl = new DisconnectControlLog(getTimeZone(), dcControlLog);
        MbusLog mbusLogs = new MbusLog(getTimeZone(), dcMbusEventLog);
        PowerFailureLog powerFailure = new PowerFailureLog(getTimeZone(), dcPowerFailure);

        eventList.addAll(standardEvents.getMeterEvents());
        eventList.addAll(fraudDetectionEvents.getMeterEvents());
        eventList.addAll(disconnectControl.getMeterEvents());
        eventList.addAll(mbusLogs.getMeterEvents());
        eventList.addAll(powerFailure.getMeterEvents());

        DataContainer dcMbusControlLog;
        MbusControlLog mbusControlLog;
        for (DeviceMapping mbusDevices : this.protocol.getMeterTopology().getMbusMeterMap()) {
            dcMbusControlLog = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getMbusControlLog(mbusDevices.getPhysicalAddress()-1).getObisCode()).getBuffer(fromCal, getToCalendar());
            mbusControlLog = new MbusControlLog(getTimeZone(), dcMbusControlLog);
            eventList.addAll(mbusControlLog.getMeterEvents());
        }
        return eventList;
    }

    protected CosemObjectFactory getCosemObjectFactory() {
        return this.protocol.getDlmsSession().getCosemObjectFactory();
    }

    protected DLMSMeterConfig getMeterConfig() {
        return this.protocol.getDlmsSession().getMeterConfig();
    }

    protected TimeZone getTimeZone() throws IOException {
        return this.protocol.getTimeZone();
    }

    protected Calendar getToCalendar() throws IOException {
        return ProtocolUtils.getCalendar(getTimeZone());
    }
}
