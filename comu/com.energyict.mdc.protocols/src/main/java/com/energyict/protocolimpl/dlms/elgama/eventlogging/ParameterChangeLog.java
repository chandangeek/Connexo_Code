/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.elgama.eventlogging;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.dlms.DataContainer;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class ParameterChangeLog extends AbstractEvent {

    private static final int METERING_DATA_PARAMETER_CHANGE = 0x01;
    private static final int COMM_PARAMETER_CHANGE = 0x02;
    private static final int COMMANDS_EXECUTION = 0x04;
    private static final int ID_PARAMETER_CHANGE = 0x08;
    private static final int UI_PARAMETER_CHANGE = 0x10;
    private static final int TARIFF_PARAMETER_CHANGE = 0x20;
    private static final int CLOCK_PARAMETER_CHANGE = 0x40;

    public ParameterChangeLog(TimeZone timeZone, DataContainer dc) {
        super(dc, timeZone);
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {
        previousSize = meterEvents.size();
        this.meterEvents = meterEvents;

        if ((eventId & METERING_DATA_PARAMETER_CHANGE) != 0) {
            eventTimeStamp = fixStamp(eventTimeStamp);
            meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.CONFIGURATIONCHANGE, METERING_DATA_PARAMETER_CHANGE, "Change of metering data parameters"));
        }
        if ((eventId & COMM_PARAMETER_CHANGE) != 0) {
            eventTimeStamp = fixStamp(eventTimeStamp);
            meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.CONFIGURATIONCHANGE, COMM_PARAMETER_CHANGE, "Change of communication interface parameters"));
        }
        if ((eventId & COMMANDS_EXECUTION) != 0) {
            eventTimeStamp = fixStamp(eventTimeStamp);
            meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, COMMANDS_EXECUTION, "Execution of commands"));
        }
        if ((eventId & ID_PARAMETER_CHANGE) != 0) {
            eventTimeStamp = fixStamp(eventTimeStamp);
            meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.CONFIGURATIONCHANGE, ID_PARAMETER_CHANGE, "Change of identification parameters"));
        }
        if ((eventId & UI_PARAMETER_CHANGE) != 0) {
            eventTimeStamp = fixStamp(eventTimeStamp);
            meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.CONFIGURATIONCHANGE, UI_PARAMETER_CHANGE, "Change of user interface parameters"));
        }
        if ((eventId & TARIFF_PARAMETER_CHANGE) != 0) {
            eventTimeStamp = fixStamp(eventTimeStamp);
            meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.CONFIGURATIONCHANGE, TARIFF_PARAMETER_CHANGE, "Change of tariff parameters"));
        }
        if ((eventId & CLOCK_PARAMETER_CHANGE) != 0) {
            eventTimeStamp = fixStamp(eventTimeStamp);
            meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.CONFIGURATIONCHANGE, CLOCK_PARAMETER_CHANGE, "Change of clock parameters"));
        }
        if (!anEventWasAdded()) {
            meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Unknown eventcode:" + eventId));
        }
    }
}