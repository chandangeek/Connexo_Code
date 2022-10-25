/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.SecurityAccessorOnDeviceType;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Listens to 'pre_delete' event of {@link DeviceType}
 * in order to remove mapping with firmware signature check security accessor {@link SecurityAccessorOnDeviceType}.
 */
@Component(name = "com.energyict.mdc.firmware.impl.DeviceTypePreDeleteEventHandler", service = TopicHandler.class, immediate = true)
public class DeviceTypePreDeleteEventHandler implements TopicHandler {
    private volatile FirmwareService firmwareService;

    public DeviceTypePreDeleteEventHandler() {
        // for OSGi
    }

    public DeviceTypePreDeleteEventHandler(FirmwareService firmwareService) {
        // for testing purposes
        this();
        setFirmwareService(firmwareService);
    }

    @Override
    public void handle(LocalEvent localEvent) {
        DeviceType source = (DeviceType) localEvent.getSource();
        firmwareService.findSecurityAccessorForSignatureValidation(source).stream()
                .forEach(SecurityAccessorOnDeviceType::delete);
    }

    @Override
    public String getTopicMatcher() {
        return "com/energyict/mdc/device/config/devicetype/PRE_DELETE";
    }

    @Reference
    public void setFirmwareService(FirmwareService firmwareService) {
        this.firmwareService = firmwareService;
    }
}
