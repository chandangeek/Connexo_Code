/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.system.impl;

import com.elster.jupiter.system.SystemModeService;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Component(name = "com.elster.jupiter.system.mode", service = {SystemModeService.class})
public class SystemModeServiceImpl implements SystemModeService {

    private String serverType;

    public SystemModeServiceImpl() {
        super();
    }

    @Activate
    public void activate(BundleContext bundleContext) {
        serverType = bundleContext.getProperty(SERVER_TYPE_PROPERTY_NAME);
    }

    @Override
    public boolean isOnlineMode() {
        return serverType == null || serverType.equalsIgnoreCase("online");
    }

}
