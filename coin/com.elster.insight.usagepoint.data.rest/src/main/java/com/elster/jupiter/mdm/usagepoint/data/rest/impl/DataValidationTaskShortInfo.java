package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.validation.rest.DataValidationTaskInfo;

public class DataValidationTaskShortInfo {

    public long id;
    public String name;
    public String trigger;

    public DataValidationTaskShortInfo() {
    }

    public DataValidationTaskShortInfo(DataValidationTaskInfo dataValidationTaskInfo) {
        this.id = dataValidationTaskInfo.id;
        this.name = dataValidationTaskInfo.name;
        if (dataValidationTaskInfo.lastValidationOccurence != null) {
//            this.trigger = dataValidationTaskInfo.lastValidationOccurence.trigger;
        }
    }
}
