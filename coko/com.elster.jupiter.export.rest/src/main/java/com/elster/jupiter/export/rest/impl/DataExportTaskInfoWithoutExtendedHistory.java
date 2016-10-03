package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.export.ExportTask;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.time.PeriodicalScheduleExpression;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.time.rest.PeriodicalExpressionInfo;
import com.elster.jupiter.util.time.Never;
import com.elster.jupiter.util.time.ScheduleExpression;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class DataExportTaskInfoWithoutExtendedHistory extends DataExportTaskInfo {

    public DataTaskHistoryWithoutEmbeddedTaskInfo lastExportOccurrence;

    public DataExportTaskInfoWithoutExtendedHistory(ExportTask dataExportTask, Thesaurus thesaurus, TimeService timeService, PropertyValueInfoService propertyValueInfoService) {
        doPopulate(dataExportTask, thesaurus, propertyValueInfoService);
        if (Never.NEVER.equals(dataExportTask.getScheduleExpression())) {
            schedule = null;
            recurrence = thesaurus.getFormat(TranslationKeys.NONE).format();
        } else {
            ScheduleExpression scheduleExpression = dataExportTask.getScheduleExpression();
            if (scheduleExpression instanceof TemporalExpression) {
                schedule = new PeriodicalExpressionInfo((TemporalExpression) scheduleExpression);
                recurrence = fromTemporalExpression((TemporalExpression) scheduleExpression, timeService);
            } else {
                schedule = PeriodicalExpressionInfo.from((PeriodicalScheduleExpression) scheduleExpression);
                recurrence = fromPeriodicalScheduleExpression((PeriodicalScheduleExpression) scheduleExpression, timeService);
            }
        }
        lastExportOccurrence = dataExportTask.getLastOccurrence().map(oc -> new DataTaskHistoryWithoutEmbeddedTaskInfo(oc, thesaurus, timeService, propertyValueInfoService)).orElse(null);
        dataExportTask.getDestinations().stream()
                .forEach(destination -> destinations.add(typeOf(destination).toInfo(destination)));
    }

}
