/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.templates;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.builders.EstimationTaskBuilder;
import com.elster.jupiter.estimation.EstimationTask;
import com.elster.jupiter.time.PeriodicalScheduleExpression;

public enum UsagePointEstimationTaskTpl implements Template<EstimationTask, EstimationTaskBuilder> {

    RESIDENTIAL_ELECTRICITY(UsagePointGroupTpl.RESIDENTIAL_ELECTRICITY, PeriodicalScheduleExpression.every(1).days().at(7, 30, 0).build()),
    RESIDENTIAL_GAS(UsagePointGroupTpl.RESIDENTIAL_GAS, PeriodicalScheduleExpression.every(1).days().at(7, 40, 0).build()),
    RESIDENTIAL_WATER(UsagePointGroupTpl.RESIDENTIAL_WATER, PeriodicalScheduleExpression.every(1).days().at(7, 50, 0).build());

    private final PeriodicalScheduleExpression scheduleExpression;
    private final UsagePointGroupTpl usagePointGroup;

    UsagePointEstimationTaskTpl(UsagePointGroupTpl usagePointGroup, PeriodicalScheduleExpression scheduleExpression) {
        this.usagePointGroup = usagePointGroup;
        this.scheduleExpression = scheduleExpression;
    }

    @Override
    public Class<EstimationTaskBuilder> getBuilderClass() {
        return EstimationTaskBuilder.class;
    }

    @Override
    public EstimationTaskBuilder get(EstimationTaskBuilder builder) {
        return builder.withQualityCodeSystem(QualityCodeSystem.MDM)
                .withName(usagePointGroup.getName())
                .withUsagePointGroup(Builders.from(usagePointGroup).get())
                .withScheduleExpression(scheduleExpression)
                .withNextExecution();
    }
}
