package com.energyict.mdc.device.data.rest;

import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;

public class DeviceStateAccessFeature implements DynamicFeature {
    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {
        if(resourceInfo.getResourceClass().isAnnotationPresent(DeviceStatesRestricted.class)
                || resourceInfo.getResourceMethod().isAnnotationPresent(DeviceStatesRestricted.class)){
            context.register(DeviceStateAccessFilter.class);
        }
    }
}
