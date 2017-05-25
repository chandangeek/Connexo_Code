/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.data.Device;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class DataLoggerSlaveDeviceInfos {

    public List<DataLoggerSlaveDeviceInfo> devices = new ArrayList<>();

    public DataLoggerSlaveDeviceInfos(Iterable<? extends Device> slaves, DataLoggerSlaveDeviceInfoFactory infoFactory) {
        slaves.forEach(each -> {
            devices.add(infoFactory.newSlaveWithLinkingInfo(each));
        });
    }

    @JsonProperty
    public int getTotal() {
        return devices.size();
    }
}
