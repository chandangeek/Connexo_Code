package com.elster.jupiter.export;

import com.elster.jupiter.orm.HasAuditInfo;
import com.elster.jupiter.orm.History;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.HasName;
import com.elster.jupiter.util.time.ScheduleExpression;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ExportTask extends HasName, HasAuditInfo {
    long getId();

    Optional<Instant> getLastRun();

    Map<String, Object> getProperties();

    Instant getNextExecution();

    List<? extends DataExportOccurrence> getOccurrences();

    DataExportOccurrenceFinder getOccurrencesFinder();

    void update();

    void delete();

    boolean canBeDeleted();

    boolean isActive();

    String getDataFormatter();

    String getDataSelector();

    List<PropertySpec> getPropertySpecs();

    List<PropertySpec> getDataSelectorPropertySpecs();

    List<PropertySpec> getDataProcessorPropertySpecs();

    ScheduleExpression getScheduleExpression();

    Optional<? extends DataExportOccurrence> getLastOccurrence();

    Optional<? extends DataExportOccurrence> getOccurrence(Long id);

    void setNextExecution(Instant instant);

    void setScheduleExpression(ScheduleExpression scheduleExpression);

    void setName(String name);

    void setProperty(String key, Object value);

    void triggerNow();

    void updateLastRun(Instant triggerTime);

    /**
     * @since v1.1
     */
    History<ExportTask> getHistory();

    Map<String, Object> getProperties(Instant at);

    Optional<ScheduleExpression> getScheduleExpression(Instant at);

    Optional<ReadingTypeDataSelector> getReadingTypeDataSelector();

    Optional<EventDataSelector> getEventDataSelector();

    Optional<ReadingTypeDataSelector> getReadingTypeDataSelector(Instant at);

    FileDestination addFileDestination(String fileLocation, String fileName, String fileExtension);

    EmailDestination addEmailDestination(String recipients, String subject, String attachmentName, String attachmentExtension);

    FtpDestination addFtpDestination(String server, int port, String user, String password, String fileLocation, String fileName, String fileExtension);

    FtpsDestination addFtpsDestination(String server, int port, String user, String password, String fileLocation, String fileName, String fileExtension);

    void removeDestination(DataExportDestination destination);

    List<DataExportDestination> getDestinations();

    List<DataExportDestination> getDestinations(Instant at);
}
