package com.elster.jupiter.tasks;

import com.elster.jupiter.messaging.DestinationSpec;

public interface RecurrentTaskBuilder {

    RecurrentTaskBuilder setCronExpression(String expression);

    RecurrentTaskBuilder setDestination(DestinationSpec destination);

    RecurrentTaskBuilder setPayLoad(String payLoad);

    RecurrentTask build();

	RecurrentTaskBuilder setName(String string);
}
