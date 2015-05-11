package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.ExportTask;
import com.elster.jupiter.orm.HasAuditInfo;
import com.elster.jupiter.properties.PropertySpec;

public interface IExportTask extends ExportTask, HasAuditInfo {
    PropertySpec getPropertySpec(String name);

    String getDisplayName(String name);
}
