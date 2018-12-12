/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export;

import com.elster.jupiter.orm.HasAuditInfo;

public interface DataExportDestination extends HasAuditInfo {

    ExportTask getTask();

    long getId();

    void save();

}
