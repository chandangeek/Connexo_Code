package com.elster.jupiter.tasks;

import java.util.Date;

public interface TaskOccurrence {

    String getPayLoad();

    Date getTriggerTime();

    RecurrentTask getRecurrentTask();

    void save();

    long getId();
}
