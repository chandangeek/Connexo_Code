package com.elster.jupiter.estimation;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.util.time.ScheduleExpression;

import java.time.Instant;

public interface EstimationTaskBuilder {

    EstimationTaskBuilder setScheduleExpression(ScheduleExpression scheduleExpression);

    EstimationTaskBuilder setNextExecution(Instant nextExecution);

    EstimationTaskBuilder scheduleImmediately();

    EstimationTask build();

    EstimationTaskBuilder setName(String string);

    EstimationTaskBuilder setEndDeviceGroup(EndDeviceGroup endDeviceGroup);

}
