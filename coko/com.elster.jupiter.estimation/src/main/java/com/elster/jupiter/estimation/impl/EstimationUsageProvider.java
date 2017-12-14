package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.EstimationTask;

import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.RelativePeriodUsageInfo;


import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.time.RelativePeriodUsageProvider;

import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.Component;

import java.util.ArrayList;
import java.util.List;

@Component(service = RelativePeriodUsageProvider.class)
public class EstimationUsageProvider implements RelativePeriodUsageProvider {

    private volatile EstimationService estimationService;
    private volatile TimeService timeService;

    @Reference
    public void setEstimationService(EstimationService estimationService) {
        this.estimationService = estimationService;
    }

    @Reference
    public void setTimeService(TimeService timeService) {
        this.timeService = timeService;
    }

    @Override
    public List<RelativePeriodUsageInfo> getUsageReferences(long relativePeriodId) {

        List<RelativePeriodUsageInfo> infos = new ArrayList<>();
        for (QualityCodeSystem code : QualityCodeSystem.values()) {
            estimationService.findEstimationTasks(code)
                    .stream()
                    .filter(task -> this.matchesRelativePeriod(task, relativePeriodId))
                    .map(this::createRelativePeriodUsageInfo)
                    .forEach(infos::add);
        }
        return infos;
    }

    private boolean matchesRelativePeriod(EstimationTask task, long relativePeriodId) {
        return task.getPeriod()
                .map(RelativePeriod::getId)
                .filter(id -> id == relativePeriodId)
                .isPresent();
    }

    @Override
    public String getType() {
        return TranslationKeys.RELATIVE_PERIOD_CATEGORY.getKey();
    }

    private RelativePeriodUsageInfo createRelativePeriodUsageInfo(EstimationTask estimationTask) {
        return new RelativePeriodUsageInfo(
                estimationTask.getName(),
                timeService.findRelativePeriodCategoryDisplayName(this.getType()),
                ((IEstimationTask) estimationTask).getRecurrentTask().getApplication(),
                estimationTask.getNextExecution());
    }

}
