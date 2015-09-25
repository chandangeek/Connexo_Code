package com.elster.jupiter.data.lifecycle;

import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskOccurrence;

import java.time.Instant;
import java.util.List;

public interface LifeCycleService {

    String COMPONENTNAME = "LFC";

    List<LifeCycleCategory> getCategories();

    List<LifeCycleCategory> getCategoriesAsOf(Instant instant);

    RecurrentTask getTask();

    TaskOccurrence runNow();
}
