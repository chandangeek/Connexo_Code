/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.templates;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.builders.DataExportTaskBuilder;
import com.elster.jupiter.export.ExportTask;
import com.elster.jupiter.time.PeriodicalScheduleExpression;

public enum DeviceDataExportTaskTpl implements Template<ExportTask, DataExportTaskBuilder> {
    NORTH_REGION(DeviceGroupTpl.NORTH_REGION),
    SOUTH_REGION(DeviceGroupTpl.SOUTH_REGION);

    private static final String DEFAULT_PREFIX = "Device data exporter - ";

    private DeviceGroupTpl deviceGroup;

    DeviceDataExportTaskTpl(DeviceGroupTpl deviceGroup) {
        this.deviceGroup = deviceGroup;
    }

    @Override
    public Class<DataExportTaskBuilder> getBuilderClass() {
        return DataExportTaskBuilder.class;
    }

    @Override
    public DataExportTaskBuilder get(DataExportTaskBuilder builder) {
        return builder
                .withName(DEFAULT_PREFIX + deviceGroup.getName())
                .withDeviceGroup(Builders.from(deviceGroup).get())
                .withScheduleExpression(PeriodicalScheduleExpression.every(1).days().at(11, 0, 0).build())
                .withNextExecution();
    }
}
