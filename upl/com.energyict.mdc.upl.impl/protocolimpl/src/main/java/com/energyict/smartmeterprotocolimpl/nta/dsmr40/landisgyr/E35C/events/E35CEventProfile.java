package com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr.E35C.events;

import com.energyict.dlms.DataContainer;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractSmartNtaProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.eventhandling.DisconnectControlLog;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.eventhandling.PowerFailureLog;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.eventhandling.DSMR40EventProfile;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.eventhandling.FraudDetectionLog;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.eventhandling.StandardEventLog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;

/**
 * @author sva
 * @since 1/07/2015 - 16:10
 */
public class E35CEventProfile extends DSMR40EventProfile {

    private static final TimeZone UTC_TIME_ZONE = TimeZone.getTimeZone("UTC");

    public E35CEventProfile(AbstractSmartNtaProtocol protocol) {
        super(protocol);
    }

    @Override
    public List<MeterEvent> getEvents(Date fromDate) throws IOException {
        List<MeterEvent> eventList = new ArrayList<>();

        Calendar fromCal = ProtocolUtils.getCleanCalendar(UTC_TIME_ZONE);
        if (fromDate == null) {
            fromDate = ProtocolUtils.getClearLastMonthDate(UTC_TIME_ZONE);
        }
        fromCal.setTime(fromDate);
        protocol.getLogger().log(Level.INFO, "Reading EVENTS from meter with serialnumber " + protocol.getSerialNumber() + ".");
        DataContainer dcEvent = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getEventLogObject().getObisCode(), true).getBuffer(fromCal, getToCalendar());
        DataContainer dcPowerFailure = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getPowerFailureLogObject().getObisCode(), true).getBuffer(fromCal, getToCalendar());
        DataContainer dcFraudDetection = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getFraudDetectionLogObject().getObisCode(), true).getBuffer(fromCal, getToCalendar());

        StandardEventLog standardEvents = new E35CStandardEventLog(dcEvent, this.protocol.getTimeZone());
        FraudDetectionLog fraudDetectionEvents = new E35CFraudDetectionLog(dcFraudDetection, this.protocol.getTimeZone());
        PowerFailureLog powerFailure = new E35CPowerFailureLog(dcPowerFailure, this.protocol.getTimeZone());

        eventList.addAll(standardEvents.getMeterEvents());
        eventList.addAll(fraudDetectionEvents.getMeterEvents());
        eventList.addAll(powerFailure.getMeterEvents());

        DataContainer dcControlLog = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getControlLogObject().getObisCode(), true).getBuffer(fromCal, getToCalendar());
        DisconnectControlLog disconnectControl = new DisconnectControlLog(dcControlLog, this.protocol.getDateTimeDeviationType());
        eventList.addAll(disconnectControl.getMeterEvents());

        return eventList;
    }

    @Override
    protected Calendar getToCalendar() throws IOException {
        return ProtocolUtils.getCalendar(UTC_TIME_ZONE);
    }
}