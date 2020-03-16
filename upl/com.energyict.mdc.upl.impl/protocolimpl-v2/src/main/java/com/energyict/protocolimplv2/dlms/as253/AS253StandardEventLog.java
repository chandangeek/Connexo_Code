package com.energyict.protocolimplv2.dlms.as253;

import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MeterProtocolEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AS253StandardEventLog {

    private List<AS253LogBookFactory.BasicEvent> basicEvents;

    AS253StandardEventLog(List<AS253LogBookFactory.BasicEvent> basicEvents){
        this.basicEvents = basicEvents;
    }

    List<MeterProtocolEvent> buildMeterEvent() throws IOException {
        List<MeterEvent> meterEvents = new ArrayList<>();
        Date eventTimeStamp;
        int eventId;
        for(AS253LogBookFactory.BasicEvent basicEvent : basicEvents){
            eventTimeStamp = basicEvent.getEventTime();
            eventId = basicEvent.getEventCode();
            switch (eventId) {
                case 1:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.POWERDOWN, eventId, "Power down"));
                    break;
                case 2:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.POWERUP, eventId, "Power up"));
                    break;
                case 3:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.SETCLOCK_BEFORE, eventId, "Time changed (Old time)"));
                    break;
                case 4:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.SETCLOCK_AFTER, eventId, "Time changed (New time)"));
                    break;
                case 7:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.ACCESS_READ, eventId, "End device accessed for read"));
                    break;
                case 8:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.ACCESS_WRITE, eventId, "End device accessed for write"));
                    break;
                case 9:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.PROCEDURE_INVOKED, eventId, "Procedure invoked"));
                    break;
                case 10:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.TABLE_WRITTEN, eventId, "Table written to"));
                    break;
                case 11:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.DEVICE_PROGRAMMED, eventId, "End device programmed"));
                    break;
                case 12:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.COMM_TERMINATED_NORMAL, eventId, "Communication was terminated normally"));
                    break;
                case 13:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.COMM_TERMINATED_ABNORMAL, eventId, "Communication was terminated abnormally"));
                    break;
                case 14:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.RESET_LIST_POINTER, eventId, "Reset list pointer"));
                    break;
                case 15:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.UPDATE_LIST_POINTER, eventId, "Update list pointer"));
                    break;
                case 16:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.HISTORY_LOG_CLEARED, eventId, "History log cleared"));
                    break;
                case 17:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.HISTORY_LOG_POINTER, eventId, "History log pointer updated"));
                    break;
                case 18:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.EVENT_LOG_CLEARED, eventId, "Event log cleared"));
                    break;
                case 19:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.EVENT_LOG_POINTER, eventId, "Event log pointer updated"));
                    break;
                case 20:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.MAXIMUM_DEMAND_RESET, eventId, "Maximum demand reset"));
                    break;
                case 21:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.SELF_READ, eventId, "Self read occurred"));
                    break;
                case 22:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.DAYLIGHT_SAVING_TIME_ON, eventId, "Daylight savings time on"));
                    break;
                case 23:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.DAYLIGHT_SAVING_TIME_OFF, eventId, "Daylight savings time off"));
                    break;
                case 24:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.SEASON_CHANGE, eventId, "Season changed"));
                    break;
                case 25:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.RATE_CHANGE, eventId, "Rate changed"));
                    break;
                case 26:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.SPECIAL_SCHEDULE, eventId, "Special schedule activation"));
                    break;
                case 27:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.TIER_SWITCH, eventId, "Tier switch changed"));
                    break;
                case 28:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.PENDING_TABLE_ACTIVATION, eventId, "Pending table activation"));
                    break;
                case 29:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.PENDING_TALE_CLEAR, eventId, "Pending table clear"));
                    break;
                case 30:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.METERING_MODE_START, eventId, "Metering mode started"));
                    break;
                case 31:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.METERING_MODE_STOP, eventId, "Metering mode stopped"));
                    break;
                case 32:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.TEST_MODE_START, eventId, "Test mode started"));
                    break;
                case 33:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.TEST_MODE_STOP, eventId, "Test mode stopped"));
                    break;
                case 34:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.METER_SHOP_START, eventId, "Meter shop started"));
                    break;
                case 35:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.METER_SHOP_STOP, eventId, "Meter shop stopped"));
                    break;
                case 36:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.CONFIGURATION_ERROR, eventId, "Configuration error detected"));
                    break;
                case 37:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.SELF_CHECK_ERROR, eventId, "Self check error detected"));
                    break;
                case 38:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.RAM_FAILURE, eventId, "RAM failure detected"));
                    break;
                case 39:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.ROM_FAILURE, eventId, "ROM failure detected"));
                    break;
                case 40:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.NON_VOLATILE_MEMORY_FAILURE, eventId, "Non volatile memory failure detected"));
                    break;
                case 41:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.CLOCK_ERROR, eventId, "Clock error detected"));
                    break;
                case 42:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.MEASUREMENT_SYSTEM_ERROR, eventId, "Measurement system error detected"));
                    break;
                case 43:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.BATTERY_VOLTAGE_LOW, eventId, "Low battery detected"));
                    break;
                case 44:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.LOW_LOSS_POTENTIAL, eventId, "Low loss potential detected"));
                    break;
                case 45:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.DEMAND_OVERLOAD, eventId, "Demand overload detected"));
                    break;
                case 46:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.POWER_FAILURE, eventId, "Power failure detected"));
                    break;
                case 47:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.TAMPER, eventId, "Tamper detected"));
                    break;
                case 48:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.REVERSE_ROTATION, eventId, "Reverse rotation detected"));
                    break;
                case 2048:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.ENTER_TIER, eventId, "Enter tier override"));
                    break;
                case 2049:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.EXIT_TIER, eventId, "Exit tier override"));
                    break;
                case 2050:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.TERMINAL_COVER_TEMPER, eventId, "Terminal cover tamper"));
                    break;
                case 2051:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.MAIN_COVER_TEMPER, eventId, "Main cover tamper"));
                    break;
                case 2052:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.EXTERNAL_EVENT, eventId, "External event 0"));
                    break;
                case 2053:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.EXTERNAL_EVENT, eventId, "External event 1"));
                    break;
                case 2054:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.EXTERNAL_EVENT, eventId, "External event 2"));
                    break;
                case 2055:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.EXTERNAL_EVENT, eventId, "External event 3"));
                    break;
                case 2056:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.PHASE_A_OFF, eventId, "Phase a off"));
                    break;
                case 2057:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.PHASE_A_ON, eventId, "Phase a on"));
                    break;
                case 2058:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.PHASE_B_OFF, eventId, "Phase b off"));
                    break;
                case 2059:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.PHASE_B_ON, eventId, "Phase b on"));
                    break;
                case 2060:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.PHASE_C_OFF, eventId, "Phase c off"));
                    break;
                case 2061:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.PHASE_C_ON, eventId, "Phase c on"));
                    break;
                case 2062:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.REMOTE_FLASH_FAILURE, eventId, "Remote flash failed"));
                    break;
                case 2063:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.RWP, eventId, "Read without power event"));
                    break;
                default:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Unknown eventcode: " + eventId));
            }
        }
        return MeterEvent.mapMeterEventsToMeterProtocolEvents(meterEvents);
    }
}