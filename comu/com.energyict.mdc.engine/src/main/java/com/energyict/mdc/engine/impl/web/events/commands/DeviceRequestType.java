package com.energyict.mdc.engine.impl.web.events.commands;

import java.util.Set;

/**
 * Provides an implementation for the {@link RequestType} interface
 * for {@link com.energyict.mdc.protocol.api.device.BaseDevice devices}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-15 (17:00)
 */
public class DeviceRequestType extends IdBusinessObjectRequestType {

    @Override
    protected String getBusinessObjectTypeName () {
        return "device";
    }

    @Override
    protected Request newRequestForAll () {
        return new AllDevicesRequest();
    }

    @Override
    protected Request newRequestFor (Set<Integer> ids) {
        return new DeviceRequest(ids);
    }

}