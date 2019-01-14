/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.templates;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.builders.DataValidationTaskBuilder;
import com.elster.jupiter.time.PeriodicalScheduleExpression;
import com.elster.jupiter.util.time.ScheduleExpression;
import com.elster.jupiter.validation.DataValidationTask;

public enum UsagePointDataValidationTaskTpl implements Template<DataValidationTask, DataValidationTaskBuilder> {

    RESIDENTIAL_ELECTRICITY(UsagePointGroupTpl.RESIDENTIAL_ELECTRICITY, PeriodicalScheduleExpression.every(1).days().at(6, 10, 0).build()),
    RESIDENTIAL_GAS(UsagePointGroupTpl.RESIDENTIAL_GAS, PeriodicalScheduleExpression.every(1).days().at(6, 20, 0).build()),
    RESIDENTIAL_WATER(UsagePointGroupTpl.RESIDENTIAL_WATER, PeriodicalScheduleExpression.every(1).days().at(6, 30, 0).build());

    private final UsagePointGroupTpl usagePointGroup;
    private final ScheduleExpression scheduleExpression;

    UsagePointDataValidationTaskTpl(UsagePointGroupTpl usagePointGroup, ScheduleExpression scheduleExpression) {
        this.usagePointGroup = usagePointGroup;
        this.scheduleExpression = scheduleExpression;
    }

    @Override
    public Class<DataValidationTaskBuilder> getBuilderClass() {
        return DataValidationTaskBuilder.class;
    }

    @Override
    public DataValidationTaskBuilder get(DataValidationTaskBuilder builder) {
        return builder
                .withQualityCodeSystem(QualityCodeSystem.MDM)
                .withName(usagePointGroup.getName())
                .withUsagePointGroup(Builders.from(usagePointGroup).get())
                .withScheduleExpression(scheduleExpression)
                .withNextExecution();
    }
}
