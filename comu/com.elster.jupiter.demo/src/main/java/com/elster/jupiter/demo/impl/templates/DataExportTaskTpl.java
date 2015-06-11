package com.elster.jupiter.demo.impl.templates;

import com.elster.jupiter.demo.impl.builders.DataExportTaskBuilder;
import com.elster.jupiter.export.ExportTask;

public enum DataExportTaskTpl implements Template<ExportTask, DataExportTaskBuilder> {
    NORTH_REGION(DeviceGroupTpl.NORTH_REGION),
    SOUTH_REGION(DeviceGroupTpl.SOUTH_REGION),
    ;
    private static final String DEFAULT_PREFIX = "Consumption data exporter - ";

    private DeviceGroupTpl deviceGroup;

    DataExportTaskTpl(DeviceGroupTpl deviceGroup) {
        this.deviceGroup = deviceGroup;
    }

    @Override
    public Class<DataExportTaskBuilder> getBuilderClass() {
        return DataExportTaskBuilder.class;
    }

    @Override
    public DataExportTaskBuilder get(DataExportTaskBuilder builder) {
        return builder
                .withName(DEFAULT_PREFIX + this.deviceGroup.getName())
                .withGroup(this.deviceGroup.getName());
    }


}
