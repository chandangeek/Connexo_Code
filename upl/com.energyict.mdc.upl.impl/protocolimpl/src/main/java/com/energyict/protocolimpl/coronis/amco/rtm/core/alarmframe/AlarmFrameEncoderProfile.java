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
public class AlarmFrameEncoderProfile extends AbstractAlarmFrame {

    public AlarmFrameEncoderProfile(RTM rtm, int optionalValue, int status, Date date) {
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
        if (isBitSet(2)) {
            events.add(new MeterEvent(date, 0, EventStatusAndDescription.EVENTCODE_LEAKAGE_RESIDUAL_B_END, "Residual leak on port B, leak flow measured = " + optionalValue));
        }
        if (isBitSet(3)) {
            events.add(new MeterEvent(date, 0, EventStatusAndDescription.EVENTCODE_LEAKAGE_EXTREME_END_B, "Extreme leak on port B, leak flow measured = " + optionalValue));
        }

        if (isBitSet(8)) {
            events.add(new MeterEvent(date, MeterEvent.BATTERY_VOLTAGE_LOW, EventStatusAndDescription.EVENTCODE_BATTERY_LOW, "Low battery warning"));
        }
        if (isBitSet(9)) {
            events.add(new MeterEvent(date, 0, EventStatusAndDescription.EVENTCODE_ENCODER_COMMUNICATION_FAULT_A, "Encoder communication error on port A"));
        }
        if (isBitSet(10)) {
            events.add(new MeterEvent(date, 0, EventStatusAndDescription.EVENTCODE_ENCODER_COMMUNICATION_FAULT_B, "Encoder communication error on port B"));
        }
        if (isBitSet(11)) {
            events.add(new MeterEvent(date, 0, EventStatusAndDescription.EVENTCODE_ENCODER_MISREAD_A, "Encoder reading error on port A"));
        }
        if (isBitSet(12)) {
            events.add(new MeterEvent(date, 0, EventStatusAndDescription.EVENTCODE_ENCODER_MISREAD_B, "Encoder reading error on port B"));
        }
        if (isBitSet(13)) {
            events.add(new MeterEvent(date, 0, EventStatusAndDescription.EVENTCODE_BACKFLOW_END_A, "Backflow detected on port A, detection flags: " + optionalValue));
        }
        if (isBitSet(14)) {
            events.add(new MeterEvent(date, 0, EventStatusAndDescription.EVENTCODE_BACKFLOW_END_B, "Backflow detected on port B, detection flags: " + optionalValue));
        }
        return events;
    }
}