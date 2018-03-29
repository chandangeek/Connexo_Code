/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.customtask.impl;

import com.elster.jupiter.customtask.CustomTask;
import com.elster.jupiter.customtask.CustomTaskProperty;
import com.elster.jupiter.orm.HasAuditInfo;
import com.elster.jupiter.properties.PropertySpec;

import java.util.List;

interface ICustomTask extends CustomTask, HasAuditInfo {

    PropertySpec getPropertySpec(String name);

    String getDisplayName(String name);

    void setScheduleImmediately(boolean scheduleImmediately);

    List<CustomTaskProperty> getCustomTaskProperties();
}
