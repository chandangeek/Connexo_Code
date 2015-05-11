package com.elster.jupiter.export;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.time.RelativePeriod;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ReadingTypeDataExportTask extends ExportTask {

    RelativePeriod getExportPeriod();

    Optional<RelativePeriod> getUpdatePeriod(); // checks for updates in this period

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

    Set<ReadingType> getReadingTypes(Instant at);

}
