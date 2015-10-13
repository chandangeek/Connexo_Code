package com.elster.jupiter.export;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.orm.HasAuditInfo;
import com.elster.jupiter.orm.History;
import com.elster.jupiter.time.RelativePeriod;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public interface EventDataSelector extends HasAuditInfo {

    long getId();

    RelativePeriod getExportPeriod();

    EndDeviceGroup getEndDeviceGroup();

    EventDataExportStrategy getEventStrategy();

    void setExportPeriod(RelativePeriod relativePeriod);

    void setEndDeviceGroup(EndDeviceGroup endDeviceGroup);

    Set<ReadingType> getReadingTypes(Instant at);

    void setValidatedDataOption(ValidatedDataOption validatedDataOption);

    void setExportUpdate(boolean exportUpdate);

    void setExportContinuousData(boolean exportContinuousData);

    void setExportOnlyIfComplete(boolean exportOnlyIfComplete);

    void save();

    History<? extends EventDataSelector> getEventSelectorHistory();

    ExportTask getExportTask();

    List<EndDeviceEventTypeFilter> getEventTypeFilters();

    EndDeviceEventTypeFilter addEventTypeFilter(String code);

    void removeEventTypeFilter(String code);

    Predicate<? super EndDeviceEventRecord> getFilterPredicate();
}
