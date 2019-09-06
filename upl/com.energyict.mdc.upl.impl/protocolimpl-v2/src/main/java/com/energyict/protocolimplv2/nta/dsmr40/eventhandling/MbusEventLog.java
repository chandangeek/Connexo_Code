package com.energyict.protocolimplv2.nta.dsmr40.eventhandling;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.axrdencoding.util.AXDRDateTimeDeviationType;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimplv2.nta.dsmr23.eventhandling.MbusLog;

import java.util.Date;
import java.util.List;

/**
 * Extends the original DSMR2.3 MbusLog with additional events for DSMR4.0
 */
public class MbusEventLog extends MbusLog {
    //77-99 - RESERVED FOR FUTURE USE
    private static final int EVENT_COMMUNICATION_ERROR_1 = 100;
    private static final int EVENT_COMMUNICATION_OK_1 = 101;
    private static final int EVENT_REPLACE_BATTERY_1= 102;
    private static final int EVENT_FRAUD_ATTEMPT_1 = 103;
    private static final int EVENT_CLOCK_ADJUSTED_1 = 104;
    private static final int EVENT_NEW_MBUS_DISCOVERED_1 = 105;
    private static final int EVENT_PERMANENT_ERROR_MBUS_1 = 106;
    //107-109 - RESERVED FOR FUTURE USE
    private static final int EVENT_COMMUNICATION_ERROR_2 = 110;
    private static final int EVENT_COMMUNICATION_OK_2 = 111;
    private static final int EVENT_REPLACE_BATTERY_2= 112;
    private static final int EVENT_FRAUD_ATTEMPT_2 = 113;
    private static final int EVENT_CLOCK_ADJUSTED_2 = 114;
    private static final int EVENT_NEW_MBUS_DISCOVERED_2 = 115;
    private static final int EVENT_PERMANENT_ERROR_MBUS_2 = 116;
    //117-119 - RESERVED FOR FUTURE USE
    private static final int EVENT_COMMUNICATION_ERROR_3 = 120;
    private static final int EVENT_COMMUNICATION_OK_3 = 121;
    private static final int EVENT_REPLACE_BATTERY_3= 122;
    private static final int EVENT_FRAUD_ATTEMPT_3 = 123;
    private static final int EVENT_CLOCK_ADJUSTED_3 = 124;
    private static final int EVENT_NEW_MBUS_DISCOVERED_3 = 125;
    private static final int EVENT_PERMANENT_ERROR_MBUS_3 = 126;
    //127-129 - RESERVED FOR FUTURE USE
    private static final int EVENT_COMMUNICATION_ERROR_4 = 130;
    private static final int EVENT_COMMUNICATION_OK_4 = 131;
    private static final int EVENT_REPLACE_BATTERY_4= 132;
    private static final int EVENT_FRAUD_ATTEMPT_4 = 133;
    private static final int EVENT_CLOCK_ADJUSTED_4 = 134;
    private static final int EVENT_NEW_MBUS_DISCOVERED_4 = 135;
    private static final int EVENT_PERMANENT_ERROR_MBUS_4 = 136;
    //137-159 - RESERVED FOR FUTURE USE
    //160-164 = RESERVED FOR BACKWARDS COMPATIBILITY
    //165-169 - RESERVED FOR FUTURE USE
    //170-174 = RESERVED FOR BACKWARDS COMPATIBILITY
    //175-179 - RESERVED FOR FUTURE USE
    //180-184 = RESERVED FOR BACKWARDS COMPATIBILITY
    //185-189 - RESERVED FOR FUTURE USE
    //190-194 = RESERVED FOR BACKWARDS COMPATIBILITY
    //195-229 - RESERVED FOR FUTURE USE
    //230-249 - MANUFACTURER SPECIFIC
    //250-254 - RESERVED FOR FUTURE USE

