/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.data.BatchService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.device.topology.multielement.MultiElementDeviceService;

import javax.xml.bind.annotation.XmlRootElement;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class DataLoggerSlaveDeviceInfos {

    private final TopologyService topologyService;
    private final MultiElementDeviceService multiElementDeviceService;
    private final Clock clock;
    private final BatchService batchService;
    public int total;
    public List<DataLoggerSlaveDeviceInfo> devices = new ArrayList<>();

    public DataLoggerSlaveDeviceInfos(TopologyService topologyService, MultiElementDeviceService multiElementDeviceService, Clock clock, BatchService batchService) {
        this.topologyService = topologyService;
        this.multiElementDeviceService = multiElementDeviceService;
        this.clock = clock;
        this.batchService = batchService;
    }

    void addAll(Iterable<? extends Device> devices) {
        for (Device each : devices) {
            add(each);
        }
    }

    private DataLoggerSlaveDeviceInfo add(Device device) {
        DataLoggerSlaveDeviceInfo result = DataLoggerSlaveDeviceInfo.from(device, batchService, topologyService, multiElementDeviceService, clock);
        devices.add(result);
        total++;
        return result;
    }
}
