package com.energyict.protocolimpl.utils;

import com.energyict.mdw.core.RtuEvent;
import com.energyict.protocol.MeterEvent;

import java.util.*;

/**
 * Copyrights EnergyICT
 * Date: 4/05/11
 * Time: 10:00
 */
public class MeterEventUtils {

    public static MeterEvent changeEventDate(MeterEvent event, Date newEventDate) {
        return new MeterEvent(newEventDate, event.getEiCode(), event.getProtocolCode(), event.getMessage());
    }

    public static MeterEvent changeEventEisCode(MeterEvent event, int newEisCode) {
        return new MeterEvent(event.getTime(), newEisCode, event.getProtocolCode(), event.getMessage());
    }

    public static MeterEvent changeEventDeviceCode(MeterEvent event, int newDeviceCode) {
        return new MeterEvent(event.getTime(), event.getEiCode(), newDeviceCode, event.getMessage());
    }

    public static MeterEvent changeEventMessage(MeterEvent event, String newMessage) {
        return new MeterEvent(event.getTime(), event.getEiCode(), event.getProtocolCode(), newMessage);
    }

    public static MeterEvent appendToEventMessage(MeterEvent event, String textToAppend) {
        return new MeterEvent(event.getTime(), event.getEiCode(), event.getProtocolCode(), event.getMessage() + textToAppend);
    }

    public static List<MeterEvent> convertRtuEventToMeterEvent(List<RtuEvent> rtuEvents) {
        ArrayList<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
        for (RtuEvent event : rtuEvents) {
            meterEvents.add(new MeterEvent(event.getDate(), event.getCode(), event.getDeviceCode(), event.getMessage()));
        }
        return meterEvents;

    }
}
