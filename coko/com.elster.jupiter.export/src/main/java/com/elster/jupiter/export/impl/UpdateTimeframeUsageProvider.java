package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.ExportTask;
import com.elster.jupiter.export.ReadingDataSelectorConfig;
import com.elster.jupiter.export.impl.AbstractExportUsageProvider;
import com.elster.jupiter.export.impl.TranslationKeys;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.time.RelativePeriodUsageProvider;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;


@Component(service = RelativePeriodUsageProvider.class)
public class UpdateTimeframeUsageProvider extends AbstractExportUsageProvider {

    @Reference
    public void setExportService(DataExportService exportService) {
        this.exportService = exportService;
    }

    @Reference
    public void setTimeService(TimeService timeService) {
        this.timeService = timeService;
    }

    @Override
    public String getType() { return TranslationKeys.RELATIVE_PERIOD_UPDATEWINDOW_CATEGORY.getKey(); }

    @Override
    protected boolean matchesRelativePeriod(ExportTask task, long relativePeriodId) {
        return task.getStandardDataSelectorConfig()
                .filter(ReadingDataSelectorConfig.class::isInstance)
                .map(ReadingDataSelectorConfig.class::cast)
                .flatMap(ReadingDataSelectorConfig::getUpdatePeriod)
                .filter(period -> period.getId() == relativePeriodId)
                .isPresent();
    }
}
