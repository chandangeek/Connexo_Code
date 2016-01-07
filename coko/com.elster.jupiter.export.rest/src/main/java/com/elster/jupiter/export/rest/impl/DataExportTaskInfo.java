package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.export.DataExportDestination;
import com.elster.jupiter.export.ExportTask;
import com.elster.jupiter.nls.Thesaurus;
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
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@XmlRootElement
public class DataExportTaskInfo {

    public long id = 0;
    public boolean active = true;
    public String name = "name";
    public ProcessorInfo dataProcessor;
    public SelectorInfo dataSelector;
    public PeriodicalExpressionInfo schedule;
    //public List<PropertyInfo> properties = new ArrayList<PropertyInfo>();
    public DataExportTaskHistoryInfo lastExportOccurrence;
    public Long nextRun;
    public Long lastRun;
    public StandardDataSelectorInfo standardDataSelector;
    public List<DestinationInfo> destinations = new ArrayList<>();
    public long version;
    public String application;


    public DataExportTaskInfo(ExportTask dataExportTask, Thesaurus thesaurus, TimeService timeService, PropertyUtils propertyUtils) {
        doPopulate(dataExportTask, thesaurus, timeService, propertyUtils);
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
        //properties = propertyUtils.convertPropertySpecsToPropertyInfos(dataExportTask.getDataProcessorPropertySpecs(), dataExportTask.getProperties());
        lastExportOccurrence = dataExportTask.getLastOccurrence().map(oc -> new DataExportTaskHistoryInfo(oc, thesaurus, timeService, propertyUtils)).orElse(null);
        dataExportTask.getDestinations().stream()
            .forEach(destination -> destinations.add(typeOf(destination).toInfo(destination)));
    }

    protected DestinationType typeOf(DataExportDestination destination) {
        return Arrays.stream(DestinationType.values())
                .filter(type -> type.getDestinationClass().isInstance(destination))
                .findAny()
                .orElseThrow(IllegalArgumentException::new);
    }

    public void populate(ExportTask dataExportTask, Thesaurus thesaurus, TimeService timeService, PropertyUtils propertyUtils) {
        doPopulate(dataExportTask, thesaurus, timeService, propertyUtils);
    }

    protected void doPopulate(ExportTask dataExportTask, Thesaurus thesaurus, TimeService timeService, PropertyUtils propertyUtils) {
        id = dataExportTask.getId();
        name = dataExportTask.getName();

        active = dataExportTask.isActive();
        String selector = dataExportTask.getDataSelector();
        SelectorType selectorType = SelectorType.forSelector(selector);
        if (selectorType.equals(SelectorType.DEFAULT_READINGS)) {
            populateReadingTypeDataExport(dataExportTask, thesaurus);
        } else if (selectorType.equals(SelectorType.DEFAULT_EVENTS)){
            populateEventTypeDataExport(dataExportTask,thesaurus);
        }


        String dataFormatter = dataExportTask.getDataFormatter();
        dataProcessor = new ProcessorInfo(dataFormatter, thesaurus.getStringBeyondComponent(dataFormatter, dataFormatter),
                propertyUtils.convertPropertySpecsToPropertyInfos(dataExportTask.getDataProcessorPropertySpecs(), dataExportTask.getProperties())) ;

        dataSelector =
                new SelectorInfo(
                        selector,
                        thesaurus.getStringBeyondComponent(selector, selector),
                        propertyUtils.convertPropertySpecsToPropertyInfos(dataExportTask.getDataSelectorPropertySpecs(), dataExportTask.getProperties()),
                        selectorType);
        Instant nextExecution = dataExportTask.getNextExecution();
        if (nextExecution != null) {
            nextRun = nextExecution.toEpochMilli();
        }
        Optional<Instant> lastRunOptional = dataExportTask.getLastRun();
        if (lastRunOptional.isPresent()) {
            lastRun = lastRunOptional.get().toEpochMilli();
        }
        version = dataExportTask.getVersion();
    }

    private void populateReadingTypeDataExport(ExportTask dataExportTask, Thesaurus thesaurus) {
        dataExportTask.getReadingTypeDataSelector()
                .ifPresent(readingTypeDataSelector -> {
                    standardDataSelector = new StandardDataSelectorInfo(readingTypeDataSelector, thesaurus);
                });
    }

    private void populateEventTypeDataExport(ExportTask dataExportTask, Thesaurus thesaurus) {
        dataExportTask.getEventDataSelector()
                .ifPresent(eventDataSelector -> {
                    standardDataSelector = new StandardDataSelectorInfo(eventDataSelector, thesaurus);
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
