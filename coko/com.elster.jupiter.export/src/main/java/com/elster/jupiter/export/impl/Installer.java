package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataExportStatus;
import com.elster.jupiter.export.security.Privileges;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.SimpleNlsKey;
import com.elster.jupiter.nls.SimpleTranslation;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.Translation;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.time.*;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.ExceptionCatcher;

import java.time.DayOfWeek;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

class Installer {

    public static final String DESTINATION_NAME = DataExportServiceImpl.DESTINATION_NAME;
    public static final String SUBSCRIBER_NAME = DataExportServiceImpl.SUBSCRIBER_NAME;
    public static final String RELATIVE_PERIOD_CATEGORY = "relativeperiod.category.dataExport";

    private final DataModel dataModel;
    private final MessageService messageService;
    private final TimeService timeService;
    private final Thesaurus thesaurus;
    private final UserService userService;

    private DestinationSpec destinationSpec;

    Installer(DataModel dataModel, MessageService messageService, TimeService timeService, Thesaurus thesaurus, UserService userService) {
        this.dataModel = dataModel;
        this.messageService = messageService;
        this.timeService = timeService;
        this.thesaurus = thesaurus;
        this.userService = userService;
    }

    public DestinationSpec getDestinationSpec() {
        return destinationSpec;
    }

    void install() {
        ExceptionCatcher.executing(
                this::installDataModel,
                this::createDestinationAndSubscriber,
                this::createRelativePeriodCategory,
                this::createTranslations,
                this::createPrivileges,
                this::createRelativePeriods
        ).andHandleExceptionsWith(Throwable::printStackTrace)
                .execute();
    }

    private void createTranslations() {
        List<Translation> translations = new ArrayList<>(/*MessageSeeds.values().length*/);
        NlsKey categoryKey = SimpleNlsKey.key(DataExportService.COMPONENTNAME, Layer.DOMAIN, RELATIVE_PERIOD_CATEGORY);
        Translation translation = SimpleTranslation.translation(categoryKey, Locale.ENGLISH, "Data Export");
        translations.add(translation);
        for (MessageSeeds messageSeed : MessageSeeds.values()) {
            NlsKey nlsKey = SimpleNlsKey.key(DataExportService.COMPONENTNAME, Layer.DOMAIN, messageSeed.getKey()).defaultMessage(messageSeed.getDefaultFormat());
            translations.add(SimpleTranslation.translation(nlsKey, Locale.ENGLISH, messageSeed.getDefaultFormat()));
        }
        for (DataExportStatus status : DataExportStatus.values()) {
            NlsKey statusKey = SimpleNlsKey.key(DataExportService.COMPONENTNAME, Layer.DOMAIN, status.toString());
            Translation statusTranslation = SimpleTranslation.translation(statusKey, Locale.ENGLISH, status.toString());
            translations.add(statusTranslation);
        }
        thesaurus.addTranslations(translations);
    }

    private void createRelativePeriodCategory() {
        timeService.createRelativePeriodCategory(RELATIVE_PERIOD_CATEGORY);
    }

    private void createDestinationAndSubscriber() {
        QueueTableSpec queueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
        destinationSpec = queueTableSpec.createDestinationSpec(DESTINATION_NAME, 60);
        destinationSpec.save();
        destinationSpec.activate();
        destinationSpec.subscribe(SUBSCRIBER_NAME);
    }

    private void installDataModel() {
        dataModel.install(true, true);
    }

    private void createPrivileges() {
        userService.createResourceWithPrivileges("SYS", "dataExportTask.dataExportTasks", "dataExportTask.dataExportTasks.description", new String[]
                {Privileges.ADMINISTRATE_DATA_EXPORT_TASK, Privileges.VIEW_DATA_EXPORT_TASK, Privileges.UPDATE_DATA_EXPORT_TASK, Privileges.UPDATE_SCHEDULE_DATA_EXPORT_TASK, Privileges.RUN_DATA_EXPORT_TASK});
    }

