package com.energyict.mdc.engine.impl.web.events.commands;

import com.energyict.mdc.device.data.DeviceDataService;

import java.util.Set;

/**
 * Provides an implementation for the {@link RequestType} interface
 * for {@link com.energyict.mdc.device.data.Device}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-15 (17:00)
 */
public class DeviceRequestType extends IdBusinessObjectRequestType {

    private final DeviceDataService deviceDataService;

    public DeviceRequestType(DeviceDataService deviceDataService) {
        this.deviceDataService = deviceDataService;
    }

    @Override
    protected String getBusinessObjectTypeName () {
        return "device";
    }

    @Override
    protected Request newRequestForAll () {
        return new AllDevicesRequest();
    }

    @Override
    protected Request newRequestFor (Set<Long> ids) {
        return new DeviceRequest(deviceDataService, ids);
    }

}