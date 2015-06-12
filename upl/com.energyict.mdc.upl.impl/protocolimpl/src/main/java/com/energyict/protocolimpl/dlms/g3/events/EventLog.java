package com.energyict.protocolimpl.dlms.g3.events;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterEvent;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 12/3/12
 * Time: 9:34 AM
 */
public interface EventLog {

    /**
     * Read the events from a logbook in a device, taking the from and to date in account.
     *
     * @param from The start date (including)
     * @param to   The end date (excluding)
     * @return A list of {@link com.energyict.protocol.MeterEvent}. This should never return 'null'.
     * @throws java.io.IOException If there occured an error while reading the events (timeout, parsing error, ...)
     */
    List<MeterEvent> getEvents(Calendar from, Calendar to) throws IOException;

    /**
     * This method is used to parse the received array of events into EIServer MeterEvents.
     */
    List<MeterEvent> parseEvents(AbstractDataType buffer) throws IOException;

    public ObisCode getObisCode();

}