package com.elster.jupiter.estimation;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.util.time.ScheduleExpression;

import java.time.Instant;

public interface EstimationTaskBuilder {

    EstimationTaskBuilder setScheduleExpression(ScheduleExpression scheduleExpression);

    EstimationTaskBuilder setNextExecution(Instant nextExecution);

    EstimationTaskBuilder setApplication(String application);

    EstimationTaskBuilder scheduleImmediately();

    EstimationTask create();

    EstimationTaskBuilder setName(String string);

    EstimationTaskBuilder setEndDeviceGroup(EndDeviceGroup endDeviceGroup);

    EstimationTaskBuilder setPeriod(RelativePeriod relativePeriod);
}
