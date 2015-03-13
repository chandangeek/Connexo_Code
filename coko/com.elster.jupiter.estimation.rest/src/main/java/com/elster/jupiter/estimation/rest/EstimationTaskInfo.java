package com.elster.jupiter.estimation.rest;

import com.elster.jupiter.estimation.EstimationTask;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.time.rest.PeriodicalExpressionInfo;
import com.elster.jupiter.time.rest.RelativePeriodInfo;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EstimationTaskInfo {

    public long id = 0;
    public boolean active = true;
    public String name = "name";
    public PeriodicalExpressionInfo schedule;
    public RelativePeriodInfo period;
    public List<ReadingTypeInfo> readingTypes = new ArrayList<ReadingTypeInfo>();
    public MeterGroupInfo deviceGroup;
    public EstimationTaskHistoryInfo lastExportOccurrence;
    public Long nextRun;
    public Long lastRun;

    public EstimationTaskInfo(EstimationTask estimationTask, Thesaurus thesaurus, TimeService timeService) {
        doPopulate(estimationTask, thesaurus, timeService);
    }

    private void doPopulate(EstimationTask estimationTask, Thesaurus thesaurus, TimeService timeService) {
        id = estimationTask.getId();
        name = estimationTask.getName();

        deviceGroup = new MeterGroupInfo(estimationTask.getEndDeviceGroup());
        active = estimationTask.isActive();
        estimationTask.getPeriod().ifPresent(period -> this.period = new RelativePeriodInfo(period, thesaurus));

        Instant nextExecution = estimationTask.getNextExecution();
        if (nextExecution != null) {
            nextRun = nextExecution.toEpochMilli();
        }
        Optional<Instant> lastRunOptional = estimationTask.getLastRun();
        if (lastRunOptional.isPresent()) {
            lastRun = lastRunOptional.get().toEpochMilli();
        }
    }

}
