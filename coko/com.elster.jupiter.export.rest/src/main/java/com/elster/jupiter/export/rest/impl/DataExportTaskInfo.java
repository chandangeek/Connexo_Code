package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.export.DataExportDestination;
import com.elster.jupiter.export.ExportTask;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.elster.jupiter.time.PeriodicalScheduleExpression;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.time.rest.PeriodicalExpressionInfo;
import com.elster.jupiter.util.time.Never;
import com.elster.jupiter.util.time.ScheduleExpression;

import javax.xml.bind.annotation.XmlRootElement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@XmlRootElement
public class DataExportTaskInfo {

    public long id = 0;
    public boolean active = true;
    public String name = "name";
    public ProcessorInfo dataProcessor;
    public ProcessorInfo dataSelector;
    public PeriodicalExpressionInfo schedule;
    public List<PropertyInfo> properties = new ArrayList<PropertyInfo>();
    public DataExportTaskHistoryInfo lastExportOccurrence;
    public Long nextRun;
    public Long lastRun;
    public DataSelectorInfo dataSelectorInfo;
    public List<DestinationInfo> destinations = new ArrayList<>();


    public DataExportTaskInfo(ExportTask dataExportTask, Thesaurus thesaurus, TimeService timeService, PropertyUtils propertyUtils) {
        doPopulate(dataExportTask, thesaurus, timeService);
        if (Never.NEVER.equals(dataExportTask.getScheduleExpression())) {
            schedule = null;
        } else {
            ScheduleExpression scheduleExpression = dataExportTask.getScheduleExpression();
            if (scheduleExpression instanceof TemporalExpression) {
                schedule = new PeriodicalExpressionInfo((TemporalExpression) scheduleExpression);
            } else {
                schedule = PeriodicalExpressionInfo.from((PeriodicalScheduleExpression) scheduleExpression);
            }
        }
        properties = propertyUtils.convertPropertySpecsToPropertyInfos(dataExportTask.getPropertySpecs(), dataExportTask.getProperties());
        lastExportOccurrence = dataExportTask.getLastOccurrence().map(oc -> new DataExportTaskHistoryInfo(oc, thesaurus, timeService, propertyUtils)).orElse(null);
        dataExportTask.getDestinations().stream()
                .forEach(destination -> destinations.add(typeOf(destination).toInfo(destination)));
    }

    private DestinationType typeOf(DataExportDestination destination) {
        return Arrays.stream(DestinationType.values())
                .filter(type -> type.getDestinationClass().isInstance(destination))
                .findAny()
                .orElseThrow(IllegalArgumentException::new);
    }

    public void populate(ExportTask dataExportTask, Thesaurus thesaurus, TimeService timeService) {
        doPopulate(dataExportTask, thesaurus, timeService);
    }

    private void doPopulate(ExportTask dataExportTask, Thesaurus thesaurus, TimeService timeService) {
        id = dataExportTask.getId();
        name = dataExportTask.getName();

        active = dataExportTask.isActive();
        populateReadingTypeDataExport(dataExportTask, thesaurus);

        String dataFormatter = dataExportTask.getDataFormatter();
        dataProcessor = new ProcessorInfo(dataFormatter, thesaurus.getStringBeyondComponent(dataFormatter, dataFormatter), Collections.<PropertyInfo>emptyList()) ;

        String selector = dataExportTask.getDataSelector();
        dataSelector = new ProcessorInfo(selector, thesaurus.getStringBeyondComponent(selector, selector), Collections.emptyList());
//TODO above : pass correct property info
        Instant nextExecution = dataExportTask.getNextExecution();
        if (nextExecution != null) {
            nextRun = nextExecution.toEpochMilli();
        }
        Optional<Instant> lastRunOptional = dataExportTask.getLastRun();
        if (lastRunOptional.isPresent()) {
            lastRun = lastRunOptional.get().toEpochMilli();
        }
    }

    private void populateReadingTypeDataExport(ExportTask dataExportTask, Thesaurus thesaurus) {
        dataExportTask.getReadingTypeDataSelector()
                .ifPresent(readingTypeDataSelector -> {
                    dataSelectorInfo = new DataSelectorInfo(readingTypeDataSelector, thesaurus);
                });
    }

    public DataExportTaskInfo() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return id == ((DataExportTaskInfo) o).id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
