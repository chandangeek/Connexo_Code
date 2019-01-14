/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.templates;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.builders.DataExportTaskBuilder;
import com.elster.jupiter.export.ExportTask;
import com.elster.jupiter.metering.config.DefaultMetrologyPurpose;
import com.elster.jupiter.time.PeriodicalScheduleExpression;

public enum UsagePointDataExportTaskTpl implements Template<ExportTask, DataExportTaskBuilder> {
    RESIDENTIAL_ELECTRICITY(UsagePointGroupTpl.RESIDENTIAL_ELECTRICITY);

    private static final String DEFAULT_PREFIX = "Usage point data exporter - ";

    private UsagePointGroupTpl usagePointGroup;

    UsagePointDataExportTaskTpl(UsagePointGroupTpl usagePointGroup) {
        this.usagePointGroup = usagePointGroup;
    }

    @Override
    public Class<DataExportTaskBuilder> getBuilderClass() {
        return DataExportTaskBuilder.class;
    }

    @Override
    public DataExportTaskBuilder get(DataExportTaskBuilder builder) {
        return builder
                .withName(DEFAULT_PREFIX + usagePointGroup.getName())
                .withUsagePointGroup(Builders.from(usagePointGroup).get())
                .withMetrologyPurpose(DefaultMetrologyPurpose.MARKET)
                .withScheduleExpression(PeriodicalScheduleExpression.every(1).days().at(11, 30, 0).build())
                .withNextExecution();
    }
}
