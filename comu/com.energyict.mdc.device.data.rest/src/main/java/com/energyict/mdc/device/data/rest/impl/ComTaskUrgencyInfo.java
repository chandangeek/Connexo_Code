package com.energyict.mdc.device.data.rest.impl;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by dvy on 15/09/2014.
 */
public class ComTaskUrgencyInfo {
    @JsonProperty("urgency")
    public int urgency;

    public DeviceInfo device;
}
