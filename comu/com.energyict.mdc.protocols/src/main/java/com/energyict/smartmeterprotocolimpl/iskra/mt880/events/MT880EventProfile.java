/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.iskra.mt880.events;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.smartmeterprotocolimpl.iskra.mt880.IskraMT880;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * @author sva
 * @since 14/10/13 - 10:08
 */
public class MT880EventProfile {

    private static final ObisCode STANDARD_EVENT_LOG_OBIS = ObisCode.fromString("0.0.99.98.0.255");
    private static final ObisCode FRAUD_DETECTION_EVENT_LOG_OBIS = ObisCode.fromString("0.0.99.98.1.255");
    private static final ObisCode POWER_QUALITY_EVENT_LOG_OBIS = ObisCode.fromString("0.0.99.98.4.255");
    private static final ObisCode POWER_DOWN_EVENT_LOG_OBIS = ObisCode.fromString("0.0.99.98.5.255");
    private static final ObisCode COMMUNICATION_EVENT_LOG_OBIS = ObisCode.fromString("0.0.99.98.6.255");
    private static final ObisCode MCO_TCO_EVENT_LOG_OBIS = ObisCode.fromString("0.0.99.98.7.255");
    private static final ObisCode MAGNETIC_TAMPER_EVENT_LOG_OBIS = ObisCode.fromString("0.0.99.98.8.255");

    private static final ObisCode POWER_FAILURE_EVENT_LOG_OBIS = ObisCode.fromString("1.0.99.97.0.255");

    private IskraMT880 protocol;

    public MT880EventProfile(IskraMT880 protocol) {
        this.protocol = protocol;
    }

    public List<MeterEvent> getEvents(Date fromDate) throws IOException {
        List<MeterEvent> eventList = new ArrayList<MeterEvent>();

        Calendar fromCal = ProtocolUtils.getCleanCalendar(getTimeZone());
        if (fromDate == null) {
            fromDate = ProtocolUtils.getClearLastMonthDate(getTimeZone());
        }
        fromCal.setTime(fromDate);
        DataContainer dcEvent = getCosemObjectFactory().getProfileGeneric(STANDARD_EVENT_LOG_OBIS).getBuffer(fromCal, getToCalendar());
        DataContainer dcFraudDetection = getCosemObjectFactory().getProfileGeneric(FRAUD_DETECTION_EVENT_LOG_OBIS).getBuffer(fromCal, getToCalendar());
        DataContainer dcPowerQuality = getCosemObjectFactory().getProfileGeneric(POWER_QUALITY_EVENT_LOG_OBIS).getBuffer(fromCal, getToCalendar());
        DataContainer dcPowerDown = getCosemObjectFactory().getProfileGeneric(POWER_DOWN_EVENT_LOG_OBIS).getBuffer(fromCal, getToCalendar());
        DataContainer dcCommunication = getCosemObjectFactory().getProfileGeneric(COMMUNICATION_EVENT_LOG_OBIS).getBuffer(fromCal, getToCalendar());
        DataContainer dcMcoTco = getCosemObjectFactory().getProfileGeneric(MCO_TCO_EVENT_LOG_OBIS).getBuffer(fromCal, getToCalendar());
        DataContainer dcMagneticTamper = getCosemObjectFactory().getProfileGeneric(MAGNETIC_TAMPER_EVENT_LOG_OBIS).getBuffer(fromCal, getToCalendar());
        DataContainer dcPowerFailure = getCosemObjectFactory().getProfileGeneric(POWER_FAILURE_EVENT_LOG_OBIS).getBuffer(fromCal, getToCalendar());

        EventsLog standardEvents = new EventsLog(dcEvent, this.protocol.getDateTimeDeviationType(), EventLogbookId.StandardEventLog);
        EventsLog fraudDetectionEvents = new EventsLog(dcFraudDetection, this.protocol.getDateTimeDeviationType(), EventLogbookId.FraudDetectionLog);
        EventsLog powerQualityEvents = new EventsLog(dcPowerQuality, this.protocol.getDateTimeDeviationType(), EventLogbookId.PowerQualityLog);
        EventsLog powerDownEvents = new EventsLog(dcPowerDown, this.protocol.getDateTimeDeviationType(), EventLogbookId.PowerDownEventLog);
        EventsLog communicationEvents = new EventsLog(dcCommunication, this.protocol.getDateTimeDeviationType(), EventLogbookId.CommunicationEventLog);
        EventsLog mcoTcoEvents = new EventsLog(dcMcoTco, this.protocol.getDateTimeDeviationType(), EventLogbookId.McoTcoEventLog);
        EventsLog magneticTamperEvents = new EventsLog(dcMagneticTamper, this.protocol.getDateTimeDeviationType(), EventLogbookId.MagneticTamperEventLog);
        PowerFailureLog powerFailureEvents = new PowerFailureLog(dcPowerFailure, this.protocol.getDateTimeDeviationType(), EventLogbookId.PowerFailureEventLog);

        eventList.addAll(standardEvents.getMeterEvents());
        eventList.addAll(fraudDetectionEvents.getMeterEvents());
        eventList.addAll(powerQualityEvents.getMeterEvents());
        eventList.addAll(powerDownEvents.getMeterEvents());
        eventList.addAll(communicationEvents.getMeterEvents());
        eventList.addAll(mcoTcoEvents.getMeterEvents());
        eventList.addAll(magneticTamperEvents.getMeterEvents());
        eventList.addAll(powerFailureEvents.getMeterEvents());
        return eventList;
    }

    protected CosemObjectFactory getCosemObjectFactory() {
        return this.protocol.getDlmsSession().getCosemObjectFactory();
    }

    protected TimeZone getTimeZone() throws IOException {
        return this.protocol.getTimeZone();
    }

    protected Calendar getToCalendar() throws IOException {
        return ProtocolUtils.getCalendar(getTimeZone());
    }
}
