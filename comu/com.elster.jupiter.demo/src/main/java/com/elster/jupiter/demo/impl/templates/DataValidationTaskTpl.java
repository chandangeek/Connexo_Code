package com.elster.jupiter.demo.impl.templates;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.builders.DataValidationTaskBuilder;
import com.elster.jupiter.validation.DataValidationTask;

public enum DataValidationTaskTpl implements Template<DataValidationTask, DataValidationTaskBuilder> {

    A1800_DEVICES(DeviceGroupTpl.A1800_DEVICES);

    private DeviceGroupTpl deviceGroup;

    DataValidationTaskTpl(DeviceGroupTpl deviceGroup) {
        this.deviceGroup = deviceGroup;
    }

    @Override
    public Class<DataValidationTaskBuilder> getBuilderClass() {
        return DataValidationTaskBuilder.class;
    }

    @Override
    public DataValidationTaskBuilder get(DataValidationTaskBuilder builder) {
        return builder
                .withName(deviceGroup.getName())
                .withEndDeviceGroup(Builders.from(deviceGroup).get())
                .withNextExecution();
    }
}
