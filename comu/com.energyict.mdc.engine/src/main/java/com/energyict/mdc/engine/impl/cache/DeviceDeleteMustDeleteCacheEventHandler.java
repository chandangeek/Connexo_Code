/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.cache;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.engine.EngineService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Optional;

@Component(name = "com.energyict.mdc.engine.impl.cache.DeviceDeleteMustDeleteCacheEventHandler", service = TopicHandler.class, immediate = true)
public class DeviceDeleteMustDeleteCacheEventHandler implements TopicHandler {

    @Override
    public void handle(LocalEvent localEvent) {
        Device source = (Device) localEvent.getSource();
        Optional<DeviceCache> deviceCacheByDevice = engineService.findDeviceCacheByDevice(source);
        if(deviceCacheByDevice.isPresent()){
            deviceCacheByDevice.get().delete();
        }
    }

    @Override
    public String getTopicMatcher() {
        return "com/energyict/mdc/device/data/device/DELETED";
    }

    private volatile EngineService engineService;

    @Reference
    public void setEngineService(EngineService engineService) {
        this.engineService = engineService;
    }
}
