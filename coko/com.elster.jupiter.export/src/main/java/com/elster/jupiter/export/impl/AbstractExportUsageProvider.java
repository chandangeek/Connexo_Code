package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.ExportTask;
import com.elster.jupiter.time.RelativePeriodUsageInfo;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.time.RelativePeriodUsageProvider;

import java.util.List;
import java.util.stream.Collectors;


public abstract class AbstractExportUsageProvider implements RelativePeriodUsageProvider {

    protected volatile DataExportService exportService;
    protected volatile TimeService timeService;

    @Override
    public List<RelativePeriodUsageInfo> getUsageReferences(long relativePeriodId) {
        return exportService.findExportTasks()
                .find()
                .stream()
                .filter(task -> this.matchesRelativePeriod(task, relativePeriodId))
                .map(this::createRelativePeriodUsageInfo)
                .collect(Collectors.toList());
    }

    private RelativePeriodUsageInfo createRelativePeriodUsageInfo(ExportTask exportTask) {
        return new RelativePeriodUsageInfo(
                exportTask.getName(),
                timeService.findRelativePeriodCategoryDisplayName(this.getType()),
                exportTask.getApplication(),
                exportTask.getNextExecution());
    }
    protected abstract boolean matchesRelativePeriod(ExportTask task, long relativePeriodId);


}
