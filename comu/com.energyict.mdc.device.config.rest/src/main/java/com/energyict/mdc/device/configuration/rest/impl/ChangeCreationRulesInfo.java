/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ChangeCreationRulesInfo {
    public String message;
    public List<String> affectedRules;
    public DeviceTypeInfo deviceTypeInfo;

    public ChangeCreationRulesInfo(String message, DeviceTypeInfo deviceTypeInfo, String... affectedRules) {
        this.message = message;
        this.deviceTypeInfo = deviceTypeInfo;
        this.affectedRules = Arrays.stream(affectedRules).collect(Collectors.toList());
    }
}
