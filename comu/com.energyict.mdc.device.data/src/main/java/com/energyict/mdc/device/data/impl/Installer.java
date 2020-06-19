/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.metering.zone.MeteringZoneService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.device.data.LoadProfileService;
import com.energyict.mdc.device.data.impl.configchange.ServerDeviceForConfigChange;
import com.energyict.mdc.device.data.impl.events.ComTaskEnablementChangeMessageHandler;
import com.energyict.mdc.device.data.impl.events.ConnectionTaskValidatorAfterConnectionFunctionModificationMessageHandlerFactory;
import com.energyict.mdc.device.data.impl.events.ConnectionTaskValidatorAfterPropertyRemovalMessageHandlerFactory;
import com.energyict.mdc.device.data.impl.kpi.DataCollectionKpiCalculatorHandlerFactory;
import com.energyict.mdc.device.data.impl.pki.tasks.certrenewal.CertificateRenewalHandlerFactory;
import com.energyict.mdc.device.data.impl.pki.tasks.crlrequest.CrlRequestHandlerFactory;
import com.energyict.mdc.device.data.impl.pki.tasks.keyrenewal.KeyRenewalHandlerFactory;
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
    public static final String COMSCHEDULE_BACKGROUND_OBSOLETION_MESSAGING_NAME = "COMSCHED_BATCH_OBSOLETE";
    static final String COMSCHEDULE_RECALCULATOR_MESSAGING_DISPLAYNAME = "Recalculate communication schedules";
    static final String COMSCHEDULE_BACKGROUND_OBSOLETION_MESSAGING_DISPLAYNAME = "Handle obsolete communication schedules";
    static final int DEFAULT_RETRY_DELAY_IN_SECONDS = 60;

    private final DataModel dataModel;
    private final UserService userService;
    private final EventService eventService;
    private final MessageService messageService;
    private final InstallerV10_2Impl installerV10_2;
    private final InstallerV10_7_1Impl installerV10_7_1;
    private final InstallerV10_7_2Impl installerV10_7_2;
    private final InstallerV10_8Impl installerV10_8;
    private final InstallerV10_8_1Impl installerV10_8_1;
    private final PrivilegesProviderV10_3 privilegesProviderV10_3;
    private final PrivilegesProviderV10_4_1 privilegesProviderV10_4_1;
    private final PrivilegesProviderV10_6 privilegesProviderV10_6;
    private final PrivilegesProviderV10_6_1 privilegesProviderV10_6_1;
    private final TaskService taskService;

    @Inject
    public Installer(DataModel dataModel, UserService userService, EventService eventService, MessageService messageService, TaskService taskService,
                     InstallerV10_2Impl installerV10_2, PrivilegesProviderV10_3 privilegesProviderV10_3, PrivilegesProviderV10_4_1 privilegesProviderV10_4_1,
                     PrivilegesProviderV10_6 privilegesProviderV10_6, PrivilegesProviderV10_6_1 privilegesProviderV10_6_1, InstallerV10_7_1Impl installerV10_7_1,
                     InstallerV10_7_2Impl installerV10_7_2, InstallerV10_8Impl installerV10_8, InstallerV10_8_1Impl installerV10_8_1) {
        super();
        this.dataModel = dataModel;
        this.userService = userService;
        this.eventService = eventService;
        this.messageService = messageService;
        this.taskService = taskService;
        this.installerV10_2 = installerV10_2;
        this.privilegesProviderV10_3 = privilegesProviderV10_3;
        this.privilegesProviderV10_4_1 = privilegesProviderV10_4_1;
        this.privilegesProviderV10_6 = privilegesProviderV10_6;
        this.privilegesProviderV10_6_1 = privilegesProviderV10_6_1;
        this.installerV10_7_1 = installerV10_7_1;
        this.installerV10_7_2 = installerV10_7_2;
        this.installerV10_8 = installerV10_8;
        this.installerV10_8_1 = installerV10_8_1;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());
        execute(dataModel,
                "alter table DDC_COMTASKEXECJOURNALENTRY add constraint PK_DDC_COMTASKJOURNALENTRY primary key (COMTASKEXECSESSION, POSITION)");
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
        doTry(
                "Create Connection task completed event subscriber",
                this::addCommunicationTestEventSubscriber,
                logger
        );
        doTry(
                "Create Certificate renewal task",
                this::createCertificateRenewalTask,
                logger
        );
        doTry(
                "Create Key renewal task",
                this::createKeyRenewalTask,
                logger
        );

        installerV10_2.install(dataModelUpgrader, logger);
        userService.addModulePrivileges(installerV10_2);
        userService.addModulePrivileges(privilegesProviderV10_3);
        userService.addModulePrivileges(privilegesProviderV10_4_1);
        userService.addModulePrivileges(privilegesProviderV10_6);
        userService.addModulePrivileges(privilegesProviderV10_6_1);
        installerV10_7_1.install(dataModelUpgrader, logger);
        installerV10_7_2.install(dataModelUpgrader, logger);
        installerV10_8.install(dataModelUpgrader, logger);
        installerV10_8_1.install(dataModelUpgrader, logger);
    }

    private void addJupiterEventSubscribers() {
        Optional<DestinationSpec> destinationSpec = this.messageService.getDestinationSpec(EventService.JUPITER_EVENTS);
        if (destinationSpec.isPresent()) {
            DestinationSpec jupiterEvents = destinationSpec.get();
            Stream.of(
                    Pair.of(SubscriberTranslationKeys.IPV6ADDRESS_SUBSCRIBER, whereCorrelationId().isEqualTo(EventType.DEVICE_UPDATED_IPADDRESSV6.topic())),
                    Pair.of(SubscriberTranslationKeys.COMTASK_ENABLEMENT_CONNECTION, whereCorrelationId().like("com/energyict/mdc/device/config/comtaskenablement/%")),
                    Pair.of(SubscriberTranslationKeys.COMTASK_ENABLEMENT_PRIORITY, whereCorrelationId().isEqualTo("com/energyict/mdc/device/config/comtaskenablement/PRIORITY_UPDATED")),
                    Pair.of(SubscriberTranslationKeys.COMTASK_ENABLEMENT_STATUS, whereCorrelationId().like("com/energyict/mdc/device/config/comtaskenablement/%")),
                    Pair.of(SubscriberTranslationKeys.METER_READING_EVENT, whereCorrelationId().isEqualTo("com/elster/jupiter/metering/meterreading/CREATED")
                            .or(whereCorrelationId().isEqualTo("com/energyict/mdc/connectiontask/COMPLETION"))))
                    .filter(subscriber -> !jupiterEvents.getSubscribers()
                            .stream()
                            .anyMatch(s -> s.getName().equals(subscriber.getFirst().getKey())))
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
        this.createMessageHandler(defaultQueueTableSpec, ConnectionTaskValidatorAfterPropertyRemovalMessageHandlerFactory.TASK_DESTINATION, SubscriberTranslationKeys.CONNECTION_TASK_VALIDATOR_AFTER_PROPERTY_REMOVAL);
        this.createMessageHandler(defaultQueueTableSpec, SchedulingService.FILTER_ITEMIZER_QUEUE_DESTINATION, SubscriberTranslationKeys.COMSCHEDULE_FILTER_ITEMIZER);
        this.createMessageHandler(defaultQueueTableSpec, SchedulingService.COM_SCHEDULER_QUEUE_DESTINATION, SubscriberTranslationKeys.COMSCHEDULE);
        this.createMessageHandler(defaultQueueTableSpec, ServerDeviceForConfigChange.CONFIG_CHANGE_BULK_QUEUE_DESTINATION, SubscriberTranslationKeys.CHANGE_DEVICE_CONFIGURATION);
        this.createMessageHandler(defaultQueueTableSpec, ComTaskEnablementChangeMessageHandler.COMTASK_ENABLEMENT_QUEUE_DESTINATION, SubscriberTranslationKeys.COMTASK_ENABLEMENT);
        this.createMessageHandler(defaultQueueTableSpec, DeviceMessageService.BULK_DEVICE_MESSAGE_QUEUE_DESTINATION, SubscriberTranslationKeys.BULK_DEVICE_MESSAGES);
        this.createMessageHandler(defaultQueueTableSpec, DeviceMessageService.DEVICE_MESSAGE_QUEUE_DESTINATION, SubscriberTranslationKeys.DEVICE_MESSAGES);
        this.createMessageHandler(defaultQueueTableSpec, ConnectionTaskValidatorAfterConnectionFunctionModificationMessageHandlerFactory.TASK_DESTINATION, SubscriberTranslationKeys.CONNECTION_TASK_VALIDATOR_AFTER_CONNECTION_FUNCTION_MODIFICATION);
        this.createMessageHandler(defaultQueueTableSpec, CertificateRenewalHandlerFactory.CERTIFICATE_RENEWAL_TASK_DESTINATION_NAME, SubscriberTranslationKeys.CERTIFICATE_RENEWAL_TASK_SUBSCRIBER);
        this.createMessageHandler(defaultQueueTableSpec, KeyRenewalHandlerFactory.KEY_RENEWAL_TASK_DESTINATION_NAME, SubscriberTranslationKeys.KEY_RENEWAL_TASK_SUBSCRIBER);
        this.createMessageHandler(defaultQueueTableSpec, MeteringZoneService.BULK_ZONE_QUEUE_DESTINATION, SubscriberTranslationKeys.ZONE_SUBSCRIBER);
        this.createMessageHandler(defaultQueueTableSpec, LoadProfileService.BULK_LOADPROFILE_QUEUE_DESTINATION, SubscriberTranslationKeys.LOADPROFILE_SUBSCRIBER);
        createPrioritizedMessageHandlers();
    }

    void createPrioritizedMessageHandlers() {
        QueueTableSpec prioritizedDefaultQueueTableSpec = messageService.getQueueTableSpec(MessageService.PRIORITIZED_RAW_QUEUE_TABLE).get();
        this.createMessageHandler(prioritizedDefaultQueueTableSpec, CrlRequestHandlerFactory.CRL_REQUEST_TASK_DESTINATION_NAME, SubscriberTranslationKeys.CRL_REQUEST_TASK_SUBSCRIBER, true);
        this.createMessageHandler(prioritizedDefaultQueueTableSpec, DataCollectionKpiCalculatorHandlerFactory.TASK_DESTINATION, SubscriberTranslationKeys.DATA_COLLECTION_KPI_CALCULATOR, true);
    }

    private void createMessageHandler(QueueTableSpec defaultQueueTableSpec, TranslationKey nameKey) {
        createMessageHandler(defaultQueueTableSpec, nameKey.getKey(), nameKey);
    }

    private void createMessageHandler(QueueTableSpec defaultQueueTableSpec, String destinationName, TranslationKey nameKey) {
        createMessageHandler(defaultQueueTableSpec, destinationName, nameKey, false);
    }

    private void createMessageHandler(QueueTableSpec defaultQueueTableSpec, String destinationName, TranslationKey subscriberKey, boolean isPrioritized) {
        Optional<DestinationSpec> destinationSpecOptional = messageService.getDestinationSpec(destinationName);
        if (!destinationSpecOptional.isPresent()) {
            DestinationSpec queue = defaultQueueTableSpec.createDestinationSpec(destinationName, DEFAULT_RETRY_DELAY_IN_SECONDS, false, isPrioritized);
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

    private void addCommunicationTestEventSubscriber() {
        Optional<DestinationSpec> destinationSpec = this.messageService.getDestinationSpec(EventService.JUPITER_EVENTS);
        if (destinationSpec.isPresent()) {
            DestinationSpec jupiterEvents = destinationSpec.get();
            Stream.of(
                    Pair.of(SubscriberTranslationKeys.TEST_COMMUNICATION_COMPLETED_EVENT, whereCorrelationId().isEqualTo("com/energyict/mdc/connectiontask/COMPLETION")))
                    .filter(subscriber -> !jupiterEvents.getSubscribers()
                            .stream()
                            .anyMatch(s -> s.getName().equals(subscriber.getFirst().getKey())))
                    .forEach(subscriber -> this.doSubscriber(jupiterEvents, subscriber));
        }
    }

    private DestinationSpec getCertRenewalDestination() {
        return messageService.getDestinationSpec(CertificateRenewalHandlerFactory.CERTIFICATE_RENEWAL_TASK_DESTINATION_NAME).orElseGet(() ->
                messageService.getQueueTableSpec("MSG_RAWQUEUETABLE")
                        .get()
                        .createDestinationSpec(CertificateRenewalHandlerFactory.CERTIFICATE_RENEWAL_TASK_DESTINATION_NAME, DEFAULT_RETRY_DELAY_IN_SECONDS));
    }

    private DestinationSpec getKeyRenewalDestination() {
        return messageService.getDestinationSpec(KeyRenewalHandlerFactory.KEY_RENEWAL_TASK_DESTINATION_NAME).orElseGet(() ->
                messageService.getQueueTableSpec("MSG_RAWQUEUETABLE")
                        .get()
                        .createDestinationSpec(KeyRenewalHandlerFactory.KEY_RENEWAL_TASK_DESTINATION_NAME, DEFAULT_RETRY_DELAY_IN_SECONDS));
    }

    private void createCertificateRenewalTask() {
        if (!taskService.getRecurrentTask(CertificateRenewalHandlerFactory.CERTIFICATE_RENEWAL_TASK_NAME).isPresent()) {
            taskService.newBuilder()
                    .setApplication("MultiSense")
                    .setName(CertificateRenewalHandlerFactory.CERTIFICATE_RENEWAL_TASK_NAME)
                    .setScheduleExpressionString(CertificateRenewalHandlerFactory.CERTIFICATE_RENEWAL_TASK_CRON_STRING)
                    .setDestination(getCertRenewalDestination())
                    .setPayLoad("Certificate Renewal")
                    .scheduleImmediately(true)
                    .build();
        }
    }

    private void createKeyRenewalTask() {
        if (!taskService.getRecurrentTask(KeyRenewalHandlerFactory.KEY_RENEWAL_TASK_NAME).isPresent()) {
            taskService.newBuilder()
                    .setApplication("MultiSense")
                    .setName(KeyRenewalHandlerFactory.KEY_RENEWAL_TASK_NAME)
                    .setScheduleExpressionString(KeyRenewalHandlerFactory.KEY_RENEWAL_TASK_CRON_STRING)
                    .setDestination(getKeyRenewalDestination())
                    .setPayLoad("Key Renewal")
                    .scheduleImmediately(true)
                    .build();
        }
    }

}
