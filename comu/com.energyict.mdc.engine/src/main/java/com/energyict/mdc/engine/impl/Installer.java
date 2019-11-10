/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.impl.EngineConfigurationServiceImpl;
import com.energyict.mdc.engine.impl.status.ComServerAliveHandlerFactory;

import org.osgi.framework.BundleContext;

import javax.inject.Inject;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Installs the components of the mdc engine bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-05-14 (14:08)
 */
class Installer implements FullInstaller {

    private final DataModel dataModel;
    private final EventService eventService;
    private final UserService userService;
    private final TaskService taskService;
    private final MessageService messageService;
    private final BundleContext bundleContext;

    private static final String TASK_NAME = "ComServerAliveStatusTask";
    private static final String TASK_SCHEDULE = "0 0/1 * 1/1 * ? *";
    private static final int TASK_RETRY_DELAY = 60;

    private static final TranslationKey TASK_DEFAULT_NAME_TRANSLATION = new TranslationKey() {
        @Override
        public String getKey() {
            return ComServerAliveHandlerFactory.COM_SERVER_ALIVE_TIMEOUT_TASK_SUBSCRIBER;
        }

        @Override
        public String getDefaultFormat() {
            return "Com Server Alive";
        }
    };

    @Inject
    Installer(DataModel dataModel, EventService eventService, UserService userService,
              TaskService taskService, MessageService messageService, BundleContext bundleContext) {
        super();
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.userService = userService;
        this.taskService = taskService;
        this.messageService = messageService;
        this.bundleContext = bundleContext;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());
        doTry(
                "Create event types for CES",
                this::createEventTypesIfNotExist,
                logger
        );
        doTry(
                "Create ComServer user",
                this::createComServerUser,
                logger
        );
        doTry(
                "Publish events",
                this::publishEvents,
                logger
        );
        doTry(
                "ComServer alive task",
                this::createComServerAliveStatusTask,
                logger
        );
    }

    private void createComServerUser() {
        User comServerUser = userService.createUser(EngineServiceImpl.COMSERVER_USER, EngineServiceImpl.COMSERVER_USER);
        Optional<Group> batchExecutorRole = userService.findGroup(UserService.BATCH_EXECUTOR_ROLE);
        comServerUser.join(batchExecutorRole.orElseThrow(() -> new IllegalStateException("Could not find batch executor role.")));
    }

    private void createEventTypesIfNotExist() {
        for (EventType eventType : EventType.values()) {
            eventType.createIfNotExists(this.eventService);
        }
    }

    private void publishEvents() {
        Stream.of(EventType.DEVICE_CONNECTION_COMPLETION.topic(),
                EventType.DEVICE_CONNECTION_FAILURE.topic(),
                EventType.UNKNOWN_INBOUND_DEVICE.topic(),
                EventType.UNKNOWN_SLAVE_DEVICE.topic())
                .map(eventService::getEventType)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(eventType -> {
                    eventType.setPublish(true);
                    eventType.update();
                });
    }

    private void createComServerAliveStatusTask() {
        String property = bundleContext.getProperty(EngineConfigurationServiceImpl.COM_SERVER_STATUS_ALIVE_FREQ_PROP);
        DestinationSpec destination = messageService.getQueueTableSpec("MSG_RAWTOPICTABLE").get()
                .createDestinationSpec(ComServerAliveHandlerFactory.COM_SERVER_ALIVE_TASK_DESTINATION, TASK_RETRY_DELAY);
        destination.activate();
        destination.subscribe(TASK_DEFAULT_NAME_TRANSLATION, EngineConfigurationService.COMPONENT_NAME, Layer.DOMAIN);
        taskService.newBuilder()
                .setApplication("Admin")
                .setName(TASK_NAME)
                .setScheduleExpressionString(property == null ? TASK_SCHEDULE : "0 0/" + property + " * 1/1 * ? *")
                .setDestination(destination)
                .setPayLoad("payload")
                .scheduleImmediately(true)
                .build();
    }
}