package com.elster.jupiter.export;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.orm.HasAuditInfo;
import com.elster.jupiter.orm.History;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.util.HasName;
import com.elster.jupiter.util.time.ScheduleExpression;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface ReadingTypeDataExportTask extends HasName, HasAuditInfo {

    long getId();

    void activate(); // resume

    void deactivate(); // suspend

    RelativePeriod getExportPeriod();

    Optional<RelativePeriod> getUpdatePeriod(); // checks for updates in this period

    Optional<Instant> getLastRun();

    EndDeviceGroup getEndDeviceGroup();

    Map<String, Object> getProperties();

    Instant getNextExecution();

    List<? extends DataExportOccurrence> getOccurrences();

    DataExportOccurrenceFinder getOccurrencesFinder();

    DataExportStrategy getStrategy();

    Set<ReadingType> getReadingTypes();

    void save();

    void delete();

    boolean canBeDeleted();

    boolean isActive();

    String getDataFormatter();

    List<PropertySpec> getPropertySpecs();

    ScheduleExpression getScheduleExpression();

    Optional<? extends DataExportOccurrence> getLastOccurrence();

    Optional<? extends DataExportOccurrence> getOccurrence(Long id);

    List<? extends ReadingTypeDataExportItem> getExportItems();

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

    void triggerNow();

    void updateLastRun(Instant triggerTime);

    /**
     * @since v1.1
     */
    History<ReadingTypeDataExportTask> getHistory();

    Map<String, Object> getProperties(Instant at);

    Set<ReadingType> getReadingTypes(Instant at);

    Optional<ScheduleExpression> getScheduleExpression(Instant at);
}
