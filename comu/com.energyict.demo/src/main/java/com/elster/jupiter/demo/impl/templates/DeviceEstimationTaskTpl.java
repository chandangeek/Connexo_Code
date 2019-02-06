/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.templates;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.builders.EstimationTaskBuilder;
import com.elster.jupiter.estimation.EstimationTask;
import com.elster.jupiter.time.PeriodicalScheduleExpression;

public enum DeviceEstimationTaskTpl implements Template<EstimationTask, EstimationTaskBuilder> {

    ALL_ELECTRICITY_DEVICES(DeviceGroupTpl.ALL_ELECTRICITY_DEVICES, PeriodicalScheduleExpression.every(1).days().at(7, 0, 0).build()),
    GAS_DEVICES(DeviceGroupTpl.GAS_DEVICES, PeriodicalScheduleExpression.every(1).days().at(7, 10, 0).build()),
    WATER_DEVICES(DeviceGroupTpl.WATER_DEVICES, PeriodicalScheduleExpression.every(1).days().at(7, 20, 0).build());

    private final PeriodicalScheduleExpression scheduleExpression;
    private final DeviceGroupTpl deviceGroup;

    DeviceEstimationTaskTpl(DeviceGroupTpl deviceGroup, PeriodicalScheduleExpression scheduleExpression) {
        this.deviceGroup = deviceGroup;
        this.scheduleExpression = scheduleExpression;
    }

    @Override
    public Class<EstimationTaskBuilder> getBuilderClass() {
        return EstimationTaskBuilder.class;
    }

    @Override
    public EstimationTaskBuilder get(EstimationTaskBuilder builder) {
        return builder.withQualityCodeSystem(QualityCodeSystem.MDC)
                .withName(deviceGroup.getName())
                .withEndDeviceGroup(Builders.from(deviceGroup).get())
                .withScheduleExpression(scheduleExpression)
                .withNextExecution();
    }
}
