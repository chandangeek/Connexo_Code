package com.energyict.protocolimplv2.dlms.acud.events.electric;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.DataStructure;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimplv2.dlms.acud.events.AbstractEvent;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class CommonEventLog extends AbstractEvent {

    public CommonEventLog(TimeZone timeZone, DataContainer dc) {
        super(dc, timeZone);
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId, DataStructure evStructure) {
        switch (eventId) {
            case 1:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.EOB_RESET, eventId, "EOB reset"));
                break;
            case 2:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.MANUAL_RESET, eventId, "Manual reset"));
                break;
            case 3:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.AUTO_RESET, eventId, "Auto reset"));
                break;
            case 4:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.ROLL_OVER_TO_ZERO, eventId, "Roll over to zero"));
                break;
            case 5:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.SELECTION_OF_INPUTS_SIGNALS, eventId, "Selection of input signals"));
                break;
            case 6:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OUTPUT_RELAY_CONTROL_SIGNALS_STATE_CHANGE, eventId, "State change of output relay control signals"));
                break;
            default:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Unknown eventcode: " + eventId));
        }
    }
}