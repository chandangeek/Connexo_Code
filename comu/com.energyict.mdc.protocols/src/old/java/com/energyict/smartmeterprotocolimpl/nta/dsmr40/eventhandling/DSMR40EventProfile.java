/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.nta.dsmr40.eventhandling;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.cosem.attributes.MbusClientAttributes;
import com.energyict.smartmeterprotocolimpl.common.topology.DeviceMapping;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractSmartNtaProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.eventhandling.DisconnectControlLog;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.eventhandling.MbusControlLog;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.eventhandling.PowerFailureLog;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.profiles.EventProfile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
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
        DataContainer dcEvent = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getEventLogObject().getObisCode(), true).getBuffer(fromCal, getToCalendar());
        DataContainer dcPowerFailure = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getPowerFailureLogObject().getObisCode(), true).getBuffer(fromCal, getToCalendar());
        DataContainer dcFraudDetection = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getFraudDetectionLogObject().getObisCode(), true).getBuffer(fromCal, getToCalendar());
        DataContainer dcMbusEventLog = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getMbusEventLogObject().getObisCode(), true).getBuffer(fromCal, getToCalendar());

        StandardEventLog standardEvents = new StandardEventLog(dcEvent, this.protocol.getDateTimeDeviationType());
        FraudDetectionLog fraudDetectionEvents = new FraudDetectionLog(dcFraudDetection, this.protocol.getDateTimeDeviationType());
        MbusEventLog mbusLogs = new MbusEventLog(dcMbusEventLog, this.protocol.getDateTimeDeviationType());
        PowerFailureLog powerFailure = new PowerFailureLog(dcPowerFailure, this.protocol.getDateTimeDeviationType());

        eventList.addAll(standardEvents.getMeterEvents());
        eventList.addAll(fraudDetectionEvents.getMeterEvents());
        eventList.addAll(mbusLogs.getMeterEvents());
        eventList.addAll(powerFailure.getMeterEvents());

        if (protocol.hasBreaker()) {
            DataContainer dcControlLog = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getControlLogObject().getObisCode(), true).getBuffer(fromCal, getToCalendar());
            DisconnectControlLog disconnectControl = new DisconnectControlLog(dcControlLog, this.protocol.getDateTimeDeviationType());
            eventList.addAll(disconnectControl.getMeterEvents());
        }

        DataContainer dcMbusControlLog;
        MbusControlLog mbusControlLog;
        try {
            for (DeviceMapping mbusDevices : this.protocol.getMeterTopology().getMbusMeterMap()) {
                if (protocol.hasBreaker()) {
                dcMbusControlLog = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getMbusControlLog(mbusDevices.getPhysicalAddress() -1).getObisCode(), true).getBuffer(fromCal, getToCalendar());
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
        } catch (IOException e) {
            if(!e.getMessage().contains("DLMSConfig, getMbusControlLog, not found in objectlist (IOL)")){
                throw e;
            } // else it just means no MbusDevices are installed ...
        }
        return eventList;
    }
}
