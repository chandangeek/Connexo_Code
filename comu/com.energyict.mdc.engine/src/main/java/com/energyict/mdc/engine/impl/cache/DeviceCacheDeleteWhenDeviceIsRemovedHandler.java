package com.energyict.mdc.engine.impl.cache;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.engine.EngineService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Listens for delete events of {@link Device}s and will
 * delete the corresponding DeviceCache object first
 */
@Component(name="com.energyict.mdc.engine.impl.cache.DeviceCacheDeleteWhenDeviceIsRemovedHandler", service = TopicHandler.class, immediate = true)
public class DeviceCacheDeleteWhenDeviceIsRemovedHandler  implements TopicHandler {

    private volatile EngineService engineService;

    // For OSGI purposes
    public DeviceCacheDeleteWhenDeviceIsRemovedHandler() {
        super();
    }

    // For unit testing purposes
    public DeviceCacheDeleteWhenDeviceIsRemovedHandler(EngineService engineService) {
        this.engineService = engineService;
    }

    @Override
    public void handle(LocalEvent localEvent) {
        deleteCacheFor((Device) localEvent.getSource());
    }

    private void deleteCacheFor(Device device) {
        this.engineService.findDeviceCacheByDevice(device).ifPresent(DeviceCache::delete);
    }

    @Override
    public String getTopicMatcher() {
        return "com/energyict/mdc/device/data/device/BEFORE_DELETE";
    }

    @Reference
    public void setEngineService(EngineService engineService){
        this.engineService = engineService;
    }
}
