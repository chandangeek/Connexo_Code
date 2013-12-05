package com.energyict.mdc.protocol.device.data;

import com.energyict.mdc.protocol.device.data.identifiers.LogBookIdentifier;
import com.energyict.mdc.protocol.device.events.MeterProtocolEvent;

import java.util.List;

/**
 * A CollectedLogBook identifies a LogBook (by {@link #getLogBookIdentifier()}),
 * and the respective collected {@link com.energyict.mdc.protocol.device.events.MeterEvent}s
 * starting from the last collected MeterEvent.
 */
public interface CollectedLogBook extends CollectedData {

    /**
     *
     * @return a List of collected {@link MeterProtocolEvent}s
     */
    public List<MeterProtocolEvent> getCollectedMeterEvents();

    /**
     * Should provide an identifier to uniquely identify the requested LogBook.
     *
     * @return the {@link LogBookIdentifier logBookIdentifier}
     *         of the BusinessObject which is actionHolder of the request
     */
    public LogBookIdentifier getLogBookIdentifier();

    public void setMeterEvents(List<MeterProtocolEvent> meterEvents);

}