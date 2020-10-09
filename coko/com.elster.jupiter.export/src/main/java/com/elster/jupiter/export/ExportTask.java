/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export;

import com.elster.jupiter.orm.HasAuditInfo;
import com.elster.jupiter.orm.History;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;
import com.elster.jupiter.util.time.ScheduleExpression;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ProviderType
public interface ExportTask extends HasId, HasName, HasAuditInfo {
    Optional<Instant> getLastRun();

    Map<String, Object> getProperties();

    Instant getNextExecution();

    List<? extends DataExportOccurrence> getOccurrences();

    DataExportOccurrenceFinder getOccurrencesFinder();

    void update();

    void delete();

    boolean canBeDeleted();

    boolean isActive();

    DataFormatterFactory getDataFormatterFactory();

    DataSelectorFactory getDataSelectorFactory();

    List<PropertySpec> getPropertySpecs();

    List<PropertySpec> getDataSelectorPropertySpecs();

    List<PropertySpec> getDataFormatterPropertySpecs();

    ScheduleExpression getScheduleExpression();

    Optional<? extends DataExportOccurrence> getLastOccurrence();

    Optional<? extends DataExportOccurrence> getOccurrence(Long id);

    void setNextExecution(Instant instant);

    void setScheduleExpression(ScheduleExpression scheduleExpression);

    void setName(String name);

    void setProperty(String key, Object value);

    void triggerNow();

    void triggerAt(Instant at, Instant trigger);

    void retryNow(DataExportOccurrence dataExportOccurrence);

    void updateLastRun(Instant triggerTime);

    void setDataFormatterFactoryName(String formatter);

    void removeProperty(PropertySpec propertySpec);

    /**
     * @since v1.1
     */
    History<ExportTask> getHistory();

    Map<String, Object> getProperties(Instant at);

    Optional<ScheduleExpression> getScheduleExpression(Instant at);

    Optional<DataSelectorConfig> getStandardDataSelectorConfig();

    Optional<DataSelectorConfig> getStandardDataSelectorConfig(Instant at);

    FileDestination addFileDestination(String fileLocation, String fileName, String fileExtension);

    EmailDestination addEmailDestination(String recipients, String subject, String attachmentName, String attachmentExtension);

    FtpDestination addFtpDestination(String server, int port, String user, String password, String fileLocation, String fileName, String fileExtension);

    FtpsDestination addFtpsDestination(String server, int port, String user, String password, String fileLocation, String fileName, String fileExtension);

    SftpDestination addSftpDestination(String server, int port, String user, String password, String fileLocation, String fileName, String fileExtension);

    WebServiceDestination addWebServiceDestination(EndPointConfiguration createEndPoint, EndPointConfiguration changeEndPoint);

    void removeDestination(DataExportDestination destination);

    List<? extends DataExportDestination> getDestinations();

    List<? extends DataExportDestination> getDestinations(Instant at);

    String getApplication();

    int getLogLevel();

    void setLogLevel(int newLevel);

    void addExportRunParameters(Instant creationDate, Instant exportPeriodStart, Instant exportPeriodEnd, Instant updatePeriodStart, Instant updatePeriodEnd);

    Optional<DataExportRunParameters> getRunParameters(Instant at);

    Instant getSuspendUntil();

    void setSuspendUntil(Instant suspendUntil);

    Optional<ReadingDataSelectorConfig> getReadingDataSelectorConfig();

    Optional<ExportTask> getPairedTask();

    void pairWith(ExportTask exportTask);

    void unpair();
}
