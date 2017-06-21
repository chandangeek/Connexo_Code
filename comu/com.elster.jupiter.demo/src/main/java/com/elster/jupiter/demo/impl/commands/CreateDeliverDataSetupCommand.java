/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.commands;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.templates.DeviceDataExportTaskTpl;
import com.elster.jupiter.demo.impl.templates.UsagePointDataExportTaskTpl;
import com.elster.jupiter.license.LicenseService;

import javax.inject.Inject;

public class CreateDeliverDataSetupCommand extends CommandWithTransaction {

    private final LicenseService licenseService;

    @Inject
    public CreateDeliverDataSetupCommand(LicenseService licenseService) {
        this.licenseService = licenseService;
    }

    @Override
    public void run() {
        Builders.from(DeviceDataExportTaskTpl.NORTH_REGION).get();
        Builders.from(DeviceDataExportTaskTpl.SOUTH_REGION).get();
        if (licenseService.getLicenseForApplication("INS").isPresent()) {
            Builders.from(UsagePointDataExportTaskTpl.RESIDENTIAL_ELECTRICITY).get();
        }
    }
}
