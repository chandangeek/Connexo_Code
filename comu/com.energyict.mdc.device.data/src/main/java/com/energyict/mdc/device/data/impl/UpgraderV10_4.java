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
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.upgrade.Upgrader;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.device.data.impl.events.ConnectionTaskValidatorAfterConnectionFunctionModificationMessageHandlerFactory;
import com.energyict.mdc.device.data.impl.pki.tasks.certrenewal.CertificateRenewalHandlerFactory;
import com.energyict.mdc.device.data.impl.pki.tasks.keyrenewal.KeyRenewalHandlerFactory;

import javax.inject.Inject;
import java.util.EnumSet;
import java.util.Optional;

class UpgraderV10_4 implements Upgrader {

    private static final int DEFAULT_RETRY_DELAY_IN_SECONDS = 60;

    private final DataModel dataModel;
    private final EventService eventService;
    private final MessageService messageService;
    private final TaskService taskService;

    @Inject
    UpgraderV10_4(DataModel dataModel, EventService eventService, MessageService messageService, TaskService taskService) {
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.messageService = messageService;
        this.taskService = taskService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 4));
        installNewEventTypes();
        installNewMessageHandlers();
        createCertificateRenewalTask();
        createKeyRenewalTask();
    }

    private void installNewEventTypes() {
        EnumSet.of(EventType.CONNECTIONTASK_SETASCONNECTIONFUNCTION, EventType.CONNECTIONTASK_CLEARCONNECTIONFUNCTION).forEach(eventType -> eventType.install(eventService));
    }

    private void installNewMessageHandlers() {
        QueueTableSpec defaultQueueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
        this.createMessageHandlerIfNotYetPresent(defaultQueueTableSpec, DeviceMessageService.BULK_DEVICE_MESSAGE_QUEUE_DESTINATION, SubscriberTranslationKeys.BULK_DEVICE_MESSAGES);
        this.createMessageHandlerIfNotYetPresent(defaultQueueTableSpec, DeviceMessageService.DEVICE_MESSAGE_QUEUE_DESTINATION, SubscriberTranslationKeys.DEVICE_MESSAGES);
        this.createMessageHandlerIfNotYetPresent(defaultQueueTableSpec, ConnectionTaskValidatorAfterConnectionFunctionModificationMessageHandlerFactory.TASK_DESTINATION, SubscriberTranslationKeys.CONNECTION_TASK_VALIDATOR_AFTER_CONNECTION_FUNCTION_MODIFICATION);
        this.createMessageHandlerIfNotYetPresent(defaultQueueTableSpec, CertificateRenewalHandlerFactory.CERTIFICATE_RENEWAL_TASK_DESTINATION_NAME, SubscriberTranslationKeys.CERTIFICATE_RENEWAL_TASK_SUBSCRIBER);
        this.createMessageHandlerIfNotYetPresent(defaultQueueTableSpec, KeyRenewalHandlerFactory.KEY_RENEWAL_TASK_DESTINATION_NAME, SubscriberTranslationKeys.KEY_RENEWAL_TASK_SUBSCRIBER);
    }

    private void createMessageHandlerIfNotYetPresent(QueueTableSpec defaultQueueTableSpec, String destinationName, TranslationKey subscriberKey) {
        Optional<DestinationSpec> destinationSpecOptional = messageService.getDestinationSpec(destinationName);
        if (!destinationSpecOptional.isPresent()) {
            DestinationSpec queue = defaultQueueTableSpec.createDestinationSpec(destinationName, DEFAULT_RETRY_DELAY_IN_SECONDS);
            queue.activate();
            queue.subscribe(subscriberKey, DeviceDataServices.COMPONENT_NAME, Layer.DOMAIN);
        } else {
            boolean notSubscribedYet = destinationSpecOptional.get()
                    .getSubscribers()
                    .stream()
                    .noneMatch(spec -> spec.getName().equals(subscriberKey.getKey()));
            if (notSubscribedYet) {
                destinationSpecOptional.get().activate();
                destinationSpecOptional.get().subscribe(subscriberKey, DeviceDataServices.COMPONENT_NAME, Layer.DOMAIN);
            }
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