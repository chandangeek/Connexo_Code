/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.utils;


import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import java.util.Date;

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
}
