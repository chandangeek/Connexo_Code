/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl;

import com.energyict.mdc.firmware.FirmwareCheckManagementOption;
import com.energyict.mdc.firmware.FirmwareManagementOptions;
import com.energyict.mdc.firmware.FirmwareStatus;

import java.util.EnumSet;

public class CheckManagementOptionInfo {
    public boolean activated;
    public EnumSet<FirmwareStatus> statuses;

    public CheckManagementOptionInfo() {
        // for the case where firmware management is off
    }

    public CheckManagementOptionInfo(boolean activated, EnumSet<FirmwareStatus> statuses) {
        this.activated = activated;
        this.statuses = statuses;
    }

    public CheckManagementOptionInfo(FirmwareManagementOptions config, FirmwareCheckManagementOption check) {
        this(config.isActivated(check), config.getStatuses(check));
    }

    public boolean isActivated() {
        return activated;
    }

    public EnumSet<FirmwareStatus> getStatuses() {
        return statuses;
    }
}
