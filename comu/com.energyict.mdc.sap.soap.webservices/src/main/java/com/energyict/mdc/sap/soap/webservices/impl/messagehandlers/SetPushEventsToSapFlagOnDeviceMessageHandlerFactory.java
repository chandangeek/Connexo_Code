/*
 * Copyright (c) 2022 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.messagehandlers;

import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.custompropertyset.SAPCustomPropertySetsImpl;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.energyict.mdc.sap.soap.webservices.setpusheventstosapflag.device.message.handler.factory",
        service = MessageHandlerFactory.class,
        property = {"subscriber=" + SetPushEventsToSapFlagOnDeviceMessageHandlerFactory.BULK_SETPUSHEVENTSTOSAP_QUEUE_SUBSCRIBER,
                "destination=" + SetPushEventsToSapFlagOnDeviceMessageHandlerFactory.BULK_SETPUSHEVENTSTOSAP_QUEUE_DESTINATION},
        immediate = true)
public class SetPushEventsToSapFlagOnDeviceMessageHandlerFactory implements MessageHandlerFactory {
    public static final String BULK_SETPUSHEVENTSTOSAP_QUEUE_DESTINATION = "BulkSAPPushEventsQD";
    public static final String BULK_SETPUSHEVENTSTOSAP_QUEUE_SUBSCRIBER = "BulkSAPPushEventsQS";
    public static final String BULK_SETPUSHEVENTSTOSAP_QUEUE_DISPLAYNAME = "Handle setting flag to push events to SAP";

    private volatile JsonService jsonService;
    private volatile DeviceService deviceService;
    private volatile SAPCustomPropertySets sapCustomPropertySets;
    private volatile Thesaurus thesaurus;

    @Override
    public MessageHandler newMessageHandler() {
        return new SetPushEventsToSapFlagOnDeviceMessageHandler(deviceService, sapCustomPropertySets, jsonService, thesaurus);
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Reference
    public void setSAPCustomPropertySets(SAPCustomPropertySets sapCustomPropertySets) {
        this.sapCustomPropertySets = sapCustomPropertySets;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(WebServiceActivator.COMPONENT_NAME, Layer.SOAP)
                .join(nlsService.getThesaurus(SAPCustomPropertySetsImpl.COMPONENT_NAME, Layer.DOMAIN));
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }
}