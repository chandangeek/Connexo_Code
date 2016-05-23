package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.RelativePeriodCategory;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.ExceptionCatcher;

import javax.inject.Inject;
import java.util.EnumSet;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.elster.jupiter.time.DefaultRelativePeriodDefinition.*;

class InstallerImpl implements FullInstaller {

    private static final Logger LOGGER = Logger.getLogger(InstallerImpl.class.getName());

    public static final String DESTINATION_NAME = EstimationServiceImpl.DESTINATION_NAME;
    public static final String SUBSCRIBER_NAME = EstimationServiceImpl.SUBSCRIBER_NAME;
    public static final String RELATIVE_PERIOD_CATEGORY = "relativeperiod.category.estimation";

    private final DataModel dataModel;
    private final MessageService messageService;
    private final TimeService timeService;
    private final EventService eventService;
    private final UserService userService;

    private DestinationSpec destinationSpec;

    @Inject
    InstallerImpl(DataModel dataModel, MessageService messageService, TimeService timeService, EventService eventService, UserService userService) {
        this.dataModel = dataModel;
        this.messageService = messageService;
        this.timeService = timeService;
        this.eventService = eventService;
        this.userService = userService;
    }

    public DestinationSpec getDestinationSpec() {
        return destinationSpec;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader) {
        ExceptionCatcher.executing(
                () -> dataModelUpgrader.upgrade(dataModel, Version.latest()),
                this::createDestinationAndSubscriber,
                this::createRelativePeriodCategory,
                this::createRelativePeriods,
                this::createEventTypes,
                this::createTaskExecutorUser
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

    private RelativePeriodCategory getCategory() {
        return timeService.findRelativePeriodCategoryByName(RELATIVE_PERIOD_CATEGORY).orElseThrow(IllegalArgumentException::new);
    }

    private void createRelativePeriods() {
        RelativePeriodCategory category = getCategory();

        EnumSet.of(LAST_7_DAYS, PREVIOUS_MONTH, PREVIOUS_WEEK, THIS_MONTH, THIS_WEEK, THIS_YEAR, TODAY, YESTERDAY).stream()
                .forEach(definition -> {
                    RelativePeriod relativePeriod = timeService.findRelativePeriodByName(definition.getPeriodName()).orElseThrow(IllegalArgumentException::new);
                    relativePeriod.addRelativePeriodCategory(category);
                });

    }
    
    private void createEventTypes() {
        for (EventType eventType : EventType.values()) {
            eventType.install(eventService);
        }
    }

    private void createTaskExecutorUser() {
        User estimationUser = userService.createUser(EstimationServiceImpl.ESTIMATION_TASKS_USER, "task executor for estimation tasks");
        Optional<Group> batchExecutorRole = userService.findGroup(UserService.BATCH_EXECUTOR_ROLE);
        if (batchExecutorRole.isPresent()) {
            estimationUser.join(batchExecutorRole.get());
        } else {
            LOGGER.log(Level.SEVERE, "Could not add role to '" + EstimationServiceImpl.ESTIMATION_TASKS_USER + "' user because role '" + UserService.BATCH_EXECUTOR_ROLE + "' is not found");
        }
    }
}
