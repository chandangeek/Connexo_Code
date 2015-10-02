package com.elster.jupiter.export;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.orm.HasAuditInfo;
import com.elster.jupiter.orm.History;
import com.elster.jupiter.time.RelativePeriod;

import java.time.Instant;
import java.util.List;
import java.util.Set;

public interface StandardDataSelector extends HasAuditInfo {

    long getId();

    RelativePeriod getExportPeriod();

    EndDeviceGroup getEndDeviceGroup();

    DataExportStrategy getStrategy();

    Set<ReadingType> getReadingTypes();

    List<? extends ReadingTypeDataExportItem> getExportItems();

    void setExportPeriod(RelativePeriod relativePeriod);

    void setEndDeviceGroup(EndDeviceGroup endDeviceGroup);

    void removeReadingType(ReadingType readingType);

    void addReadingType(ReadingType readingType);

    void addReadingType(String readingTypeMrid);

    void setUpdatePeriod(RelativePeriod relativePeriod);

    void setUpdateWindow(RelativePeriod updateWindow);

    Set<ReadingType> getReadingTypes(Instant at);

    void setValidatedDataOption(ValidatedDataOption validatedDataOption);

    void setExportUpdate(boolean exportUpdate);

    void setExportContinuousData(boolean exportContinuousData);

    void setExportOnlyIfComplete(boolean exportOnlyIfComplete);

    void save();

    History<StandardDataSelector> getHistory();

    ExportTask getExportTask();
}
