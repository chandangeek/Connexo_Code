/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.templates;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.builders.DataValidationTaskBuilder;
import com.elster.jupiter.validation.DataValidationTask;

public enum DeviceDataValidationTaskTpl implements Template<DataValidationTask, DataValidationTaskBuilder> {

    A1800_DEVICES(DeviceGroupTpl.A1800_DEVICES);

    private final DeviceGroupTpl deviceGroup;

    DeviceDataValidationTaskTpl(DeviceGroupTpl deviceGroup) {
        this.deviceGroup = deviceGroup;
    }

    @Override
    public Class<DataValidationTaskBuilder> getBuilderClass() {
        return DataValidationTaskBuilder.class;
    }

    @Override
    public DataValidationTaskBuilder get(DataValidationTaskBuilder builder) {
        return builder
                .withQualityCodeSystem(QualityCodeSystem.MDC)
                .withName(deviceGroup.getName())
                .withEndDeviceGroup(Builders.from(deviceGroup).get())
                .withNextExecution();
    }
}
