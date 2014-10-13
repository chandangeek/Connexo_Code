package com.elster.jupiter.tasks;

import java.time.Instant;

public interface TaskOccurrence {

    String getPayLoad();

    Instant getTriggerTime();

    RecurrentTask getRecurrentTask();

    void save();

    long getId();
}
