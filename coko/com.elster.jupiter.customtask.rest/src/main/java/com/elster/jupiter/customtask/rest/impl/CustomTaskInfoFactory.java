/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.customtask.rest.impl;

import com.elster.jupiter.customtask.CustomTask;
import com.elster.jupiter.customtask.CustomTaskOccurrence;
import com.elster.jupiter.customtask.CustomTaskService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.time.PeriodicalScheduleExpression;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.time.rest.PeriodicalExpressionInfo;
import com.elster.jupiter.util.time.Never;
import com.elster.jupiter.util.time.ScheduleExpression;

import javax.inject.Inject;
import javax.inject.Provider;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public class CustomTaskInfoFactory {

    private final Thesaurus thesaurus;
    private final TimeService timeService;
    private final PropertyValueInfoService propertyValueInfoService;
    private final Provider<CustomTaskHistoryInfoFactory> customTaskHistoryInfoFactoryProvider;
    private final CustomTaskService customTaskService;


    @Inject
    public CustomTaskInfoFactory(Thesaurus thesaurus, TimeService timeService, PropertyValueInfoService propertyValueInfoService, CustomTaskService customTaskService,
                                 Provider<CustomTaskHistoryInfoFactory> customTaskHistoryInfoFactoryProvider) {
        this.thesaurus = thesaurus;
        this.timeService = timeService;
        this.propertyValueInfoService = propertyValueInfoService;
        this.customTaskHistoryInfoFactoryProvider = customTaskHistoryInfoFactoryProvider;
        this.customTaskService = customTaskService;
    }

    public CustomTaskInfo asInfo(CustomTask customTask) {
        CustomTaskInfo info = asMinimalInfo(customTask);
        info.lastOccurrence = customTask.getLastOccurrence().map(oc -> customTaskHistoryInfoFactoryProvider.get().asInfo(oc)).orElse(null);
        return info;
    }

    public CustomTaskInfo asInfoWithMinimalHistory(CustomTask customTask) {
        CustomTaskInfo info = asMinimalInfo(customTask);
        info.lastOccurrence = customTask.getLastOccurrence().map(oc -> customTaskHistoryInfoFactoryProvider.get().asMinimalInfo(oc)).orElse(null);
        return info;
    }

    private CustomTaskInfo asMinimalInfo(CustomTask customTask) {
        CustomTaskInfo info = asInfoWithoutHistory(customTask);
        if (Never.NEVER.equals(customTask.getScheduleExpression())) {
            info.schedule = null;
            info.recurrence = thesaurus.getFormat(TranslationKeys.NONE).format();
        } else {
            ScheduleExpression scheduleExpression = customTask.getScheduleExpression();
            if (scheduleExpression instanceof TemporalExpression) {
                info.schedule = new PeriodicalExpressionInfo((TemporalExpression) scheduleExpression);
                info.recurrence = fromTemporalExpression((TemporalExpression) scheduleExpression);
            } else {
                info.schedule = PeriodicalExpressionInfo.from((PeriodicalScheduleExpression) scheduleExpression);
                info.recurrence = fromPeriodicalScheduleExpression((PeriodicalScheduleExpression) scheduleExpression);
            }
        }
        return info;
    }


    public CustomTaskInfo asInfoWithHistory(CustomTask customTask, CustomTaskOccurrence customTaskOccurrence) {
        CustomTaskInfo info = new CustomTaskInfo();
        Instant versionAt = customTaskOccurrence.getTriggerTime();

        info.id = customTask.getId();
        info.name = customTask.getName();
        info.active = customTask.isActive();
        info.logLevel = customTaskOccurrence.getRecurrentTask().getLogLevel(versionAt);
        info.properties = customTask.getPropertySpecs().stream()
                .map(propertiesInfo -> {
                    return new CustomTaskPropertiesInfo(propertiesInfo.getName(),
                            propertiesInfo.getDisplayName(),
                            propertyValueInfoService.getPropertyInfos(propertiesInfo.getProperties(), customTask.getValues(versionAt)));

                })
                .collect(Collectors.toList());
        info.nextRecurrentTasks = constructTaskInfo(customTask.getNextRecurrentTasks());
        info.previousRecurrentTasks = constructTaskInfo(customTask.getPrevRecurrentTasks());

        Instant nextExecution = customTask.getNextExecution();
        if (nextExecution != null) {
            info.nextRun = nextExecution;
        }
        customTask.getLastRun().ifPresent(lastRun -> info.lastRun = lastRun);
        info.version = customTask.getVersion();
        return info;
    }

    public CustomTaskInfo asInfoWithoutHistory(CustomTask customTask) {
        CustomTaskInfo info = new CustomTaskInfo();
        info.id = customTask.getId();
        info.name = customTask.getName();
        info.logLevel = customTask.getLogLevel();
        info.active = customTask.isActive();
        info.nextRecurrentTasks = constructTaskInfo(customTask.getNextRecurrentTasks());
        info.previousRecurrentTasks = constructTaskInfo(customTask.getPrevRecurrentTasks());
        info.properties = customTask.getPropertySpecs().stream()
                .map(propertiesInfo -> {
                    return new CustomTaskPropertiesInfo(propertiesInfo.getName(),
                            propertiesInfo.getDisplayName(),
                            propertyValueInfoService.getPropertyInfos(propertiesInfo.getProperties(), customTask.getValues()));

                })
                .collect(Collectors.toList());


        Instant nextExecution = customTask.getNextExecution();
        if (nextExecution != null) {
            info.nextRun = nextExecution;
        }
        customTask.getLastRun().ifPresent(lastRun -> info.lastRun = lastRun);
        info.version = customTask.getVersion();
        return info;
    }

    private String fromTemporalExpression(TemporalExpression scheduleExpression) {
        return timeService.toLocalizedString(scheduleExpression);
    }

    private String fromPeriodicalScheduleExpression(PeriodicalScheduleExpression scheduleExpression) {
        return timeService.toLocalizedString(scheduleExpression);
    }

    private List<TaskInfo> constructTaskInfo(List<RecurrentTask> recurrentTasks) {
        return recurrentTasks.stream().map(recurrentTask -> TaskInfo.from(recurrentTask)).collect(Collectors.toList());
    }
}
