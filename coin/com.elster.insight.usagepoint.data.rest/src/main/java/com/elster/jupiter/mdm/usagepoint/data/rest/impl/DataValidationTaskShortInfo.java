package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.validation.rest.DataValidationTaskInfo;

public class DataValidationTaskShortInfo {

    public long id;
    public String name;
    public String trigger;

    public DataValidationTaskShortInfo(DataValidationTaskInfo dataValidationTaskInfo) {
        id = dataValidationTaskInfo.getId();
        name = dataValidationTaskInfo.getName();
        if (dataValidationTaskInfo.lastValidationOccurence != null) {
            trigger = dataValidationTaskInfo.lastValidationOccurence.trigger;
        }
    }
}
