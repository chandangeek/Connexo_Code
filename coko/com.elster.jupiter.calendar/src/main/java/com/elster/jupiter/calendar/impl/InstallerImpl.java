/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.EventType;
import com.elster.jupiter.calendar.OutOfTheBoxCategory;
import com.elster.jupiter.calendar.impl.importers.CalendarImporterMessageHandler;
import com.elster.jupiter.calendar.security.Privileges;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

class InstallerImpl implements FullInstaller, PrivilegesProvider {

    private final UserService userService;
    private final EventService eventService;
    private final DataModel dataModel;
    private final MessageService messageService;

    @Inject
    public InstallerImpl(UserService userService, EventService eventService, DataModel dataModel, MessageService messageService) {
        this.userService = userService;
        this.eventService = eventService;
        this.dataModel = dataModel;
        this.messageService = messageService;
    }

    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());
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
        userService.addModulePrivileges(this);
        if (!messageService.getDestinationSpec(CalendarImporterMessageHandler.DESTINATION_NAME).isPresent()) {
            doTry(
                    "Create TOU Calendar import queue",
                    this::createQueue,
                    logger
            );
        }
    }

    private void createCategories() {
        for (OutOfTheBoxCategory outOfTheBoxCategory : OutOfTheBoxCategory.values()) {
            CategoryImpl category = this.dataModel.getInstance(CategoryImpl.class);
            category.init(outOfTheBoxCategory.getDefaultDisplayName());
            category.save();
        }
    }

    private void createEventTypes() {
        for (EventType eventType : EventType.values()) {
            eventType.install(eventService);
        }
    }

    @Override
    public String getModuleName() {
        return CalendarService.COMPONENTNAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        List<ResourceDefinition> resources = new ArrayList<>();
        resources.add(userService.createModuleResourceWithPrivileges(getModuleName(),
                Privileges.RESOURCE_TOU_CALENDARS.getKey(), Privileges.RESOURCE_TOU_CALENDARS_DESCRIPTION.getKey(),
                Collections.singletonList(Privileges.Constants.MANAGE_TOU_CALENDARS)));
        return resources;
    }

    private void createQueue() {
        QueueTableSpec queueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
        DestinationSpec destinationSpec = queueTableSpec.createDestinationSpec(CalendarImporterMessageHandler.DESTINATION_NAME, 60);
        destinationSpec.save();
        destinationSpec.activate();
        destinationSpec.subscribe(TranslationKeys.CALENDAR_IMPORTER_MESSAGE_HANDLER_DISPLAYNAME, CalendarService.COMPONENTNAME, Layer.DOMAIN);
    }

}