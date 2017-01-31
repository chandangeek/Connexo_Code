/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.nta.dsmr23.profiles;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.attributes.MbusClientAttributes;
import com.energyict.smartmeterprotocolimpl.common.topology.DeviceMapping;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractSmartNtaProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.eventhandling.DisconnectControlLog;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.eventhandling.EventsLog;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.eventhandling.FraudDetectionLog;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.eventhandling.MbusControlLog;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.eventhandling.MbusLog;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.eventhandling.PowerFailureLog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
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
        DataContainer dcPowerFailure = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getPowerFailureLogObject().getObisCode()).getBuffer(fromCal, getToCalendar());
        DataContainer dcFraudDetection = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getFraudDetectionLogObject().getObisCode()).getBuffer(fromCal, getToCalendar());
        DataContainer dcMbusEventLog = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getMbusEventLogObject().getObisCode()).getBuffer(fromCal, getToCalendar());

        EventsLog standardEvents = getStandardEventsLog(dcEvent);
        FraudDetectionLog fraudDetectionEvents = getFraudDetectionLog(dcFraudDetection);
        MbusLog mbusLogs = getMbusLog(dcMbusEventLog);
        PowerFailureLog powerFailure = getPowerFailureLog(dcPowerFailure);

        eventList.addAll(standardEvents.getMeterEvents());
        eventList.addAll(fraudDetectionEvents.getMeterEvents());
        eventList.addAll(mbusLogs.getMeterEvents());
        eventList.addAll(powerFailure.getMeterEvents());

        if (protocol.hasBreaker()) {
            DataContainer dcControlLog = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getControlLogObject().getObisCode()).getBuffer(fromCal, getToCalendar());
            DisconnectControlLog disconnectControl = getDisconnectControlLog(dcControlLog);
            eventList.addAll(disconnectControl.getMeterEvents());
        }

        DataContainer dcMbusControlLog;
        MbusControlLog mbusControlLog;
        for (DeviceMapping mbusDevices : this.protocol.getMeterTopology().getMbusMeterMap()) {
            if (protocol.hasBreaker()) {
            dcMbusControlLog = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getMbusControlLog(mbusDevices.getPhysicalAddress()-1).getObisCode()).getBuffer(fromCal, getToCalendar());
            mbusControlLog = new MbusControlLog(dcMbusControlLog, this.protocol.getDateTimeDeviationType());
            eventList.addAll(mbusControlLog.getMeterEvents());
            }

            UniversalObject mbusClient = getMeterConfig().getMbusClient(mbusDevices.getPhysicalAddress() - 1);
            long mbusStatus = getCosemObjectFactory().getGenericRead(new DLMSAttribute(mbusClient.getObisCode(), MbusClientAttributes.STATUS)).getValue();
            if ((mbusStatus & 0x10) == 0x10) {
                int eventId = Integer.parseInt("1" + (mbusDevices.getPhysicalAddress() - 1) + "5");
                eventList.add(mbusLogs.createNewMbusEventLogbookEvent(new Date(), eventId));
            }
        }
        return eventList;
    }

    protected PowerFailureLog getPowerFailureLog(DataContainer dcPowerFailure) {
        return new PowerFailureLog(dcPowerFailure, this.protocol.getDateTimeDeviationType());
    }

    protected MbusLog getMbusLog(DataContainer dcMbusEventLog) {
        return new MbusLog(dcMbusEventLog, this.protocol.getDateTimeDeviationType());
    }

    protected DisconnectControlLog getDisconnectControlLog(DataContainer dcControlLog) {
        return new DisconnectControlLog(dcControlLog, this.protocol.getDateTimeDeviationType());
    }

    protected FraudDetectionLog getFraudDetectionLog(DataContainer dcFraudDetection) {
        return new FraudDetectionLog(dcFraudDetection, this.protocol.getDateTimeDeviationType());
    }

    protected EventsLog getStandardEventsLog(DataContainer dcEvent) {
        return new EventsLog(dcEvent, this.protocol.getDateTimeDeviationType());
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
