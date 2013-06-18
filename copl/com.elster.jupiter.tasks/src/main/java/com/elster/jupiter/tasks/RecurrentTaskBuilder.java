package com.elster.jupiter.tasks;

public interface RecurrentTaskBuilder {

    RecurrentTaskBuilder setCronExpression(String expression);

    RecurrentTaskBuilder setDestination(String destination);

    RecurrentTaskBuilder setPayLoad(String payLoad);

    RecurrentTask build();
}
