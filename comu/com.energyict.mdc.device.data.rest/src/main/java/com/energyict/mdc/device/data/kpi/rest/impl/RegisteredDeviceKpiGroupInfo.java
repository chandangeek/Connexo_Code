/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.kpi.rest.impl;

import com.energyict.mdc.device.topology.kpi.RegisteredDevicesKpi;
import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;

public class RegisteredDeviceKpiGroupInfo {
    public long id;
    public String name;
    public TemporalExpressionInfo frequency;

    public RegisteredDeviceKpiGroupInfo(RegisteredDevicesKpi kpi) {
        id = kpi.getId();
        name = kpi.getDeviceGroup().getName();
        frequency = kpi.getFrequency()!=null?TemporalExpressionInfo.from(kpi.getFrequency()):null;
    }
}
