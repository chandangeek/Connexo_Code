/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.configchange;

import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;
import com.energyict.mdc.device.data.impl.ServerDeviceService;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.energyict.mdc.device.data.impl.configchange.DeviceConfigChangeHandlerFactory",
        service = MessageHandlerFactory.class,
        property = {"subscriber=" + ServerDeviceForConfigChange.DEVICE_CONFIG_CHANGE_SUBSCRIBER, "destination=" + ServerDeviceForConfigChange.CONFIG_CHANGE_BULK_QUEUE_DESTINATION},
        immediate = true)
public class DeviceConfigChangeHandlerFactory implements MessageHandlerFactory {

    private volatile MessageService messageService;
    private volatile JsonService jsonService;
    private volatile SearchService searchService;
    private volatile Thesaurus thesaurus;
    private volatile DeviceService deviceService;
    private volatile DeviceDataModelService deviceDataModelService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;

    /*OSGI*/
    @SuppressWarnings("unused")
    public DeviceConfigChangeHandlerFactory() {
    }

    @Override
    public MessageHandler newMessageHandler() {
        DeviceConfigChangeHandler.ConfigChangeContext configChangeContext = new DeviceConfigChangeHandler.ConfigChangeContext(messageService, jsonService, searchService, thesaurus, ((ServerDeviceService) deviceService), deviceDataModelService, deviceConfigurationService, deviceLifeCycleConfigurationService);
        return new DeviceConfigChangeHandler(jsonService, configChangeContext);
    }

    @Reference
    public void setDeviceDataModelService(DeviceDataModelService deviceDataModelService){
        this.deviceDataModelService = deviceDataModelService;
    }

    @Reference
    public void setDeviceService(DeviceService deviceService){
        this.deviceService = deviceService;
    }
    @Reference
    public void setNlsService(NlsService nlsService){
        this.thesaurus = nlsService.getThesaurus(DeviceDataServices.COMPONENT_NAME, Layer.DOMAIN);
    }
    @Reference
    public void setSearchService(SearchService searchService){
        this.searchService = searchService;
    }
    @Reference
    public void setJsonService(JsonService jsonService){
        this.jsonService = jsonService;
    }
    @Reference
    public void setMessageService(MessageService messageService){
        this.messageService = messageService;
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService){
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Reference
    public void setDeviceLifeCycleConfigurationService(DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
    }

}
