/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.EventType;
import com.elster.jupiter.calendar.OutOfTheBoxCategory;
import com.elster.jupiter.calendar.impl.importers.CalendarImporterMessageHandler;
import com.elster.jupiter.calendar.security.Privileges;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

class InstallerImpl implements FullInstaller, PrivilegesProvider {

    private static final int DEFAULT_RETRY_DELAY_IN_SECONDS = 60;

    private final ServerCalendarService calendarService;

    @Inject
    InstallerImpl(ServerCalendarService calendarService) {
        this.calendarService = calendarService;
    }

    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(this.calendarService.getDataModel(), Version.latest());
        doTry(
                "Create default Calendar categories.",
                this::createCategories,
                logger
        );
        doTry(
                "Create event types for CAL.",
                this::createEventTypes,
                logger
        );
        this.calendarService.getUserService().addModulePrivileges(this);
        if (!this.calendarService.getMessageService().getDestinationSpec(CalendarImporterMessageHandler.DESTINATION_NAME).isPresent()) {
            doTry(
                    "Create TOU Calendar import queue",
                    this::createQueue,
                    logger
            );
        }
        doTry(
                "Create vault for CAL.",
                this::createVault,
                logger
        );
        doTry(
                "Create record spec for CAL.",
                this::createRecordSpec,
                logger
        );
        doTry(
                "Create message handlers for CAL.",
                this::createMessageHandlers,
                logger
        );
        doTry(
                "Create recurrent task for CAL.",
                this::createRecurrentTask,
                logger
        );
    }

    private void createCategories() {
        for (OutOfTheBoxCategory outOfTheBoxCategory : OutOfTheBoxCategory.values()) {
            CategoryImpl category = this.calendarService.getDataModel().getInstance(CategoryImpl.class);
            category.init(outOfTheBoxCategory);
            category.save();
        }
    }

    private void createEventTypes() {
        for (EventType eventType : EventType.values()) {
            eventType.install(this.calendarService.getEventService());
        }
    }

    private void createVault() {
        this.calendarService.createVault();
    }

    private void createRecordSpec() {
        this.calendarService.createRecordSpec();
    }

    private void createMessageHandlers() {
        QueueTableSpec defaultQueueTableSpec = this.calendarService.getMessageService().getQueueTableSpec("MSG_RAWQUEUETABLE").get();
        this.createMessageHandler(defaultQueueTableSpec, CalendarTimeSeriesExtenderHandlerFactory.TASK_DESTINATION, TranslationKeys.RECURRENT_TASK);
    }

    private void createMessageHandler(QueueTableSpec defaultQueueTableSpec, String destinationName, TranslationKey subscriberKey) {
        Optional<DestinationSpec> destinationSpecOptional = this.calendarService.getMessageService().getDestinationSpec(destinationName);
        if (!destinationSpecOptional.isPresent()) {
            DestinationSpec queue = defaultQueueTableSpec.createDestinationSpec(destinationName, DEFAULT_RETRY_DELAY_IN_SECONDS);
            queue.activate();
            queue.subscribe(subscriberKey, CalendarService.COMPONENTNAME, Layer.DOMAIN);
        } else {
            boolean notSubscribedYet = !destinationSpecOptional.get()
                    .getSubscribers()
                    .stream()
                    .anyMatch(spec -> spec.getName().equals(subscriberKey.getKey()));
            if (notSubscribedYet) {
                destinationSpecOptional.get().activate();
                destinationSpecOptional.get().subscribe(subscriberKey, CalendarService.COMPONENTNAME, Layer.DOMAIN);
            }
        }
    }

    private void createRecurrentTask() {
        DestinationSpec destination = this.calendarService.getMessageService().getDestinationSpec(CalendarTimeSeriesExtenderHandlerFactory.TASK_DESTINATION).get();
        this.calendarService.getTaskService().newBuilder()
                //TODO: make this dynamic in 10.3
                .setApplication("Admin")
                .setName(CalendarTimeSeriesExtenderHandlerFactory.TASK_NAME)
                .setScheduleExpressionString("0 0 0 1 12 ? *")  // Every year, Dec 1st at midnight (no matter what day)
                .setDestination(destination)
                .setPayLoad(CalendarTimeSeriesExtenderHandler.GLOBAL_START_PAYLOAD)
                .scheduleImmediately(true)
                .build();
    }

    @Override
    public String getModuleName() {
        return CalendarService.COMPONENTNAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        List<ResourceDefinition> resources = new ArrayList<>();
        resources.add(this.calendarService.getUserService().createModuleResourceWithPrivileges(getModuleName(),
                Privileges.RESOURCE_TOU_CALENDARS.getKey(), Privileges.RESOURCE_TOU_CALENDARS_DESCRIPTION.getKey(),
                Collections.singletonList(Privileges.Constants.MANAGE_TOU_CALENDARS)));
        return resources;
    }

    private void createQueue() {
        QueueTableSpec queueTableSpec = this.calendarService.getMessageService().getQueueTableSpec("MSG_RAWQUEUETABLE").get();
        DestinationSpec destinationSpec = queueTableSpec.createDestinationSpec(CalendarImporterMessageHandler.DESTINATION_NAME, 60);
        destinationSpec.save();
        destinationSpec.activate();
        destinationSpec.subscribe(TranslationKeys.CALENDAR_IMPORTER_MESSAGE_HANDLER_DISPLAYNAME, CalendarService.COMPONENTNAME, Layer.DOMAIN);
    }

}