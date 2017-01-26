package com.elster.jupiter.estimation;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.tasks.TaskLogLevel;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.util.time.ScheduleExpression;

import java.time.Instant;

public interface EstimationTaskBuilder {

    EstimationTaskBuilder setScheduleExpression(ScheduleExpression scheduleExpression);

    EstimationTaskBuilder setNextExecution(Instant nextExecution);

    EstimationTaskBuilder setQualityCodeSystem(QualityCodeSystem qualityCodeSystem);

    EstimationTaskBuilder scheduleImmediately();

    EstimationTask create();

    EstimationTaskBuilder setName(String string);

    EstimationTaskBuilder setLogLevel(TaskLogLevel logLevel);

    EstimationTaskBuilder setEndDeviceGroup(EndDeviceGroup endDeviceGroup);

    EstimationTaskBuilder setUsagePointGroup(UsagePointGroup usagePointGroup);

    EstimationTaskBuilder setPeriod(RelativePeriod relativePeriod);
}
