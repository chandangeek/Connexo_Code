/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LifeCycleChangeInfo {
    public String message;
    public List<String> affectedRules;
    public DeviceTypeInfo deviceType;

    public LifeCycleChangeInfo(String message, DeviceTypeInfo deviceType, String... affectedRules) {
        this.message = message;
        this.deviceType = deviceType;
        this.affectedRules = Arrays.stream(affectedRules).filter(rule -> !rule.isEmpty()).collect(Collectors.toList());
    }
}
