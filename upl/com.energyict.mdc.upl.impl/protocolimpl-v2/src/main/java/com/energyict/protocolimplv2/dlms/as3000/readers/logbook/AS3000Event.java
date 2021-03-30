package com.energyict.protocolimplv2.dlms.as3000.readers.logbook;

import com.energyict.protocol.MeterEvent;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public enum AS3000Event {

    NEW_INTERVAL_BECAUSE_POWER_DOWN(0x00000001, "New interval because of power-down", MeterEvent.POWERDOWN),
    NEW_INTERVAL_POWER_UP_AND_VARIABLE_CHANGED(0x00000002, "New interval because of power-up and variable changed by setting", MeterEvent.POWERUP),
    NEW_TIME_OR_DAYLIGHT_SWITCH(0x00000004, "New time/date or daylight savings switch", MeterEvent.DAYLIGHT_SAVING_TIME_ENABLED_OR_DISABLED),
    NEW_INTERVAL_BECAUSE_DEMAND_RESET_OR_PHASE12_OUTAGE(0x00000008, "New interval because of demand reset and 1-phase or 2-phase power outage", MeterEvent.MAXIMUM_DEMAND_EVENT),
    SEASON_CHANGE(0x00000010, "Season change, i.e. dst switch (VDEW) and system reverse energy flow", MeterEvent.SEASON_CHANGE),
    VALUES_NOT_RELIABLE(0x00000020, "Values not reliable", MeterEvent.MEASUREMENT_SYSTEM_ERROR),
    CARRY_OVER_ERROR(0x00000040, "Carry over error (copy of errcovr, syserr)", MeterEvent.CONFIGURATION_ERROR),
    FATAL_ERROR(0x00000080, "Fatal error ('OR' of some syserr flags)", MeterEvent.FATAL_ERROR),
    INPUT2_EVENT_DETECTED(0x00000100, "Input 2 event detected", MeterEvent.INPUT_EVENT),
    LOAD_PROFILE_INIT(0x00000200, "Load profile initialised", MeterEvent.LOADPROFILE_CLEARED),
    LOGBOOK_INIT(0x00000400, "Logbook initialised", MeterEvent.EVENT_LOG_CLEARED),
    INPUT1_DETECTED(0x00000800, "Input 1 event detected", MeterEvent.INPUT_EVENT),
    REVERSE_POWER_PHASE1OR2(0x00001000, "Reverse power in 1 or 2 phases detected", MeterEvent.REVERSE_POWER),
    ERROR_OR_WARN_OFF(0x00002000, "Error or warning off", MeterEvent.METER_ALARM_END),
    ERROR_OR_WARN_ON(0x00004000, "Error or warning on ('OR' of syserr and syswarn flags)", MeterEvent.METER_ALARM),
    VARIABLE_CHANGED_BY_SETTING(0x00008000, "Variable changed by setting", MeterEvent.CONFIGURATIONCHANGE),
    PHASE3_MISSING(0x00010000, "Phase L3 is missing", MeterEvent.MISSINGVOLTAGE_L3),
    PHASE2_MISSING(0x00020000, "Phase L2 is missing", MeterEvent.MISSINGVOLTAGE_L2),
    PHASE1_MISSING(0x00040000, "Phase L1 is missing", MeterEvent.MISSINGVOLTAGE_L1),
    CONTACTOR_SWITCHED_OFF(0x00080000, "Contactor switched off", MeterEvent.MANUAL_DISCONNECTION),
    WRONG_PASS_USED(0x00100000, "Wrong password was used", MeterEvent.N_TIMES_WRONG_PASSWORD),
    MAIN_COVER_OPENED(0x00200000, "Main cover is or was opened", MeterEvent.MAIN_COVER_TEMPER),
    TERMINAL_COVER_OPENED(0x00400000, "Terminal cover is or was opened", MeterEvent.METER_COVER_OPENED),
    CHANGE_OF_IMPULS(0x00800000, "Change of Impuls constant", MeterEvent.CHANGE_IMPULSE);
    // bit 0 to 7 are ignored since we do not know if they are status or events .... therefore no season, energy or demand reset events are implemented for now

    private final int eventId;
    private final String description;
    private final int cxoCode;


    AS3000Event(int eventId, String s, int cxoCode) {
        this.eventId = eventId;
        this.description = s;
        this.cxoCode = cxoCode;
    }

    private boolean is(int eventId) {
        return this.eventId == (eventId & this.eventId);
    }

    public String getDescription() {
        return this.description;
    }

    public static List<MeterEvent> buildMeterEvents(Date eventTimeStamp, int eventId) {
        List<MeterEvent> meterEvents = Arrays.stream(AS3000Event.values()).filter(f -> f.is(eventId)).map(f -> f.toMeterEvent(eventTimeStamp)).collect(Collectors.toList());
        if (meterEvents.isEmpty()) {
            return Arrays.asList(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Unknown eventcode: " + eventId));
        }
        return meterEvents;
    }

    private MeterEvent toMeterEvent(Date timeStamp) {
        return new MeterEvent((Date) timeStamp.clone(), this.cxoCode, this.eventId, this.description);
    }
}
