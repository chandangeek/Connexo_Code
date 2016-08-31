package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.time.rest.PeriodicalExpressionInfo;
import com.elster.jupiter.validation.rest.DataValidationTaskInfo;

public class DataValidationTaskShortInfo {

    public long id;
    public String name;
    public PeriodicalExpressionInfo schedule;

    public DataValidationTaskShortInfo() {
    }

    public DataValidationTaskShortInfo(DataValidationTaskInfo dataValidationTaskInfo) {
        id = dataValidationTaskInfo.getId();
        name = dataValidationTaskInfo.getName();
        schedule = dataValidationTaskInfo.schedule;
    }
}
