/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.data.Device;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class DeviceInfos {

    public int total;
    public List<DeviceInfo> devices = new ArrayList<>();

    public DeviceInfos() {
    }

    public DeviceInfos(Iterable<? extends Device> readingTypes) {
        addAll(readingTypes);
    }

    void addAll(Iterable<? extends Device> devices) {
        for (Device each : devices) {
            add(each);
        }
    }

    public DeviceInfo add(Device device) {
        DeviceInfo result = DeviceInfo.from(device);
        devices.add(result);
        total++;
        return result;
    }
}
