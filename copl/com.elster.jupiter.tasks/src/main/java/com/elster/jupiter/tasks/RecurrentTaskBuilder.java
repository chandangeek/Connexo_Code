/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.tasks;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.util.time.ScheduleExpression;

import java.time.Instant;

public interface RecurrentTaskBuilder {

    RecurrentTaskBuilderNameSetter setApplication(String application);

    interface RecurrentTaskBuilderNameSetter {

        RecurrentTaskBuilderScheduleSetter setName(String string);

    }

    interface RecurrentTaskBuilderScheduleSetter {
        RecurrentTaskBuilderDestinationSetter setScheduleExpressionString(String expression);

        RecurrentTaskBuilderDestinationSetter setScheduleExpression(ScheduleExpression scheduleExpression);
    }

    interface RecurrentTaskBuilderDestinationSetter {
        RecurrentTaskBuilderPayloadSetter setDestination(DestinationSpec destination);
    }

    interface RecurrentTaskBuilderPayloadSetter {
        RecurrentTaskBuilderFinisher setPayLoad(String payLoad);
    }

    interface RecurrentTaskBuilderFinisher {

        RecurrentTaskBuilderFinisher scheduleImmediately(boolean value);

        RecurrentTaskBuilderFinisher setFirstExecution(Instant instant);

        RecurrentTask build();


    }
}
