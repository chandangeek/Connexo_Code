package com.energyict.mdc.engine.impl.events.filtering;

import com.energyict.mdc.engine.events.ComServerEvent;
import com.energyict.mdc.engine.events.DeviceRelatedEvent;
import com.energyict.mdc.protocol.api.device.BaseDevice;

import java.util.List;

/**
 * Provides an implementation for the {@link EventFilterCriterion} interface
 * that will filter {@link ComServerEvent}s when they do not relate
 * to a number of {@link com.energyict.mdc.protocol.api.device.BaseDevice devices}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-31 (12:18)
 */
public class DeviceFilter implements EventFilterCriterion {

    private List<BaseDevice> devices;

    public DeviceFilter (List<BaseDevice> devices) {
        super();
        this.devices = devices;
    }

    public List<BaseDevice> getDevices () {
        return devices;
    }

    public void setDevices (List<BaseDevice> devices) {
        this.devices = devices;
    }

    @Override
    public boolean matches (ComServerEvent event) {
        if (event.isDeviceRelated()) {
            DeviceRelatedEvent connectionEvent = (DeviceRelatedEvent) event;
            return !this.devices.contains(connectionEvent.getDevice());
        }
        else {
            return false;
        }
    }

}