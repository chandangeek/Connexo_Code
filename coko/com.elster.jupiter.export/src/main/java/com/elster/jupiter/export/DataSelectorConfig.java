/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export;

import com.elster.jupiter.orm.HasAuditInfo;
import com.elster.jupiter.orm.History;
import com.elster.jupiter.time.RelativePeriod;

import java.util.logging.Logger;

public interface DataSelectorConfig extends HasAuditInfo {

    long getId();

    ExportTask getExportTask();

    RelativePeriod getExportPeriod();

    boolean isExportContinuousData();

    DataSelector createDataSelector(Logger logger);

    void apply(DataSelectorConfigVisitor visitor);

    History<DataSelectorConfig> getHistory();

    interface Updater {

        Updater setExportPeriod(RelativePeriod relativePeriod);

        Updater setExportContinuousData(boolean exportContinuousData);

        DataSelectorConfig complete();

    }

    interface DataSelectorConfigVisitor {

        void visit(MeterReadingSelectorConfig config);

        void visit(UsagePointReadingSelectorConfig config);

        void visit(EventSelectorConfig config);

    }
}
