package com.elster.jupiter.export;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.util.HasName;
import com.elster.jupiter.util.time.ScheduleExpression;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface ReadingTypeDataExportTask extends HasName {

    long getId();

    void activate(); // resume

    void deactivate(); // suspend

    RelativePeriod getExportPeriod();

    Optional<RelativePeriod> getUpdatePeriod(); // checks for updates in this period

    Optional<Instant> getLastRun();

    EndDeviceGroup getEndDeviceGroup();

    Map<String, Object> getProperties();

    Instant getNextExecution();

    List<? extends DataExportOccurrence> getOccurrences(Range<Instant> interval);

    DataExportStrategy getStrategy();

    Set<ReadingType> getReadingTypes();

    void save();

    void delete();

    boolean isActive();

    String getDataFormatter();

    List<PropertySpec<?>> getPropertySpecs();

    ScheduleExpression getScheduleExpression();

    Optional<? extends DataExportOccurrence> getLastOccurrence();
    List<ReadingTypeDataExportItem> getExportItems();
    Optional<? extends DataExportOccurrence> getLastOccurence();

    void setNextExecution(Instant instant);

    void setScheduleExpression(ScheduleExpression scheduleExpression);

    void setName(String name);

    void setExportPeriod(RelativePeriod relativePeriod);

    void setProperty(String key, Object value);

    void setEndDeviceGroup(EndDeviceGroup endDeviceGroup);

    void removeReadingType(ReadingType readingType);

    void addReadingType(ReadingType readingType);

    void addReadingType(String readingTypeMrid);

    void setUpdatePeriod(RelativePeriod relativePeriod);
}
