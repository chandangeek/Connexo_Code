package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.validation.rest.DataValidationTaskInfo;

import java.util.List;

public class PurposeInfo {
    public long id;
    public String name;
    public boolean required;
    public boolean active;
    public String mRID;
    public IdWithNameInfo status;
    public List<MeterRoleInfo> meterRoles;
    public Long version;
    public UsagePointValidationStatusInfo validationInfo;
    public List<DataValidationTaskInfo> dataValidationTasks;

    public PurposeInfo() {
    }
}
