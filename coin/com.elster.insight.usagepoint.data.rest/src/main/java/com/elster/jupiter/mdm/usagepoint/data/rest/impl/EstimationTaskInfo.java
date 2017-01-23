package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.estimation.EstimationTask;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.time.PeriodicalScheduleExpression;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.time.rest.PeriodicalExpressionInfo;
import com.elster.jupiter.util.time.Never;
import com.elster.jupiter.util.time.ScheduleExpression;

import java.time.Instant;

public class EstimationTaskInfo {
    public long id;
    public String name;
    public String recurrence;
    public PeriodicalExpressionInfo schedule;
    public Long nextRun;

    public EstimationTaskInfo(EstimationTask estimationTask, Thesaurus thesaurus, TimeService timeService) {
        id = estimationTask.getId();
        name = estimationTask.getName();
        Instant nextExecution = estimationTask.getNextExecution();
        if (nextExecution != null) {
            nextRun = nextExecution.toEpochMilli();
        }
        ScheduleExpression scheduleExpression = estimationTask.getScheduleExpression();
        if (Never.NEVER.equals(scheduleExpression)) {
            schedule = null;
            recurrence = thesaurus.getFormat(DefaultTranslationKey.NONE).format();
        } else {
            if (scheduleExpression instanceof TemporalExpression) {
                schedule = new PeriodicalExpressionInfo((TemporalExpression) scheduleExpression);
                recurrence = timeService.toLocalizedString((TemporalExpression) scheduleExpression);;
            } else {
                schedule = PeriodicalExpressionInfo.from((PeriodicalScheduleExpression) scheduleExpression);
                recurrence = timeService.toLocalizedString((PeriodicalScheduleExpression) scheduleExpression);;
            }
        }
    }
}
