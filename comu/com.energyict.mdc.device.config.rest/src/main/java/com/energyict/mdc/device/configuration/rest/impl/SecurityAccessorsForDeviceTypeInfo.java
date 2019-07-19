/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SecurityAccessorsForDeviceTypeInfo {
    // this is fucking device name (did not changed name while needed change on JS as well)
    public String name;
    // this is fucking device version (did not changed name while needed change on JS as well)
    public long version;
    public List<SecurityAccessorTypeInfo> securityAccessors;
}
