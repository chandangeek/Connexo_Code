/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.device.configuration.rest.EstimationRuleSetRefInfo;

import java.util.List;

public class EstimationRuleSetReorderInfo {
    public DeviceConfigurationInfo parent = new DeviceConfigurationInfo();
    public List<EstimationRuleSetRefInfo> ruleSets;
}
