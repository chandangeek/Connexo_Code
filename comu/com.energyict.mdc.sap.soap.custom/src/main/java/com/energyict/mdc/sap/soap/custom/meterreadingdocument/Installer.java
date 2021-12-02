/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.custom.meterreadingdocument;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.FullInstaller;

import com.energyict.mdc.sap.soap.custom.eventhandlers.CustomSAPDeviceEventHandler;

import javax.inject.Inject;
import java.util.logging.Logger;


public class Installer implements FullInstaller {

    private final MessageService messageService;

    @Inject
    public Installer(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
       addJupiterEventSubscribers();
    }

    private void addJupiterEventSubscribers() {
        this.messageService.getDestinationSpec(EventService.JUPITER_EVENTS)
                .ifPresent(jupiterEvents -> {
                    boolean subscriberExists = jupiterEvents.getSubscribers()
                            .stream()
                            .anyMatch(s -> s.getName().equals(TranslationKeys.SAP_METER_READING_DOCUMENT_EVENT_SUBSCRIBER.getKey()));

                    if (!subscriberExists) {
                        jupiterEvents.subscribe(TranslationKeys.SAP_METER_READING_DOCUMENT_EVENT_SUBSCRIBER,
                                CustomSAPDeviceEventHandler.COMPONENT_NAME,
                                Layer.DOMAIN,
                                DestinationSpec.whereCorrelationId().isEqualTo(SAPMeterReadingDocumentOnDemandHandler.SCHEDULED_COMTASKEXECUTION_COMPLETED)
                                        .or(DestinationSpec.whereCorrelationId().isEqualTo(SAPMeterReadingDocumentOnDemandHandler.SCHEDULED_COMTASKEXECUTION_FAILED))
                                        .or(DestinationSpec.whereCorrelationId().isEqualTo(SAPMeterReadingDocumentOnDemandHandler.MANUAL_COMTASKEXECUTION_COMPLETED))
                                        .or(DestinationSpec.whereCorrelationId().isEqualTo(SAPMeterReadingDocumentOnDemandHandler.MANUAL_COMTASKEXECUTION_FAILED))
                        );
                    }
                });
    }
}
