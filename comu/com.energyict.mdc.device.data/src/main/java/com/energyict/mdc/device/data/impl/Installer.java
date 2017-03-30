/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.impl.configchange.ServerDeviceForConfigChange;
import com.energyict.mdc.device.data.impl.events.ComTaskEnablementChangeMessageHandler;
import com.energyict.mdc.device.data.impl.events.ConnectionTaskValidatorAfterPropertyRemovalMessageHandlerFactory;
import com.energyict.mdc.device.data.impl.kpi.DataCollectionKpiCalculatorHandlerFactory;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.scheduling.SchedulingService;

import javax.inject.Inject;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static com.elster.jupiter.messaging.DestinationSpec.whereCorrelationId;

/**
 * Represents the Installer for the Device data bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-30 (15:42)
 */
public class Installer implements FullInstaller {

    public static final String COMSCHEDULE_RECALCULATOR_MESSAGING_NAME = "COMSCHED_RECALCULATOR";
    static final String COMSCHEDULE_RECALCULATOR_MESSAGING_DISPLAYNAME = "Recalculate communication schedules";
    public static final String COMSCHEDULE_BACKGROUND_OBSOLETION_MESSAGING_NAME = "COMSCHED_BATCH_OBSOLETE";
    static final String COMSCHEDULE_BACKGROUND_OBSOLETION_MESSAGING_DISPLAYNAME = "Handle obsolete communication schedules";
    private static final int DEFAULT_RETRY_DELAY_IN_SECONDS = 60;

    private final DataModel dataModel;
    private final UserService userService;
    private final EventService eventService;
    private final MessageService messageService;
    private final InstallerV10_2Impl installerV10_2;

