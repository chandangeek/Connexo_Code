/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.impl.upgrade;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.Upgrader;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.InboundSoapEndpointsActivator;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.TranslationKeys;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.eventhandler.ComTaskExecutionEventHandler;

import javax.inject.Inject;

public class UpgraderV10_9_13 implements Upgrader {
    private final MessageService messageService;

    @Inject
    UpgraderV10_9_13(MessageService messageService) {
        this.messageService = messageService;

    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        addJupiterEventSubscribers();

    }

    private void addJupiterEventSubscribers() {
        this.messageService.getDestinationSpec(EventService.JUPITER_EVENTS)
                .ifPresent(jupiterEvents -> {
                    boolean subscriberExists = jupiterEvents.getSubscribers()
                            .stream()
                            .anyMatch(s -> s.getName().equals(TranslationKeys.COM_TASK_EXECUTION_EVENT_SUBSCRIBER.getKey()));

                    if (!subscriberExists) {
                        jupiterEvents.subscribe(TranslationKeys.COM_TASK_EXECUTION_EVENT_SUBSCRIBER,
                                InboundSoapEndpointsActivator.COMPONENT_NAME,
                                Layer.SOAP,
                                DestinationSpec.whereCorrelationId().isEqualTo(ComTaskExecutionEventHandler.EventType.SCHEDULED_COMTASKEXECUTION_COMPLETED.topic())
                                        .or(DestinationSpec.whereCorrelationId().isEqualTo(ComTaskExecutionEventHandler.EventType.SCHEDULED_COMTASKEXECUTION_FAILED.topic()))
                                        .or(DestinationSpec.whereCorrelationId().isEqualTo(ComTaskExecutionEventHandler.EventType.MANUAL_COMTASKEXECUTION_COMPLETED.topic()))
                                        .or(DestinationSpec.whereCorrelationId().isEqualTo(ComTaskExecutionEventHandler.EventType.MANUAL_COMTASKEXECUTION_FAILED.topic()))
                        );
                    }
                });
    }
}
