/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.nta.dsmr50.elster.am540.events;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.dlms.DataContainer;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractSmartNtaProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.eventhandling.PowerFailureLog;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.eventhandling.DSMR40EventProfile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

public class AM540EventProfile extends DSMR40EventProfile {

    public AM540EventProfile(AbstractSmartNtaProtocol protocol) {
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

        DataContainer dcStandardEvents = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getEventLogObject().getObisCode(), true).getBuffer(fromCal, getToCalendar());
        DataContainer dcPowerFailure = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getPowerFailureLogObject().getObisCode(), true).getBuffer(fromCal, getToCalendar());
        DataContainer dcFraudDetection = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getFraudDetectionLogObject().getObisCode(), true).getBuffer(fromCal, getToCalendar());
        //DataContainer dcCommunicationSession = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getCommunicationSessionLogObject().getObisCode(), true).getBuffer(fromCal, getToCalendar());
        //DataContainer dcMBusEvents = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getMbusEventLogObject().getObisCode(), true).getBuffer(fromCal, getToCalendar());

        AM540StandardEventLog standardEvents = new AM540StandardEventLog(dcStandardEvents, this.protocol.getDateTimeDeviationType());
        AM540FraudDetectionLog fraudDetectionEvents = new AM540FraudDetectionLog(dcFraudDetection, this.protocol.getDateTimeDeviationType());
        PowerFailureLog powerFailureEvents = new PowerFailureLog(dcPowerFailure, this.protocol.getDateTimeDeviationType());
        //AM540CommunicationSessionLog communicationSessionEvents = new AM540CommunicationSessionLog(dcCommunicationSession, this.protocol.getDateTimeDeviationType());
        //AM540MBusLog mbusEvents = new AM540MBusLog(dcMBusEvents, this.protocol.getDateTimeDeviationType());

        eventList.addAll(standardEvents.getMeterEvents());
        eventList.addAll(fraudDetectionEvents.getMeterEvents());
        eventList.addAll(powerFailureEvents.getMeterEvents());
        //eventList.addAll(communicationSessionEvents.getMeterEvents());
        //eventList.addAll(mbusEvents.getMeterEvents());

        return eventList;
    }
}