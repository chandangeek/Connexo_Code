package com.energyict.protocolimpl.coronis.amco.rtm.core.alarmframe;

import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimpl.coronis.amco.rtm.RTM;
import com.energyict.protocolimpl.coronis.amco.rtm.core.EventStatusAndDescription;

import java.util.*;

/**
 * Copyrights EnergyICT
 * Date: 11-apr-2011
 * Time: 13:45:40
 */
public class AlarmFramePulseAndValveProfile extends AbstractAlarmFrame {

    public AlarmFramePulseAndValveProfile(RTM rtm, int optionalValue, int status, Date date) {
        super(rtm, optionalValue, status, date);
    }

    public List<MeterEvent> getMeterEvents() {
        List<MeterEvent> events = new ArrayList<MeterEvent>();

        if (isBitSet(0)) {
            events.add(new MeterEvent(date, 0, EventStatusAndDescription.EVENTCODE_LEAKAGE_RESIDUAL_A_END, "Residual leak on port A, leak flow measured = " + optionalValue));
        }
        if (isBitSet(1)) {
            events.add(new MeterEvent(date, 0, EventStatusAndDescription.EVENTCODE_LEAKAGE_EXTREME_END_A, "Extreme leak on port A, leak flow measured = " + optionalValue));
        }

        if (isBitSet(8)) {
            events.add(new MeterEvent(date, MeterEvent.BATTERY_VOLTAGE_LOW, EventStatusAndDescription.EVENTCODE_BATTERY_LOW,  "Low battery warning"));
        }
        if (isBitSet(9)) {
            events.add(new MeterEvent(date, MeterEvent.TAMPER, EventStatusAndDescription.EVENTCODE_WIRECUT_TAMPER_A, "Tamper detection on port A"));
        }
        if (isBitSet(17)) {
            events.add(new MeterEvent(date, 0, EventStatusAndDescription.EVENTCODE_VALVE_FAULT, "Valve communication error"));
        }
        return events;
    }
}