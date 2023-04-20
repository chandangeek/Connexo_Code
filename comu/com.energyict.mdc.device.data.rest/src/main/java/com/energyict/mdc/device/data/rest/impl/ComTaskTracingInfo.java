/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.fasterxml.jackson.annotation.JsonSetter;

/**
 * Created by Yulia Alenkova on 02/08/2021.
 */
public class ComTaskTracingInfo {
    public boolean traced;
    public DeviceInfo device;

    @JsonSetter
    public void setTraced(boolean traced) {
        this.traced = traced;
    }

}