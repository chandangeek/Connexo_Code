
package com.energyict.mdc.device.data.impl.messagehandlers;

import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.zone.MeteringZoneService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.DeviceService;


import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.energyict.mdc.zone.device.message.handler.factory",
        service = MessageHandlerFactory.class,
        property = {"subscriber="+ MeteringZoneService.BULK_ZONE_QUEUE_SUBSCRIBER,
                "destination="+ MeteringZoneService.BULK_ZONE_QUEUE_DESTINATION},
        immediate = true)
public class ZoneOnDeviceMessageHandlerFactory implements MessageHandlerFactory{
    private volatile JsonService jsonService;
    private volatile DataModel dataModel;
    private volatile TransactionService transactionService;
    private volatile DeviceService deviceService;
    private volatile MeteringZoneService meteringZoneService;
    private volatile MeteringService meteringService;
    private volatile Thesaurus thesaurus;

    @Override
    public MessageHandler newMessageHandler() {
        return dataModel.
                getInstance(ZoneOnDeviceMessageHandler.class).
                init(meteringService, deviceService, meteringZoneService, jsonService, transactionService, thesaurus);
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Reference
    private void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setMeteringZoneService(MeteringZoneService meteringZoneService) {
        this.meteringZoneService = meteringZoneService;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.dataModel = ormService.newDataModel("ZoneMessageHandlers", "Message handler for bulk action on zone");
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(DeviceDataServices.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }


    @Activate
    public void activate() {
        this.dataModel.register(this.getModule());
    }


    private Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(TransactionService.class).toInstance(transactionService);
                bind(JsonService.class).toInstance(jsonService);
                bind(MeteringZoneService.class).toInstance(meteringZoneService);
                bind(DeviceService.class).toInstance(deviceService);
                bind(MeteringService.class).toInstance(meteringService);
            }
        };
    }
}

