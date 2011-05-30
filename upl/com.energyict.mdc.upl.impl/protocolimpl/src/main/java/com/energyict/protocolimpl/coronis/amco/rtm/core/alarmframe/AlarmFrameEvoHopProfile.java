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
public class AlarmFrameEvoHopProfile extends AbstractAlarmFrame {

    public AlarmFrameEvoHopProfile(RTM rtm, int optionalValue, int status, Date date) {
        super(rtm, optionalValue, status, date);
    }

    public List<MeterEvent> getMeterEvents() {
        List<MeterEvent> events = new ArrayList<MeterEvent>();
        if (isBitSet(8)) {
            events.add(new MeterEvent(date, MeterEvent.BATTERY_VOLTAGE_LOW, EventStatusAndDescription.EVENTCODE_BATTERY_LOW, "Low battery warning"));
        }
        return events;
    }
}