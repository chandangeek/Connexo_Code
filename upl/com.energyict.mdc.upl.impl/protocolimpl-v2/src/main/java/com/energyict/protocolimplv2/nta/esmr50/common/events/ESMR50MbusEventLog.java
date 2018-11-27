package com.energyict.protocolimplv2.nta.esmr50.common.events;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.axrdencoding.util.AXDRDateTimeDeviationType;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimplv2.nta.dsmr40.eventhandling.MbusEventLog;

import java.util.Date;
import java.util.List;

/**
 * Extends the original DSMR4.0 MbusLog with additional events for ESMR5.0
 */

//TODO: md: some events specific for slaved devices overriden here with generic event codes; check correctness
public class ESMR50MbusEventLog extends MbusEventLog {

    private static final int EVENT_BATTERY_LOW = 100;
    private static final int EVENT_BATTERY_CONSUMPTION_HIGH = 101;
    private static final int EVENT_REVERSE_FLOW = 102;
    private static final int EVENT_TAMPER_P2 = 103;
    private static final int EVENT_TAMPER_P0 = 104;
    private static final int EVENT_TAMPER_CASE = 105;
    private static final int EVENT_TAMPER_MAGNETIC = 106;
    private static final int EVENT_TEMP_OUT_OF_RANGE = 107;
    private static final int EVENT_CLOCK_SYNC_ERROR = 108;
    private static final int EVENT_SW_ERROR = 109;
    private static final int EVENT_WATCHDOG_ERROR = 110;
    private static final int EVENT_SYSTEM_HW_ERROR = 111;
    private static final int EVENT_CFG_CALIBRATION_ERROR = 112;
    private static final int EVENT_HIGH_FLOW_ERROR = 113;
    private static final int EVENT_TEMP_SENSOR_ERROR = 114;
    private static final int EVENT_BINDING_FLAG = 115;
    private static final int EVENT_FIRMWARE_UPGRADE_SUCCESSFUL = 116;
    private static final int EVENT_FIRMWARE_UPGRADE_UNSUCCESSFUL = 117;
    private static final int EVENT_FUAK_CHANGE_SUCCESSFUL = 118;

    private static final int EVENT_MBUS_COMMUNCATION_ERROR = 132;
    private static final int EVENT_MBUS_COMMUNCATION_ERROR_RESOLVED = 133;
    private static final int EVENT_MBUS_SECURITY_ERROR = 134;
    private static final int EVENT_MBUS_SECURITY_ERROR_RESOLVED = 135;
    private static final int EVENT_NEW_MBUS_DISCOVERED = 136;

    public ESMR50MbusEventLog(DataContainer dc, AXDRDateTimeDeviationType deviationType) {
        super(dc, deviationType);
    }

    private int mBusChannel;
    public ESMR50MbusEventLog(DataContainer dc, int mBusChannel) {
        super(dc);
        this.mBusChannel = mBusChannel;
    }

    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {

        //select only event ids that correspond to mbus channel
        //channel 1: 100 .. 109
        //channel 2: 110 .. 119
        //channel 3: 120 .. 129
        //channel 4: 130 .. 139
        int clonedEventId = eventId;
        //if outside range, consider it an unknown event
        if (eventId != 255 && eventId < (90 + mBusChannel * 10) && eventId > (99 + mBusChannel * 10))
            clonedEventId = 0;

        switch (clonedEventId) {
            case EVENT_BATTERY_LOW:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Battery low"));
                break;
            case EVENT_BATTERY_CONSUMPTION_HIGH:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Battery consumption high"));
                break;
            case EVENT_REVERSE_FLOW:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Reverse flow"));
                break;
            case EVENT_TAMPER_P2:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Tamper P2"));
                break;
            case EVENT_TAMPER_P0:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Tamper P0"));
                break;
            case EVENT_TAMPER_CASE:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Tamper case"));
                break;
            case EVENT_TAMPER_MAGNETIC:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Tamper magnetic"));
                break;
            case EVENT_TEMP_OUT_OF_RANGE:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Temp out of range"));
                break;
            case EVENT_CLOCK_SYNC_ERROR:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.CLOCK_INVALID, eventId, "Clock sync error"));
                break;
            case EVENT_SW_ERROR:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Software checksum error"));
                break;
            case EVENT_WATCHDOG_ERROR:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.WATCHDOG_ERROR, eventId, "Watchdog error"));
                break;
            case EVENT_SYSTEM_HW_ERROR:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Selfcheck failure"));
                break;
            case EVENT_CFG_CALIBRATION_ERROR:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "CFG Calibration error"));
                break;
            case EVENT_HIGH_FLOW_ERROR:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Flow higher than Qmax (High flow)"));
                break;
            case EVENT_TEMP_SENSOR_ERROR:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Temp sensor error"));
                break;
            case EVENT_BINDING_FLAG:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Binding flag"));
                break;
            case EVENT_FIRMWARE_UPGRADE_SUCCESSFUL:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "FW upgrade successful"));
                break;
            case EVENT_FIRMWARE_UPGRADE_UNSUCCESSFUL:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "FW upgrade unsuccessful"));
                break;
            case EVENT_FUAK_CHANGE_SUCCESSFUL:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "FUAK change successful"));
                break;
            case EVENT_MBUS_COMMUNCATION_ERROR:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.COMMUNICATION_ERROR_MBUS, eventId, "M-Bus communication error"));
                break;
            case EVENT_MBUS_COMMUNCATION_ERROR_RESOLVED:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.COMMUNICATION_OK_MBUS, eventId, "M-Bus communication error resolved"));
                break;
            case EVENT_MBUS_SECURITY_ERROR:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.COMMUNICATION_ERROR_MBUS, eventId, "M-Bus security error"));
                break;
            case EVENT_MBUS_SECURITY_ERROR_RESOLVED:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.COMMUNICATION_OK_MBUS, eventId, "M-Bus security error resolved"));
                break;
            case EVENT_NEW_MBUS_DISCOVERED:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "New M-Bus device discovered"));
                break;
            default:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Unknown eventcode: " + eventId));
        }
    }
}
