/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.security.Privileges;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.metering.GasDayOptions;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.RelativePeriodCategory;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import static com.elster.jupiter.time.DefaultRelativePeriodDefinition.LAST_7_DAYS;
import static com.elster.jupiter.time.DefaultRelativePeriodDefinition.PREVIOUS_MONTH;
import static com.elster.jupiter.time.DefaultRelativePeriodDefinition.PREVIOUS_WEEK;
import static com.elster.jupiter.time.DefaultRelativePeriodDefinition.THIS_MONTH;
import static com.elster.jupiter.time.DefaultRelativePeriodDefinition.THIS_WEEK;
import static com.elster.jupiter.time.DefaultRelativePeriodDefinition.THIS_YEAR;
import static com.elster.jupiter.time.DefaultRelativePeriodDefinition.TODAY;
import static com.elster.jupiter.time.DefaultRelativePeriodDefinition.YESTERDAY;
import static com.elster.jupiter.util.streams.Currying.perform;

class InstallerImpl implements FullInstaller, PrivilegesProvider {

    private static final String DESTINATION_NAME = EstimationServiceImpl.DESTINATION_NAME;
    public static final String SUBSCRIBER_NAME = EstimationServiceImpl.SUBSCRIBER_NAME;
    private static final String RELATIVE_PERIOD_CATEGORY = "relativeperiod.category.estimation";

    private final DataModel dataModel;
    private final MessageService messageService;
    private final TimeService timeService;
    private final EventService eventService;
    private final UserService userService;
    private final MeteringService meteringService;

    private DestinationSpec destinationSpec;

    @Inject
    InstallerImpl(DataModel dataModel, MessageService messageService, TimeService timeService, EventService eventService, UserService userService, MeteringService meteringService) {
        this.dataModel = dataModel;
        this.messageService = messageService;
        this.timeService = timeService;
        this.eventService = eventService;
        this.userService = userService;
        this.meteringService = meteringService;
    }

    public DestinationSpec getDestinationSpec() {
        return destinationSpec;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());

        doTry(
                "Create estimation task queue",
                this::createDestinationAndSubscriber,
                logger
        );
        doTry(
                "Create relative period category for EST",
                this::createRelativePeriodCategory,
                logger
        );
        doTry(
                "Assign default relative periods to EST category",
                this::createRelativePeriods,
                logger
        );
        doTry(
                "Create event types for EST",
                this::createEventTypes,
                logger
        );
        doTry(
                "Create task execution user",
                this::createTaskExecutorUser,
                logger
        );
        userService.addModulePrivileges(this);
    }

    private void createRelativePeriodCategory() {
        timeService.createRelativePeriodCategory(RELATIVE_PERIOD_CATEGORY);
    }

    private void createDestinationAndSubscriber() {
        QueueTableSpec queueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
        destinationSpec = queueTableSpec.createDestinationSpec(DESTINATION_NAME, 60);
        destinationSpec.save();
        destinationSpec.activate();
        destinationSpec.subscribe(TranslationKeys.SUBSCRIBER_NAME, EstimationService.COMPONENTNAME, Layer.DOMAIN);
    }

    @Override
    public String getModuleName() {
        return EstimationService.COMPONENTNAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        List<ResourceDefinition> resources = new ArrayList<>();
        resources.add(userService.createModuleResourceWithPrivileges(EstimationService.COMPONENTNAME, Privileges.RESOURCE_ESTIMATION_RULES.getKey(), Privileges.RESOURCE_ESTIMATION_RULES_DESCRIPTION.getKey(),
                Arrays.asList(
                        Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION, Privileges.Constants.VIEW_ESTIMATION_CONFIGURATION,
                        Privileges.Constants.UPDATE_ESTIMATION_CONFIGURATION,Privileges.Constants.UPDATE_SCHEDULE_ESTIMATION_TASK,
                        Privileges.Constants.RUN_ESTIMATION_TASK, Privileges.Constants.VIEW_ESTIMATION_TASK,
                        Privileges.Constants.ADMINISTRATE_ESTIMATION_TASK, Privileges.Constants.FINE_TUNE_ESTIMATION_CONFIGURATION_ON_DEVICE,
                        Privileges.Constants.FINE_TUNE_ESTIMATION_CONFIGURATION_ON_DEVICE_CONFIGURATION)));
        return resources;
    }


    private RelativePeriodCategory getCategory() {
        return timeService.findRelativePeriodCategoryByName(RELATIVE_PERIOD_CATEGORY)
                .orElseThrow(IllegalArgumentException::new);
    }

    private void createRelativePeriods() {
        RelativePeriodCategory category = getCategory();

        EnumSet.of(LAST_7_DAYS, PREVIOUS_MONTH, PREVIOUS_WEEK, THIS_MONTH, THIS_WEEK, THIS_YEAR, TODAY, YESTERDAY)
                .forEach(definition -> {
                    RelativePeriod relativePeriod = timeService.findRelativePeriodByName(definition.getPeriodName())
                            .orElseThrow(IllegalArgumentException::new);
                    relativePeriod.addRelativePeriodCategory(category);
                });

        this.meteringService
                .getGasDayOptions()
                .ifPresent(perform(this::createGasRelativePeriods).with(category));
    }

    private void createGasRelativePeriods(GasDayOptions gasDayOptions, RelativePeriodCategory category) {
        gasDayOptions
                .getRelativePeriods()
                .forEach(relativePeriod -> relativePeriod.addRelativePeriodCategory(category));
    }

    private void createEventTypes() {
        for (EventType eventType : EventType.values()) {
            eventType.install(eventService);
        }
    }

    private void createTaskExecutorUser() {
        User estimationUser = userService.createUser(EstimationServiceImpl.ESTIMATION_TASKS_USER, "task executor for estimation tasks");
        Optional<Group> batchExecutorRole = userService.findGroup(UserService.BATCH_EXECUTOR_ROLE);
        estimationUser.join(batchExecutorRole.orElseThrow(() -> new IllegalStateException("Role " + UserService.BATCH_EXECUTOR_ROLE + "is not found.")));
    }
}
