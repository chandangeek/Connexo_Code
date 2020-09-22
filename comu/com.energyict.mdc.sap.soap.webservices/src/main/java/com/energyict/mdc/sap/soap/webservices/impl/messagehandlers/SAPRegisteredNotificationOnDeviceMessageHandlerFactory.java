/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.messagehandlers;

import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.UtilitiesDeviceRegisteredNotification;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.custompropertyset.SAPCustomPropertySetsImpl;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.time.Clock;

@Component(name = "com.energyict.mdc.sap.soap.webservices.sapregisterednotification.device.message.handler.factory",
        service = MessageHandlerFactory.class,
        property = {"subscriber=" + SAPRegisteredNotificationOnDeviceMessageHandlerFactory.BULK_SAPREGISTEREDNOTIFICATION_QUEUE_SUBSCRIBER,
                "destination=" + SAPRegisteredNotificationOnDeviceMessageHandlerFactory.BULK_SAPREGISTEREDNOTIFICATION_QUEUE_DESTINATION},
        immediate = true)
public class SAPRegisteredNotificationOnDeviceMessageHandlerFactory implements MessageHandlerFactory {
    public static final String BULK_SAPREGISTEREDNOTIFICATION_QUEUE_DESTINATION = "BulkSAPRegNotificationQD";
    public static final String BULK_SAPREGISTEREDNOTIFICATION_QUEUE_SUBSCRIBER = "BulkSAPRegNotificationQS";
    public static final String BULK_SAPREGISTEREDNOTIFICATION_QUEUE_DISPLAYNAME = "Handle sending SAP registered notification";

    private volatile JsonService jsonService;
    private volatile DataModel dataModel;
    private volatile DeviceService deviceService;
    private volatile EndPointConfigurationService endPointConfigurationService;
    private volatile UtilitiesDeviceRegisteredNotification utilitiesDeviceRegisteredNotification;
    private volatile SAPCustomPropertySets sapCustomPropertySets;
    private volatile Clock clock;
    private volatile Thesaurus thesaurus;

    @Override
    public MessageHandler newMessageHandler() {
        return new SAPRegisteredNotificationOnDeviceMessageHandler(deviceService, endPointConfigurationService, utilitiesDeviceRegisteredNotification, sapCustomPropertySets, clock, jsonService, thesaurus);
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }


    @Reference
    public void setEndPointConfigurationService(EndPointConfigurationService endPointConfigurationService) {
        this.endPointConfigurationService = endPointConfigurationService;
    }

    @Reference
    public void setUtilitiesDeviceRegisteredNotification(UtilitiesDeviceRegisteredNotification utilitiesDeviceRegisteredNotification) {
        this.utilitiesDeviceRegisteredNotification = utilitiesDeviceRegisteredNotification;
    }

    @Reference
    public void setSAPCustomPropertySets(SAPCustomPropertySets sapCustomPropertySets) {
        this.sapCustomPropertySets = sapCustomPropertySets;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.dataModel = ormService.newDataModel("SAPRegisteredNotificationMessageHandlers", "Message handler for bulk action on send SAP registered notification");
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

    @Activate
    public void activate() {
        this.dataModel.register(this.getModule());
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(JsonService.class).toInstance(jsonService);
                bind(DeviceService.class).toInstance(deviceService);
                bind(EndPointConfigurationService.class).toInstance(endPointConfigurationService);
                bind(SAPCustomPropertySets.class).toInstance(sapCustomPropertySets);
                bind(UtilitiesDeviceRegisteredNotification.class).toInstance(utilitiesDeviceRegisteredNotification);
                bind(Clock.class).toInstance(clock);
            }
        };
    }
}
