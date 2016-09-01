package com.elster.jupiter.validation.rest;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.History;
import com.elster.jupiter.time.PeriodicalScheduleExpression;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.rest.PeriodicalExpressionInfo;
import com.elster.jupiter.util.time.Never;
import com.elster.jupiter.util.time.ScheduleExpression;
import com.elster.jupiter.validation.DataValidationOccurrence;
import com.elster.jupiter.validation.DataValidationTask;
import com.elster.jupiter.validation.DataValidationTaskStatus;

import javax.inject.Inject;
import javax.inject.Provider;
import java.time.Instant;
import java.util.Optional;

public class DataValidationTaskHistoryInfoFactory {

    private final Thesaurus thesaurus;
    private final Provider<DataValidationTaskInfoFactory> dataValidationTaskInfoFactory;

    @Inject
    public DataValidationTaskHistoryInfoFactory(Thesaurus thesaurus, Provider<DataValidationTaskInfoFactory> dataValidationTaskInfoFactory) {
        this.thesaurus = thesaurus;
        this.dataValidationTaskInfoFactory = dataValidationTaskInfoFactory;
    }

    public DataValidationTaskHistoryInfo asInfo(DataValidationOccurrence dataValidationOccurrence) {
        return asInfo((History<DataValidationTask>) dataValidationOccurrence.getTask().getHistory(), dataValidationOccurrence);
    }

    public DataValidationTaskHistoryInfo asInfo(History<DataValidationTask> history, DataValidationOccurrence dataValidationOccurrence) {
        DataValidationTaskHistoryInfo info = new DataValidationTaskHistoryInfo();
        info.id = dataValidationOccurrence.getId();
        info.startedOn = dataValidationOccurrence.getStartDate().map(this::toLong).orElse(null);
        info.finishedOn = dataValidationOccurrence.getEndDate().map(this::toLong).orElse(null);
        info.duration = calculateDuration(info.startedOn, info.finishedOn);
        info.status = getName(dataValidationOccurrence.getStatus(), thesaurus);
        info.reason = dataValidationOccurrence.getFailureReason();
        info.lastRun = dataValidationOccurrence.getTriggerTime().toEpochMilli();
        setStatusOnDate(info, dataValidationOccurrence);
        DataValidationTask version = history.getVersionAt(dataValidationOccurrence.getTriggerTime())
                .orElseGet(() -> history.getVersionAt(dataValidationOccurrence.getTask().getCreateTime())
                        .orElseGet(dataValidationOccurrence::getTask));
        info.task = dataValidationTaskInfoFactory.get().asInfo(version);
        Optional<ScheduleExpression> foundSchedule = version.getScheduleExpression(dataValidationOccurrence.getTriggerTime());
        if (!foundSchedule.isPresent() || Never.NEVER.equals(foundSchedule.get())) {
            info.task.schedule = null;
        } else if (foundSchedule.isPresent()) {
            ScheduleExpression scheduleExpression = foundSchedule.get();
            if (scheduleExpression instanceof TemporalExpression) {
                info.task.schedule = new PeriodicalExpressionInfo((TemporalExpression) scheduleExpression);
            } else {
                info.task.schedule = PeriodicalExpressionInfo.from((PeriodicalScheduleExpression) scheduleExpression);
            }
        }
        return info;
    }

    private void setStatusOnDate(DataValidationTaskHistoryInfo info, DataValidationOccurrence dataValidationOccurrence) {
        DataValidationTaskStatus dataExportStatus = dataValidationOccurrence.getStatus();
        String statusTranslation =
                thesaurus.getStringBeyondComponent(dataExportStatus.toString(), dataExportStatus.toString());
        if (DataValidationTaskStatus.BUSY.equals(dataExportStatus)) {
            info.statusPrefix = statusTranslation + " " + thesaurus.getString("since", "since");
            info.statusDate = info.startedOn;
        } else if ((DataValidationTaskStatus.FAILED.equals(dataExportStatus)) || (DataValidationTaskStatus.SUCCESS.equals(dataExportStatus))) {
            info.statusPrefix = statusTranslation + " " + thesaurus.getString("on", "on");
            info.statusDate = info.finishedOn;
        } else {
            info.statusPrefix = statusTranslation;
        }
    }

    private static Long calculateDuration(Long startedOn, Long finishedOn) {
        if (startedOn == null || finishedOn == null) {
            return null;
        }
        return finishedOn - startedOn;
    }

    private Long toLong(Instant instant) {
        return instant == null ? null : instant.toEpochMilli();
    }

    private static String getName(DataValidationTaskStatus status, Thesaurus thesaurus) {
        return thesaurus.getStringBeyondComponent(status.toString(), status.toString());
    }
}
