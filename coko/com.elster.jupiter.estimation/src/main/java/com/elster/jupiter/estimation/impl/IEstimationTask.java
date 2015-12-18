package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.estimation.EstimationTask;
import com.elster.jupiter.tasks.RecurrentTask;

public interface IEstimationTask extends EstimationTask {

    void doSave();

    void setScheduleImmediately(boolean scheduleImmediately);

    RecurrentTask getRecurrentTask();
}
