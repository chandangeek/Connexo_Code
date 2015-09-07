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

class Installer {

    public static final String DESTINATION_NAME = DataExportServiceImpl.DESTINATION_NAME;
    public static final String SUBSCRIBER_NAME = DataExportServiceImpl.SUBSCRIBER_NAME;
    public static final String RELATIVE_PERIOD_CATEGORY = "relativeperiod.category.dataExport";

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

    private RelativePeriodCategory getCategory() {
        return timeService.findRelativePeriodCategoryByName(RELATIVE_PERIOD_CATEGORY).orElseThrow(IllegalArgumentException::new);
    }

    private void createRelativePeriods() {
        RelativePeriodCategory category = getCategory();

        EnumSet.of(LAST_7_DAYS, PREVIOUS_MONTH, PREVIOUS_WEEK, THIS_MONTH, THIS_WEEK, TODAY, YESTERDAY).stream()
                .forEach(definition -> {
                    RelativePeriod relativePeriod = timeService.findRelativePeriodByName(definition.getPeriodName()).orElseThrow(IllegalArgumentException::new);
                    relativePeriod.addRelativePeriodCategory(category);
                });
    }
}
