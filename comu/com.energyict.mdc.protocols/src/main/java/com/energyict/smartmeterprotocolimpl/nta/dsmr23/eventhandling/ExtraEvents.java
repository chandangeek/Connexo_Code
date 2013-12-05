package com.energyict.smartmeterprotocolimpl.nta.dsmr23.eventhandling;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import java.util.Date;
import java.util.HashMap;

/**
 * Configuration of extra events that are defined after NTA implementation
 *
 * @author gna
 */
public class ExtraEvents {

    public static HashMap<Integer, String[]> extraEvents = new HashMap<Integer, String[]>();

    private static int CODE = 0;
    private static int TEXT = 1;

    static {
        buildExtraEvents();
    }

    private static void buildExtraEvents() {
        extraEvents.put(new Integer(230), new String[]{
                Integer.toString(MeterEvent.FATAL_ERROR), "Fatal Error"});
        extraEvents.put(new Integer(231), new String[]{
                Integer.toString(MeterEvent.BILLING_ACTION), "Billing Reset"});
        extraEvents.put(new Integer(232), new String[]{
                Integer.toString(MeterEvent.PHASE_FAILURE),
                "Power failure on L1"});
        extraEvents.put(new Integer(233), new String[]{
                Integer.toString(MeterEvent.PHASE_FAILURE),
                "Power failure on L2"});
        extraEvents.put(new Integer(234), new String[]{
                Integer.toString(MeterEvent.PHASE_FAILURE),
                "Power failure on L3"});
        extraEvents.put(new Integer(235), new String[]{
                Integer.toString(MeterEvent.POWERUP), "Power returned on L1"});
        extraEvents.put(new Integer(236), new String[]{
                Integer.toString(MeterEvent.POWERUP), "Power returned on L2"});
        extraEvents.put(new Integer(237), new String[]{
                Integer.toString(MeterEvent.POWERUP), "Power returned on L3"});
        extraEvents.put(new Integer(238), new String[]{
                Integer.toString(MeterEvent.OTHER),
                "Indicates 'No Connection' timeout"});
        extraEvents.put(new Integer(239), new String[]{
                Integer.toString(MeterEvent.SETCLOCK),
                "Large Clock adjustment"});
        extraEvents.put(new Integer(240), new String[]{
                Integer.toString(MeterEvent.OTHER),
                "Device Reset"});
    }

    /**
     * Creates a meterEvent with the given eventTimeStamp
     *
     * @param eventTimeStamp - the time the event occurred
     * @param eventId        - the code returned from the device
     * @return a specific meterEvent
     */
    public static MeterEvent getExtraEvent(Date eventTimeStamp, int eventId) {
        return new MeterEvent(eventTimeStamp, Integer.parseInt(extraEvents
                .get(new Integer(eventId))[CODE]), eventId, (extraEvents
                .get(new Integer(eventId))[TEXT]), EventLogbookId.UnknownEventLogbook.eventLogId(), 0);
    }
}
