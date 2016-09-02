package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.time.rest.PeriodicalExpressionInfo;
import com.elster.jupiter.validation.rest.DataValidationTaskInfo;

import java.time.Instant;

public class DataValidationTaskShortInfo {

    public long id;
    public String name;
    public PeriodicalExpressionInfo schedule;
    public Instant nextRun;

    public DataValidationTaskShortInfo() {
    }

    public DataValidationTaskShortInfo(DataValidationTaskInfo dataValidationTaskInfo) {
        id = dataValidationTaskInfo.id;
        name = dataValidationTaskInfo.name;
        schedule = dataValidationTaskInfo.schedule;
        nextRun = dataValidationTaskInfo.nextRun;
    }
}
