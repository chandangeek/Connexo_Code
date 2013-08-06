package com.elster.jupiter.metering.rest;

import com.elster.jupiter.metering.AmrSystem;

public class AmrSystemInfo {

    public String name;

    public AmrSystemInfo() {
    }

    public AmrSystemInfo(AmrSystem amrSystem) {
        this.name = amrSystem.getName();
    }
}
