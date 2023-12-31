/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest;

import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;

public class DeviceStateAccessFeature implements DynamicFeature {
    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {
        if(resourceInfo.getResourceClass().isAnnotationPresent(DeviceStagesRestricted.class)
                || resourceInfo.getResourceMethod().isAnnotationPresent(DeviceStagesRestricted.class)){
            context.register(DeviceStageAccessFilter.class);
        }
    }
}
