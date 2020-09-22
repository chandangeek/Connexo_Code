/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.enddevicecontrols;

import com.energyict.mdc.cim.webservices.inbound.soap.impl.ScheduleStrategy;

import ch.iec.tc57._2011.enddevicecontrols.EndDeviceControlAttribute;

import java.util.ArrayList;
import java.util.List;

public class EndDeviceControlMessage {
    private String commandCode;
    private List<EndDeviceControlAttribute> attributes;
    private ScheduleStrategy scheduleStrategy;

    private List<EndDeviceMessage> endDeviceMessages = new ArrayList<>();

    public String getCommandCode() {
        return commandCode;
    }

    public void setCommandCode(String commandCode) {
        this.commandCode = commandCode;
    }

    public List<EndDeviceControlAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<EndDeviceControlAttribute> attributes) {
        this.attributes = attributes;
    }

    public ScheduleStrategy getScheduleStrategy() {
        return scheduleStrategy;
    }

    public void setScheduleStrategy(ScheduleStrategy scheduleStrategy) {
        this.scheduleStrategy = scheduleStrategy;
    }

    public List<EndDeviceMessage> getEndDeviceMessages() {
        return endDeviceMessages;
    }

    public void addEndDeviceMessage(EndDeviceMessage endDeviceMessage) {
        endDeviceMessages.add(endDeviceMessage);
    }
}
