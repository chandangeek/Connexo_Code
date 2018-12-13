/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SecurityAccessorsForDeviceTypeInfo {
    public String name;
    public long version;
    public List<SecurityAccessorTypeInfo> securityAccessors;
}
