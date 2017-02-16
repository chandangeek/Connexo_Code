/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.math.BigDecimal;

/**
 * Created by dvy on 15/09/2014.
 */
public class ComTaskUrgencyInfo {
    @JsonProperty("urgency")
    public int urgency;
    public DeviceInfo device;

    @JsonSetter
    public void setUrgency(BigDecimal urgency) {
        this.urgency = urgency != null ? urgency.intValue() : 0;
    }

}