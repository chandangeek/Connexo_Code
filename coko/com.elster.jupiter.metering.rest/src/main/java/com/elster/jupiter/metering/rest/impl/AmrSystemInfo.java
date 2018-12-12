/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.metering.AmrSystem;

public class AmrSystemInfo {

    public String name;

    public AmrSystemInfo() {
    }

    public AmrSystemInfo(AmrSystem amrSystem) {
        this.name = amrSystem.getName();
    }
}