    public MbusEventLog(DataContainer dc, AXDRDateTimeDeviationType deviationType) {
        super(dc, deviationType);
    }
    public MbusEventLog(DataContainer dc, int mBusChannel) {
        super(dc, mBusChannel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void buildMeterEvent(final List<MeterEvent> meterEvents, final Date eventTimeStamp, final int eventId) {
        //select only event ids that correspond to mbus channel
        //channel 1: 100 .. 109
        //channel 2: 110 .. 119
        //channel 3: 120 .. 129
        //channel 4: 130 .. 139
        int clonedEventId = eventId;
        //if outside range, consider it an unknown event
        if (eventId != 255 && eventId < (90 + mBusChannel * 10) && eventId > (99 + mBusChannel * 10))
            clonedEventId = 0;
        switch(clonedEventId){
            // channel 1 events
            case EVENT_COMMUNICATION_ERROR_1: {meterEvents.add(createNewMbusEventLogbookEvent(eventTimeStamp, MeterEvent.COMMUNICATION_ERROR_MBUS, eventId, "Communication problem when reading the meter connected to channel 1 of the M-Bus"));}break;
            case EVENT_COMMUNICATION_OK_1:  {meterEvents.add(createNewMbusEventLogbookEvent(eventTimeStamp, MeterEvent.COMMUNICATION_OK_MBUS, eventId, "Communication with the M-Bus meter connected to channel 1 of the M-Bus is ok again"));}break;
            case EVENT_REPLACE_BATTERY_1:  {meterEvents.add(createNewMbusEventLogbookEvent(eventTimeStamp, MeterEvent.REPLACE_BATTERY_MBUS, eventId, "Battery must be exchanged due to the expected end of life time on channel 1"));}break;
            case EVENT_FRAUD_ATTEMPT_1:  {meterEvents.add(createNewMbusEventLogbookEvent(eventTimeStamp, MeterEvent.FRAUD_ATTEMPT_MBUS, eventId, "Fraud attempt has been registered on channel 1"));}break;
            case EVENT_CLOCK_ADJUSTED_1: {meterEvents.add(createNewMbusEventLogbookEvent(eventTimeStamp, MeterEvent.CLOCK_ADJUSTED_MBUS, eventId, "Clock has been adjusted on channel 1"));}break;
            case EVENT_NEW_MBUS_DISCOVERED_1 : {meterEvents.add(createNewMbusEventLogbookEvent(eventTimeStamp, MeterEvent.CONFIGURATIONCHANGE, eventId, "A new M-Bus Device has been detected on channel 1"));}break;
            case EVENT_PERMANENT_ERROR_MBUS_1 : {meterEvents.add(createNewMbusEventLogbookEvent(eventTimeStamp, MeterEvent.MEASUREMENT_SYSTEM_ERROR, eventId, "Permanent error on Mbus channel 1"));}break;
            //channel 2 events
            case EVENT_COMMUNICATION_ERROR_2: {meterEvents.add(createNewMbusEventLogbookEvent(eventTimeStamp, MeterEvent.COMMUNICATION_ERROR_MBUS, eventId, "Communication problem when reading the meter connected to channel 2 of the M-Bus"));}break;
            case EVENT_COMMUNICATION_OK_2:  {meterEvents.add(createNewMbusEventLogbookEvent(eventTimeStamp, MeterEvent.COMMUNICATION_OK_MBUS, eventId, "Communication with the M-Bus meter connected to channel 2 of the M-Bus is ok again"));}break;
            case EVENT_REPLACE_BATTERY_2:  {meterEvents.add(createNewMbusEventLogbookEvent(eventTimeStamp, MeterEvent.REPLACE_BATTERY_MBUS, eventId, "Battery must be exchanged due to the expected end of life time on channel 2"));}break;
            case EVENT_FRAUD_ATTEMPT_2:  {meterEvents.add(createNewMbusEventLogbookEvent(eventTimeStamp, MeterEvent.FRAUD_ATTEMPT_MBUS, eventId, "Fraud attempt has been registered on channel 2"));}break;
            case EVENT_CLOCK_ADJUSTED_2: {meterEvents.add(createNewMbusEventLogbookEvent(eventTimeStamp, MeterEvent.CLOCK_ADJUSTED_MBUS, eventId, "Clock has been adjusted on channel 2"));}break;
            case EVENT_NEW_MBUS_DISCOVERED_2 : {meterEvents.add(createNewMbusEventLogbookEvent(eventTimeStamp, MeterEvent.CONFIGURATIONCHANGE, eventId, "A new M-Bus Device has been detected on channel 2"));}break;
            case EVENT_PERMANENT_ERROR_MBUS_2 : {meterEvents.add(createNewMbusEventLogbookEvent(eventTimeStamp, MeterEvent.MEASUREMENT_SYSTEM_ERROR, eventId, "Permanent error on Mbus channel 2"));}break;
            //channel 3 events
            case EVENT_COMMUNICATION_ERROR_3: {meterEvents.add(createNewMbusEventLogbookEvent(eventTimeStamp, MeterEvent.COMMUNICATION_ERROR_MBUS, eventId, "Communication problem when reading the meter connected to channel 3 of the M-Bus"));}break;
            case EVENT_COMMUNICATION_OK_3:  {meterEvents.add(createNewMbusEventLogbookEvent(eventTimeStamp, MeterEvent.COMMUNICATION_OK_MBUS, eventId, "Communication with the M-Bus meter connected to channel 3 of the M-Bus is ok again"));}break;
            case EVENT_REPLACE_BATTERY_3:  {meterEvents.add(createNewMbusEventLogbookEvent(eventTimeStamp, MeterEvent.REPLACE_BATTERY_MBUS, eventId, "Battery must be exchanged due to the expected end of life time on channel 3"));}break;
            case EVENT_FRAUD_ATTEMPT_3:  {meterEvents.add(createNewMbusEventLogbookEvent(eventTimeStamp, MeterEvent.FRAUD_ATTEMPT_MBUS, eventId, "Fraud attempt has been registered on channel 3"));}break;
            case EVENT_CLOCK_ADJUSTED_3: {meterEvents.add(createNewMbusEventLogbookEvent(eventTimeStamp, MeterEvent.CLOCK_ADJUSTED_MBUS, eventId, "Clock has been adjusted on channel 3"));}break;
            case EVENT_NEW_MBUS_DISCOVERED_3 : {meterEvents.add(createNewMbusEventLogbookEvent(eventTimeStamp, MeterEvent.CONFIGURATIONCHANGE, eventId, "A new M-Bus Device has been detected on channel 3"));}break;
            case EVENT_PERMANENT_ERROR_MBUS_3 : {meterEvents.add(createNewMbusEventLogbookEvent(eventTimeStamp, MeterEvent.MEASUREMENT_SYSTEM_ERROR, eventId, "Permanent error on Mbus channel 3"));}break;
            //channel 4 events
            case EVENT_COMMUNICATION_ERROR_4: {meterEvents.add(createNewMbusEventLogbookEvent(eventTimeStamp, MeterEvent.COMMUNICATION_ERROR_MBUS, eventId, "Communication problem when reading the meter connected to channel 4 of the M-Bus"));}break;
            case EVENT_COMMUNICATION_OK_4:  {meterEvents.add(createNewMbusEventLogbookEvent(eventTimeStamp, MeterEvent.COMMUNICATION_OK_MBUS, eventId, "Communication with the M-Bus meter connected to channel 4 of the M-Bus is ok again"));}break;
            case EVENT_REPLACE_BATTERY_4:  {meterEvents.add(createNewMbusEventLogbookEvent(eventTimeStamp, MeterEvent.REPLACE_BATTERY_MBUS, eventId, "Battery must be exchanged due to the expected end of life time on channel 4"));}break;
            case EVENT_FRAUD_ATTEMPT_4:  {meterEvents.add(createNewMbusEventLogbookEvent(eventTimeStamp, MeterEvent.FRAUD_ATTEMPT_MBUS, eventId, "Fraud attempt has been registered on channel 4"));}break;
            case EVENT_CLOCK_ADJUSTED_4: {meterEvents.add(createNewMbusEventLogbookEvent(eventTimeStamp, MeterEvent.CLOCK_ADJUSTED_MBUS, eventId, "Clock has been adjusted on channel 4"));}break;
            case EVENT_NEW_MBUS_DISCOVERED_4 : {meterEvents.add(createNewMbusEventLogbookEvent(eventTimeStamp, MeterEvent.CONFIGURATIONCHANGE, eventId, "A new M-Bus Device has been detected on channel 4"));}break;
            case EVENT_PERMANENT_ERROR_MBUS_4 : {meterEvents.add(createNewMbusEventLogbookEvent(eventTimeStamp, MeterEvent.MEASUREMENT_SYSTEM_ERROR, eventId, "Permanent error on Mbus channel 4"));}break;
            default: super.buildMeterEvent(meterEvents, eventTimeStamp, eventId);
        }
    }
}
