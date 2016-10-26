package com.elster.jupiter.export;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.orm.HasAuditInfo;
import com.elster.jupiter.time.RelativePeriod;

import java.util.Set;

public interface AggregatedDataSelectorConfig extends HasAuditInfo {

    long getId();

    RelativePeriod getExportPeriod();

    void setExportPeriod(RelativePeriod relativePeriod);

    UsagePointGroup getUsagePointGroup();

    void setUsagePointGroup(UsagePointGroup usagePointGroup);

    Set<ReadingType> getReadingTypes();

    void addReadingType(ReadingType readingType);

    void removeReadingType(ReadingType readingType);

    ValidatedDataOption getValidatedDataOption();

    void setExportContinuousData(boolean exportContinuousData);

    void setExportOnlyIfComplete(boolean exportOnlyIfComplete);

    void setValidatedDataOption(ValidatedDataOption validatedDataOption);

    void save();

    ExportTask getExportTask();
}
