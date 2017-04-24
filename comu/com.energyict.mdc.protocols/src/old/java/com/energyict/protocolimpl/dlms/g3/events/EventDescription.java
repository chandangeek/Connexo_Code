/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.g3.events;

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