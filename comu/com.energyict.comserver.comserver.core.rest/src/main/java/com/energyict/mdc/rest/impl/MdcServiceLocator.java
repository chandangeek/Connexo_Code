package com.energyict.mdc.rest.impl;

import com.energyict.mdc.rest.DeviceProtocolFactoryService;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

@Provider
public class MdcServiceLocator implements ServiceLocator, ContextResolver<DeviceProtocolFactoryService> {

    private DeviceProtocolFactoryService deviceProtocolFactoryService;

    @Override
    public DeviceProtocolFactoryService getDeviceProtocolFactoryService() {
        return deviceProtocolFactoryService;
    }

    public void setDeviceProtocolFactoryService(DeviceProtocolFactoryService deviceProtocolFactoryService) {
        this.deviceProtocolFactoryService = deviceProtocolFactoryService;
    }

    @Override
    public DeviceProtocolFactoryService getContext(Class<?> type) {
        return deviceProtocolFactoryService;
    }
}