    @Inject
    public Installer(DataModel dataModel, UserService userService, EventService eventService, MessageService messageService, InstallerV10_2Impl installerV10_2) {
        super();
        this.dataModel = dataModel;
        this.userService = userService;
        this.eventService = eventService;
        this.messageService = messageService;
        this.installerV10_2 = installerV10_2;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());
        doTry(
                "Create event types for ",
                this::createEventTypes,
                logger
        );
        doTry(
                "Create message handlers",
                this::createMessageHandlers,
                logger
        );
        doTry(
                "Create event subscribers",
                this::addJupiterEventSubscribers,
                logger
        );
        doTry(
                "Create master data",
                this::createMasterData,
                logger
        );
        installerV10_2.install(dataModelUpgrader, logger);
        userService.addModulePrivileges(installerV10_2);
    }

    private void addJupiterEventSubscribers() {
        Optional<DestinationSpec> destinationSpec = this.messageService.getDestinationSpec(EventService.JUPITER_EVENTS);
        if (destinationSpec.isPresent()) {
            DestinationSpec jupiterEvents = destinationSpec.get();
            Stream.of(
                    Pair.of(SubscriberTranslationKeys.COMTASK_ENABLEMENT_CONNECTION, whereCorrelationId().like("com/energyict/mdc/device/config/comtaskenablement/%")),
                    Pair.of(SubscriberTranslationKeys.COMTASK_ENABLEMENT_PRIORITY, whereCorrelationId().isEqualTo("com/energyict/mdc/device/config/comtaskenablement/PRIORITY_UPDATED")),
                    Pair.of(SubscriberTranslationKeys.COMTASK_ENABLEMENT_STATUS, whereCorrelationId().like("com/energyict/mdc/device/config/comtaskenablement/%")),
                    Pair.of(SubscriberTranslationKeys.METER_READING_EVENT, whereCorrelationId().isEqualTo("com/elster/jupiter/metering/meterreading/CREATED")
                            .or(whereCorrelationId().isEqualTo("com/energyict/mdc/connectiontask/COMPLETION"))))
                    .filter(subscriber -> !jupiterEvents.getSubscribers()
                            .stream()
                            .anyMatch(s -> s.getName().equals(subscriber.getFirst())))
                    .forEach(subscriber -> this.doSubscriber(jupiterEvents, subscriber));
        }
    }

    private void doSubscriber(DestinationSpec jupiterEvents, Pair<SubscriberTranslationKeys, Condition> subscriber) {
        jupiterEvents.subscribe(subscriber.getFirst(), DeviceDataServices.COMPONENT_NAME, Layer.DOMAIN, subscriber.getLast());
    }

    private void createMessageHandlers() {
        QueueTableSpec defaultQueueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
        this.createMessageHandler(defaultQueueTableSpec, SubscriberTranslationKeys.COMSCHEDULE_RECALCULATOR);
        this.createMessageHandler(defaultQueueTableSpec, SubscriberTranslationKeys.COMSCHEDULE_BACKGROUND_OBSOLETION);
        this.createMessageHandler(defaultQueueTableSpec, ConnectionTaskService.FILTER_ITEMIZER_QUEUE_DESTINATION, SubscriberTranslationKeys.CONNECTION_FILTER_ITEMIZER);
        this.createMessageHandler(defaultQueueTableSpec, ConnectionTaskService.CONNECTION_RESCHEDULER_QUEUE_DESTINATION, SubscriberTranslationKeys.CONNECTION_RESCHEDULER);
        this.createMessageHandler(defaultQueueTableSpec, ConnectionTaskService.FILTER_ITEMIZER_PROPERTIES_QUEUE_DESTINATION, SubscriberTranslationKeys.CONNECTION_PROPERTY_FILTER_ITEMIZER);
        this.createMessageHandler(defaultQueueTableSpec, ConnectionTaskService.CONNECTION_PROP_UPDATER_QUEUE_DESTINATION, SubscriberTranslationKeys.CONNECTION_PROPERTY_UPDATER);
        this.createMessageHandler(defaultQueueTableSpec, CommunicationTaskService.FILTER_ITEMIZER_QUEUE_DESTINATION, SubscriberTranslationKeys.COMMUNICATION_FILTER_ITEMIZER);
        this.createMessageHandler(defaultQueueTableSpec, CommunicationTaskService.COMMUNICATION_RESCHEDULER_QUEUE_DESTINATION, SubscriberTranslationKeys.COMMUNICATION_RESCHEDULER);
        this.createMessageHandler(defaultQueueTableSpec, DataCollectionKpiCalculatorHandlerFactory.TASK_DESTINATION, SubscriberTranslationKeys.DATA_COLLECTION_KPI_CALCULATOR);
        this.createMessageHandler(defaultQueueTableSpec, ConnectionTaskValidatorAfterPropertyRemovalMessageHandlerFactory.TASK_DESTINATION, SubscriberTranslationKeys.CONNECTION_TASK_VALIDATOR_AFTER_PROPERTY_REMOVAL);
        this.createMessageHandler(defaultQueueTableSpec, SchedulingService.FILTER_ITEMIZER_QUEUE_DESTINATION, SubscriberTranslationKeys.COMSCHEDULE_FILTER_ITEMIZER);
        this.createMessageHandler(defaultQueueTableSpec, SchedulingService.COM_SCHEDULER_QUEUE_DESTINATION, SubscriberTranslationKeys.COMSCHEDULE);
        this.createMessageHandler(defaultQueueTableSpec, ServerDeviceForConfigChange.CONFIG_CHANGE_BULK_QUEUE_DESTINATION, SubscriberTranslationKeys.CHANGE_DEVICE_CONFIGURATION);
        this.createMessageHandler(defaultQueueTableSpec, ComTaskEnablementChangeMessageHandler.COMTASK_ENABLEMENT_QUEUE_DESTINATION, SubscriberTranslationKeys.COMTASK_ENABLEMENT);
    }

    private void createMessageHandler(QueueTableSpec defaultQueueTableSpec, TranslationKey nameKey) {
        createMessageHandler(defaultQueueTableSpec, nameKey.getKey(), nameKey);
    }

    private void createMessageHandler(QueueTableSpec defaultQueueTableSpec, String destinationName, TranslationKey subscriberKey) {
        Optional<DestinationSpec> destinationSpecOptional = messageService.getDestinationSpec(destinationName);
        if (!destinationSpecOptional.isPresent()) {
            DestinationSpec queue = defaultQueueTableSpec.createDestinationSpec(destinationName, DEFAULT_RETRY_DELAY_IN_SECONDS);
            queue.activate();
            queue.subscribe(subscriberKey, DeviceDataServices.COMPONENT_NAME, Layer.DOMAIN);
        } else {
            boolean notSubscribedYet = !destinationSpecOptional.get()
                    .getSubscribers()
                    .stream()
                    .anyMatch(spec -> spec.getName().equals(subscriberKey.getKey()));
            if (notSubscribedYet) {
                destinationSpecOptional.get().activate();
                destinationSpecOptional.get().subscribe(subscriberKey, DeviceDataServices.COMPONENT_NAME, Layer.DOMAIN);
            }
        }
    }

    private void createMasterData() {
        // No master data so far
    }

    private void createEventTypes() {
        for (EventType eventType : EventType.values()) {
            eventType.createIfNotExists(this.eventService);
        }
    }
}
