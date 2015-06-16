package com.elster.jupiter.export;

import com.elster.jupiter.orm.HasAuditInfo;

public interface DataExportDestination extends HasAuditInfo {

    ExportTask getTask();

    long getId();

    void save();

}
