/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.protocol.api.DeviceMessageFile;
import com.energyict.mdc.protocol.api.DeviceMessageFileService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Optional;

/**
 * Provides an implementation for the {@link DeviceMessageFileService}
 * that delegates to the {@link ServerDeviceConfigurationService}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-05-13 (08:56)
 */
@Component(name = "com.energyict.mdc.device.message.file", service = {DeviceMessageFileService.class}, immediate = true)
public class DeviceMessageFileServiceImpl implements DeviceMessageFileService {
    private volatile ServerDeviceConfigurationService deviceConfigurationService;

    // For OSGi purposes
    public DeviceMessageFileServiceImpl() {
        super();
    }

    // For testing purposes
    @Inject
    public DeviceMessageFileServiceImpl(ServerDeviceConfigurationService deviceConfigurationService) {
        this();
        this.setDeviceConfigurationService(deviceConfigurationService);
    }

    @Reference
    public void setDeviceConfigurationService(ServerDeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Override
    public Optional<DeviceMessageFile> findDeviceMessageFile(long id) {
        return this.deviceConfigurationService.findDeviceMessageFile(id);
    }

}