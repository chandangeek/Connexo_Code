package com.elster.jupiter.tasks;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.util.time.ScheduleExpression;

import java.time.Instant;

public interface RecurrentTaskBuilder {

    RecurrentTaskBuilder setScheduleExpressionString(String expression);

    RecurrentTaskBuilder setDestination(DestinationSpec destination);

    RecurrentTaskBuilder setPayLoad(String payLoad);

    RecurrentTaskBuilder scheduleImmediately(boolean value);

    RecurrentTaskBuilder setFirstExecution(Instant instant);

    RecurrentTask build();

	RecurrentTaskBuilder setName(String string);

    RecurrentTaskBuilder setScheduleExpression(ScheduleExpression scheduleExpression);
}
