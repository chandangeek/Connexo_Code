package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.estimation.EstimationTask;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.time.PeriodicalScheduleExpression;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.time.rest.PeriodicalExpressionInfo;
import com.elster.jupiter.util.time.Never;
import com.elster.jupiter.util.time.ScheduleExpression;

import javax.inject.Inject;
import java.time.Instant;

public class EstimationTaskInfoFactory {

    private final Thesaurus thesaurus;
    private final TimeService timeService;

    @Inject
    public EstimationTaskInfoFactory(Thesaurus thesaurus, TimeService timeService) {
        this.thesaurus = thesaurus;
        this.timeService = timeService;
    }

    public EstimationTaskShortInfo asInfo(EstimationTask estimationTask) {
        EstimationTaskShortInfo info = new EstimationTaskShortInfo();
        info.id = estimationTask.getId();
        info.name = estimationTask.getName();
        Instant nextExecution = estimationTask.getNextExecution();
        if (nextExecution != null) {
            info.nextRun = nextExecution;
        }
        ScheduleExpression scheduleExpression = estimationTask.getScheduleExpression();
        if (Never.NEVER.equals(scheduleExpression)) {
            info.schedule = null;
            info.recurrence = thesaurus.getFormat(DefaultTranslationKey.NONE).format();
        } else {
            if (scheduleExpression instanceof TemporalExpression) {
                info.schedule = new PeriodicalExpressionInfo((TemporalExpression) scheduleExpression);
                info.recurrence = timeService.toLocalizedString((TemporalExpression) scheduleExpression);;
            } else {
                info.schedule = PeriodicalExpressionInfo.from((PeriodicalScheduleExpression) scheduleExpression);
                info.recurrence = timeService.toLocalizedString((PeriodicalScheduleExpression) scheduleExpression);;
            }
        }
        return info;
    }
}
