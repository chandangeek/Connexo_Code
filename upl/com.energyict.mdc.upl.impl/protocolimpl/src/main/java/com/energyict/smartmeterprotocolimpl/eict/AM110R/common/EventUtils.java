package com.energyict.smartmeterprotocolimpl.eict.AM110R.common;

import com.energyict.protocol.MeterEvent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Provides functionality to handle meterEvents
 */
public class EventUtils {

    /**
     * Removes the duplicate events.
     * This means EICode AND ProtocolCode AND Message AND Date must be equal!
     *
     * @param meterEvents the list of MeterEvents to clean up
     */
    public static void removeDuplicateEvents(List<MeterEvent> meterEvents) {
        List<MeterEvent> duplicateEvent = new ArrayList<MeterEvent>();

        for (MeterEvent meterEvent : meterEvents) {
            if (!duplicateEvent.contains(meterEvent)) {
                for (MeterEvent mEvent : meterEvents) {
                    if (!mEvent.equals(meterEvent)) {
                        if (mEvent.getEiCode() == meterEvent.getEiCode() && mEvent.getMessage().equalsIgnoreCase(meterEvent.getMessage())
                                && mEvent.getProtocolCode() == meterEvent.getProtocolCode() && mEvent.getTime().equals(meterEvent.getTime())) {
                            duplicateEvent.add(mEvent);
                        }
                    }
                }
            }
        }
        meterEvents.removeAll(duplicateEvent);
    }

    /**
     * Removes the events with a Date equal or prior to the given fromDate (Otherwise the ComServer will throw a duplicate exception)
     *
     * @param meterEvents the list of MeterEvents to clean up
     * @param fromDate    the fromDate from where to start skipping.
     */
    public static void removeStoredEvents(List<MeterEvent> meterEvents, Date fromDate) {
        List<MeterEvent> pastEvents = new ArrayList<MeterEvent>();
        for (MeterEvent meterEvent : meterEvents) {
            if (!meterEvent.getTime().after(fromDate)) {
                pastEvents.add(meterEvent);
            }
        }
        meterEvents.removeAll(pastEvents);
    }
}
