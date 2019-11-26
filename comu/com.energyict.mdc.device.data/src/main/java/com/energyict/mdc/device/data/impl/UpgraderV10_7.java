/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.upgrade.Upgrader;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.LoadProfileService;
import com.energyict.mdc.device.data.impl.ami.servicecall.ServiceCallCommands;
import com.energyict.mdc.device.data.impl.ami.servicecall.handlers.CommunicationTestServiceCallHandler;
import com.energyict.mdc.device.data.impl.ami.servicecall.handlers.OnDemandReadServiceCallHandler;
import com.energyict.mdc.device.data.impl.kpi.DataCollectionKpiCalculatorHandlerFactory;
import com.energyict.mdc.device.data.impl.pki.tasks.crlrequest.CrlRequestHandlerFactory;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Logger;

public class UpgraderV10_7 implements Upgrader {

    private final DataModel dataModel;
    private final EventService eventService;
    private final MessageService messageService;
    private final ServiceCallService serviceCallService;
    private final Installer installer;

    @Inject
    public UpgraderV10_7(DataModel dataModel, MessageService messageService, ServiceCallService serviceCallService,
                         EventService eventService, Installer installer) {
        this.dataModel = dataModel;
        this.messageService = messageService;
        this.serviceCallService = serviceCallService;
        this.eventService = eventService;
        this.installer = installer;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 7));
        EventType.COMTASKEXECUTION_COMPLETION.createIfNotExists(eventService);
        deleteOldDestinations();
        installer.createPrioritizedMessageHandlers();
        createMessageHandlerLP();
        updateServiceCallTypes();
        updateConnectionTaskJournalTable();
    }

    private void deleteOldDestinations() {
        Optional<DestinationSpec> destinationSpec = messageService.getDestinationSpec(CrlRequestHandlerFactory.CRL_REQUEST_TASK_DESTINATION_NAME);
        destinationSpec.ifPresent(destination -> {
            destination.unSubscribe(CrlRequestHandlerFactory.CRL_REQUEST_TASK_DESTINATION_NAME);
            destination.delete();
        });
        destinationSpec = messageService.getDestinationSpec(DataCollectionKpiCalculatorHandlerFactory.TASK_DESTINATION);
        destinationSpec.ifPresent(destination -> {
            destination.unSubscribe(DataCollectionKpiCalculatorHandlerFactory.TASK_DESTINATION);
            destination.delete();
        });
    }

    private void createMessageHandlerLP() {
        QueueTableSpec defaultQueueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
        Optional<DestinationSpec> destinationSpecOptional = messageService.getDestinationSpec(LoadProfileService.BULK_LOADPROFILE_QUEUE_DESTINATION);
        if (!destinationSpecOptional.isPresent()) {
            DestinationSpec queue = defaultQueueTableSpec.createDestinationSpec(LoadProfileService.BULK_LOADPROFILE_QUEUE_DESTINATION, Installer.DEFAULT_RETRY_DELAY_IN_SECONDS);
            subscribeLP(queue);
        } else {
            boolean notSubscribedYet = destinationSpecOptional.get()
                    .getSubscribers()
                    .stream()
                    .noneMatch(spec -> spec.getName().equals(SubscriberTranslationKeys.LOADPROFILE_SUBSCRIBER.getKey()));
            if (notSubscribedYet) {
                subscribeLP(destinationSpecOptional.get());
            }
        }
    }

    private void updateServiceCallTypes() {
        for (ServiceCallCommands.ServiceCallTypeMapping type : ServiceCallCommands.ServiceCallTypeMapping.values()) {
            type.getApplication().ifPresent(
                    application ->
                            serviceCallService
                                    .findServiceCallType(type.getTypeName(), type.getTypeVersion()).ifPresent(
                                    serviceCallType -> {
                                        serviceCallType.setApplication(application);
                                        serviceCallType.save();
                                    }
                            ));
        }

        serviceCallService.findServiceCallType(OnDemandReadServiceCallHandler.SERVICE_CALL_HANDLER_NAME, OnDemandReadServiceCallHandler.VERSION).ifPresent(
                serviceCallType -> {
                    serviceCallType.setApplication(OnDemandReadServiceCallHandler.APPLICATION);
                    serviceCallType.save();
                }
        );

        serviceCallService.findServiceCallType(CommunicationTestServiceCallHandler.SERVICE_CALL_HANDLER_NAME, CommunicationTestServiceCallHandler.VERSION).ifPresent(
                serviceCallType -> {
                    serviceCallType.setApplication(CommunicationTestServiceCallHandler.APPLICATION);
                    serviceCallType.save();
                }
        );
    }

    private void subscribeLP(DestinationSpec queue) {
        queue.activate();
        queue.subscribe(SubscriberTranslationKeys.LOADPROFILE_SUBSCRIBER, DeviceDataServices.COMPONENT_NAME, Layer.DOMAIN);
    }

    private void updateConnectionTaskJournalTable() {
        execute(dataModel, "ALTER TABLE DDC_CONNECTIONTASKJRNL RENAME COLUMN COMSERVER TO COMPORT");
    }

}
