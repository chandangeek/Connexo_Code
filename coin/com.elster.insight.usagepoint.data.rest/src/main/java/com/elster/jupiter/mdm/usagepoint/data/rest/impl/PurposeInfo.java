/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.mdm.usagepoint.config.rest.ReadingTypeDeliverablesInfo;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.VersionInfo;
import com.elster.jupiter.validation.rest.DataValidationTaskMinimalInfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PurposeInfo {
    public long id;
    public String name;
    public String description;
    public VersionInfo<Long> parent;
    public boolean required;
    public boolean active;
    public String mRID;
    public IdWithNameInfo status;
    public List<MeterRoleInfo> meterRoles;
    public Long version;
    public UsagePointValidationStatusInfo validationInfo;
    public List<DataValidationTaskMinimalInfo> dataValidationTasks;
    public List<ReadingTypeDeliverablesInfo> readingTypeDeliverables;

}