    private void createRelativePeriods() {
        List<RelativePeriodCategory> categories = new ArrayList<>();
        categories.add(timeService.findRelativePeriodCategoryByName(RELATIVE_PERIOD_CATEGORY).orElseThrow(IllegalArgumentException::new));

        List<RelativeOperation> nowList = new ArrayList<>();
        setEndOfDayTime(nowList);

        // Last 7 days
        List<RelativeOperation> lastSevenDaysFromList = new ArrayList<>();
        lastSevenDaysFromList.add(new RelativeOperation(RelativeField.DAY, RelativeOperator.MINUS, 7));
        setMidnightTime(lastSevenDaysFromList);

        List<RelativeOperation> lastSevenDaysToList = new ArrayList<>();
        lastSevenDaysToList.add(new RelativeOperation(RelativeField.DAY, RelativeOperator.MINUS, 1));
        setEndOfDayTime(lastSevenDaysToList);
        timeService.createRelativePeriod("Last 7 days", new RelativeDate(lastSevenDaysFromList), new RelativeDate(lastSevenDaysToList), categories);

        // Previous month
        List<RelativeOperation> previousMonthFromList = new ArrayList<>();
        previousMonthFromList.add(new RelativeOperation(RelativeField.MONTH, RelativeOperator.MINUS, 1));
        previousMonthFromList.add(new RelativeOperation(RelativeField.DAY, RelativeOperator.EQUAL, 1));
        setMidnightTime(previousMonthFromList);

        List<RelativeOperation> previousMonthToList = new ArrayList<>();
        previousMonthToList.add(new RelativeOperation(RelativeField.MONTH, RelativeOperator.MINUS, 1));
        previousMonthToList.add(new RelativeOperation(RelativeField.DAY, RelativeOperator.EQUAL, RelativeField.LAST_DAY_OF_MONTH));
        setEndOfDayTime(previousMonthToList);
        timeService.createRelativePeriod("Previous month", new RelativeDate(previousMonthFromList), new RelativeDate(previousMonthToList), categories);

        // This month
        List<RelativeOperation> thisMonthFromList = new ArrayList<>();
        thisMonthFromList.add(new RelativeOperation(RelativeField.DAY, RelativeOperator.EQUAL, 1));
        setMidnightTime(thisMonthFromList);
        timeService.createRelativePeriod("This month", new RelativeDate(thisMonthFromList), new RelativeDate(nowList), categories);

        // Previous week
        DayOfWeek firstDayOfWeek = WeekFields.of(Locale.getDefault()).getFirstDayOfWeek();
        List<RelativeOperation> previousWeekFromList = new ArrayList<>();
        previousWeekFromList.add(new RelativeOperation(RelativeField.WEEK, RelativeOperator.MINUS, 1));
        previousWeekFromList.add(new RelativeOperation(RelativeField.DAY_OF_WEEK, RelativeOperator.EQUAL, firstDayOfWeek.getValue()));
        setMidnightTime(previousWeekFromList);

        List<RelativeOperation> previousWeekToList = new ArrayList<>();
        previousWeekToList.add(new RelativeOperation(RelativeField.WEEK, RelativeOperator.MINUS, 1));
        previousWeekToList.add(new RelativeOperation(RelativeField.DAY_OF_WEEK, RelativeOperator.EQUAL,
                firstDayOfWeek.getValue()+6 < 8 ? firstDayOfWeek.getValue()+6 : (firstDayOfWeek.getValue()+6)%7));
        setEndOfDayTime(previousWeekToList);
        timeService.createRelativePeriod("Previous week", new RelativeDate(previousWeekFromList), new RelativeDate(previousWeekToList), categories);

        // This week
        List<RelativeOperation> thisWeekFromList = new ArrayList<>();
        thisWeekFromList.add(new RelativeOperation(RelativeField.DAY_OF_WEEK, RelativeOperator.EQUAL, firstDayOfWeek.getValue()));
        setMidnightTime(thisWeekFromList);
        timeService.createRelativePeriod("This week", new RelativeDate(thisWeekFromList), new RelativeDate(nowList), categories);

        // Yesterday
        List<RelativeOperation> yesterdayFromList = new ArrayList<>();
        yesterdayFromList.add(new RelativeOperation(RelativeField.DAY, RelativeOperator.MINUS, 1));
        setMidnightTime(yesterdayFromList);

        List<RelativeOperation> yesterdayToList = new ArrayList<>();
        yesterdayToList.add(new RelativeOperation(RelativeField.DAY, RelativeOperator.MINUS, 1));
        setEndOfDayTime(yesterdayToList);
        timeService.createRelativePeriod("Yesterday", new RelativeDate(yesterdayFromList), new RelativeDate(yesterdayToList), categories);

        // Today
        List<RelativeOperation> todayFromList = new ArrayList<>();
        setMidnightTime(todayFromList);
        timeService.createRelativePeriod("Today", new RelativeDate(todayFromList), new RelativeDate(nowList), categories);
    }

    private void setMidnightTime(List<RelativeOperation> operationList) {
        RelativeOperation midnightHours = new RelativeOperation(RelativeField.HOUR, RelativeOperator.EQUAL, 0);
        RelativeOperation midnightMinutes = new RelativeOperation(RelativeField.MINUTES, RelativeOperator.EQUAL, 0);
        operationList.add(midnightHours);
        operationList.add(midnightMinutes);
    }

    private void setEndOfDayTime(List<RelativeOperation> operationList) {
        RelativeOperation endOfDayHours = new RelativeOperation(RelativeField.HOUR, RelativeOperator.EQUAL, 23);
        RelativeOperation endOfDayMinutes = new RelativeOperation(RelativeField.MINUTES, RelativeOperator.EQUAL, 59);
        RelativeOperation endOfDaySeconds = new RelativeOperation(RelativeField.SECONDS, RelativeOperator.EQUAL, 59);
        RelativeOperation endOfDayMillis = new RelativeOperation(RelativeField.MILLIS, RelativeOperator.EQUAL, 59);
        operationList.add(endOfDayHours);
        operationList.add(endOfDayMinutes);
        operationList.add(endOfDaySeconds);
        operationList.add(endOfDayMillis);
    }
}
