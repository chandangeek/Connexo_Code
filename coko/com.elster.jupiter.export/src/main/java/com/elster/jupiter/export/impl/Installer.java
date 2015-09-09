package com.elster.jupiter.export.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.RelativePeriodCategory;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.ExceptionCatcher;

import java.util.EnumSet;

import static com.elster.jupiter.time.DefaultRelativePeriodDefinition.LAST_7_DAYS;
import static com.elster.jupiter.time.DefaultRelativePeriodDefinition.PREVIOUS_MONTH;
import static com.elster.jupiter.time.DefaultRelativePeriodDefinition.PREVIOUS_WEEK;
import static com.elster.jupiter.time.DefaultRelativePeriodDefinition.THIS_MONTH;
import static com.elster.jupiter.time.DefaultRelativePeriodDefinition.THIS_WEEK;
import static com.elster.jupiter.time.DefaultRelativePeriodDefinition.TODAY;
import static com.elster.jupiter.time.DefaultRelativePeriodDefinition.YESTERDAY;
import static com.elster.jupiter.time.DefaultRelativePeriodDefinition.THIS_YEAR;

class Installer {

    public static final String DESTINATION_NAME = DataExportServiceImpl.DESTINATION_NAME;
    public static final String SUBSCRIBER_NAME = DataExportServiceImpl.SUBSCRIBER_NAME;
    public static final String RELATIVE_PERIOD_CATEGORY = "relativeperiod.category.dataExport";
    public static final String RELATIVE_PERIOD_UPDATEWINDOW_CATEGORY = "relativeperiod.category.updateWindow";
    public static final String RELATIVE_PERIOD_UPDATETIMEFRAME_CATEGORY = "relativeperiod.category.updateTimeframe";

    private final DataModel dataModel;
    private final MessageService messageService;
    private final TimeService timeService;

    private DestinationSpec destinationSpec;

    Installer(DataModel dataModel, MessageService messageService, TimeService timeService) {
        this.dataModel = dataModel;
        this.messageService = messageService;
        this.timeService = timeService;
    }

    public DestinationSpec getDestinationSpec() {
        return destinationSpec;
    }

    void install() {
        ExceptionCatcher.executing(
                this::installDataModel,
                this::createDestinationAndSubscriber,
                this::createRelativePeriodCategory,
                this::createRelativePeriods
        ).andHandleExceptionsWith(Throwable::printStackTrace)
                .execute();
    }

    private void createRelativePeriodCategory() {
        timeService.createRelativePeriodCategory(RELATIVE_PERIOD_CATEGORY);
        timeService.createRelativePeriodCategory(RELATIVE_PERIOD_UPDATEWINDOW_CATEGORY);
        timeService.createRelativePeriodCategory(RELATIVE_PERIOD_UPDATETIMEFRAME_CATEGORY);
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

    private RelativePeriodCategory getCategory(String name) {
        return timeService.findRelativePeriodCategoryByName(name).orElseThrow(IllegalArgumentException::new);
    }

    private void createRelativePeriods() {
        EnumSet.of(LAST_7_DAYS, PREVIOUS_MONTH, PREVIOUS_WEEK, THIS_MONTH, THIS_WEEK, TODAY, YESTERDAY).stream()
                .forEach(definition -> {
                    RelativePeriod relativePeriod = timeService.findRelativePeriodByName(definition.getPeriodName()).orElseThrow(IllegalArgumentException::new);
                    relativePeriod.addRelativePeriodCategory(getCategory(RELATIVE_PERIOD_CATEGORY));
                });

        EnumSet.of(LAST_7_DAYS, PREVIOUS_MONTH, PREVIOUS_WEEK, YESTERDAY).stream()
                .forEach(definition -> {
                    RelativePeriod relativePeriod = timeService.findRelativePeriodByName(definition.getPeriodName()).orElseThrow(IllegalArgumentException::new);
                    relativePeriod.addRelativePeriodCategory(getCategory(RELATIVE_PERIOD_UPDATEWINDOW_CATEGORY));
                });

        EnumSet.of(THIS_MONTH, THIS_WEEK, THIS_YEAR, TODAY).stream()
                .forEach(definition -> {
                    RelativePeriod relativePeriod = timeService.findRelativePeriodByName(definition.getPeriodName()).orElseThrow(IllegalArgumentException::new);
                    relativePeriod.addRelativePeriodCategory(getCategory(RELATIVE_PERIOD_UPDATETIMEFRAME_CATEGORY));
                });


    }
}
