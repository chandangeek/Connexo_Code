/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.util.time.ScheduleExpression;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.util.List;

@ProviderType
public interface EstimationTaskBuilder {

    EstimationTaskBuilder setScheduleExpression(ScheduleExpression scheduleExpression);

    EstimationTaskBuilder setNextExecution(Instant nextExecution);

    EstimationTaskBuilder setQualityCodeSystem(QualityCodeSystem qualityCodeSystem);

    EstimationTaskBuilder scheduleImmediately();

    EstimationTask create();

    EstimationTaskBuilder setName(String string);

    EstimationTaskBuilder setRevalidate(boolean revalidate);

    EstimationTaskBuilder setLogLevel(int logLevel);

    EstimationTaskBuilder setEndDeviceGroup(EndDeviceGroup endDeviceGroup);

    EstimationTaskBuilder setUsagePointGroup(UsagePointGroup usagePointGroup);

    EstimationTaskBuilder setMetrologyPurpose(MetrologyPurpose metrologyPurpose);

    EstimationTaskBuilder setPeriod(RelativePeriod relativePeriod);

    EstimationTaskBuilder setNextRecurrentTasks(List<RecurrentTask> nextRecurrentTasks);
}
