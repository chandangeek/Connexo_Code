/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.tasks;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.util.time.ScheduleExpression;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;

@ProviderType
public interface RecurrentTaskBuilder {

    RecurrentTaskBuilderNameSetter setApplication(String application);

    @ProviderType
    interface RecurrentTaskBuilderNameSetter {

        RecurrentTaskBuilderScheduleSetter setName(String string);

    }

    @ProviderType
    interface RecurrentTaskBuilderScheduleSetter {
        RecurrentTaskBuilderDestinationSetter setScheduleExpressionString(String expression);

        RecurrentTaskBuilderDestinationSetter setScheduleExpression(ScheduleExpression scheduleExpression);
    }

    @ProviderType
    interface RecurrentTaskBuilderDestinationSetter {
        RecurrentTaskBuilderPayloadSetter setDestination(DestinationSpec destination);
    }

    @ProviderType
    interface RecurrentTaskBuilderPayloadSetter {
        RecurrentTaskBuilderFinisher setPayLoad(String payLoad);
    }

    @ProviderType
    interface RecurrentTaskBuilderFinisher {

        RecurrentTaskBuilderFinisher scheduleImmediately(boolean value);

        RecurrentTaskBuilderFinisher setFirstExecution(Instant instant);

        RecurrentTaskBuilderFinisher setLogLevel(int level);

        RecurrentTask build();

    }
}
