/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.math.BigDecimal;

/**
 * Created by Yulia Alenkova on 02/08/2021.
 */
public class ComTaskTracingInfo {
    @JsonProperty("isTracing")
    public boolean isTracing;
    public DeviceInfo device;

    @JsonSetter
    public void setIsTracing(boolean isTracing) {
        this.isTracing =  isTracing;
    }

}