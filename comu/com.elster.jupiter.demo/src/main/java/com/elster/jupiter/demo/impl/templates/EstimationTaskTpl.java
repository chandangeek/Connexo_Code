package com.elster.jupiter.demo.impl.templates;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.builders.EstimationTaskBuilder;
import com.elster.jupiter.estimation.EstimationTask;

public enum EstimationTaskTpl implements Template<EstimationTask, EstimationTaskBuilder> {

    ALL_ELECTRICITY_DEVICES(DeviceGroupTpl.ALL_ELECTRICITY_DEVICES);

    private DeviceGroupTpl deviceGroup;

    EstimationTaskTpl(DeviceGroupTpl deviceGroup) {
        this.deviceGroup = deviceGroup;
    }

    @Override
    public Class<EstimationTaskBuilder> getBuilderClass() {
        return EstimationTaskBuilder.class;
    }

    @Override
    public EstimationTaskBuilder get(EstimationTaskBuilder builder) {
        return builder
                .withName(deviceGroup.getName())
                .withEndDeviceGroup(Builders.from(deviceGroup).get())
                .withNextExecution();
    }
}
