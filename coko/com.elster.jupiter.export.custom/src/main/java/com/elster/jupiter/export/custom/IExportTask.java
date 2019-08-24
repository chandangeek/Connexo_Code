/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.custom;

import com.elster.jupiter.export.ExportTask;
import com.elster.jupiter.export.MeterReadingSelectorConfig;
import com.elster.jupiter.orm.HasAuditInfo;

import java.util.Optional;

public interface IExportTask extends ExportTask, HasAuditInfo {
    Optional<MeterReadingSelectorConfig> getReadingDataSelectorConfig();
}
