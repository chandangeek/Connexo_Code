package com.elster.jupiter.export;

import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.orm.HasAuditInfo;
import com.elster.jupiter.orm.History;
import com.elster.jupiter.time.RelativePeriod;

import java.util.List;
import java.util.function.Predicate;

public interface EventDataSelector extends HasAuditInfo {

    long getId();

    RelativePeriod getExportPeriod();

    EndDeviceGroup getEndDeviceGroup();

    EventDataExportStrategy getEventStrategy();

    void setExportPeriod(RelativePeriod relativePeriod);

    void setEndDeviceGroup(EndDeviceGroup endDeviceGroup);

    void save();

    History<? extends EventDataSelector> getEventSelectorHistory();

    ExportTask getExportTask();

    List<EndDeviceEventTypeFilter> getEventTypeFilters();

    EndDeviceEventTypeFilter addEventTypeFilter(String code);

    void removeEventTypeFilter(String code);

    Predicate<? super EndDeviceEventRecord> getFilterPredicate();
}
