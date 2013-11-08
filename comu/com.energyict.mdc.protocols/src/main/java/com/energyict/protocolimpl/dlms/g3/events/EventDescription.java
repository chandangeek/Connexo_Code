package com.energyict.protocolimpl.dlms.g3.events;

/**
 * Copyrights EnergyICT
 * Date: 28/03/12
 * Time: 15:02
 */
public class EventDescription {

    private final int meterEventCode;
    private final int eisEventCode;
    private final String eventMessage;

    public EventDescription(int meterEventCode, int eisEventCode, String eventMessage) {
        this.meterEventCode = meterEventCode;
        this.eisEventCode = eisEventCode;
        this.eventMessage = eventMessage;
    }

    public int getMeterEventCode() {
        return meterEventCode;
    }

    public int getEisEventCode() {
        return eisEventCode;
    }

    public String getEventMessage() {
        return eventMessage;
    }
}