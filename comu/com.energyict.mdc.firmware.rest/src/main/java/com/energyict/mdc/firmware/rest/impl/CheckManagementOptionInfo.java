/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl;

import com.energyict.mdc.firmware.FirmwareStatus;

import java.util.EnumSet;

public class CheckManagementOptionInfo {
    public EnumSet<FirmwareStatus> activatedFor;

    public CheckManagementOptionInfo() {
        this.activatedFor = EnumSet.noneOf(FirmwareStatus.class);
    }

    public CheckManagementOptionInfo(EnumSet<FirmwareStatus> activatedFor) {
        this.activatedFor = activatedFor;
    }

    public EnumSet<FirmwareStatus> getActivatedFor() {
        return activatedFor;
    }
}
