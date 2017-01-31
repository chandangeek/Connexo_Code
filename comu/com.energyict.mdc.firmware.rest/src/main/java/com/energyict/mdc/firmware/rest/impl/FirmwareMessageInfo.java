/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.properties.rest.PropertyInfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FirmwareMessageInfo {
    public String uploadOption;
    public String localizedValue;
    public List<PropertyInfo> properties;
    public Instant releaseDate;
    public long version; // device version here!

    public FirmwareMessageInfo() {}
}
