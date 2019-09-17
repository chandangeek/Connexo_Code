/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportProperty;
import com.elster.jupiter.export.DataSelectorConfig;
import com.elster.jupiter.export.ExportTask;
import com.elster.jupiter.orm.HasAuditInfo;
import com.elster.jupiter.properties.PropertySpec;

import java.time.Instant;
import java.util.List;

interface IExportTask extends ExportTask, HasAuditInfo {

    PropertySpec getPropertySpec(String name);

    String getDisplayName(String name);

    void setScheduleImmediately(boolean scheduleImmediately);

    List<DataExportProperty> getDataExportProperties();

    List<DataExportProperty> getDataExportProperties(Instant at);

    void setStandardDataSelectorConfig(DataSelectorConfig dataSelectorConfig);

    CompositeDataExportDestination getCompositeDestination();

    CompositeDataExportDestination getCompositeDestination(Instant at);

    boolean hasDefaultSelector();

}
