/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.SecurityAccessor;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.SecurityAccessorOnDeviceType;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;

/**
 * Listens to 'validate_delete' events of {@link com.elster.jupiter.pki.SecurityAccessor}
 * and will veto the deletion if it is still referenced by a {@link com.energyict.mdc.device.config.DeviceType}
 */
@Component(name = "com.energyict.mdc.firmware.impl.SecurityAccessorDeletionEventHandler", service = TopicHandler.class, immediate = true)
public class SecurityAccessorDeletionEventHandler implements TopicHandler {
    private volatile FirmwareService firmwareService;

    //OSGI
    public SecurityAccessorDeletionEventHandler() {
    }

    // For testing purposes only
    public SecurityAccessorDeletionEventHandler(FirmwareService firmwareService) {
        this();
        setFirmwareService(firmwareService);
    }

    @Override
    public void handle(LocalEvent localEvent) {
        SecurityAccessor securityAccessor = (SecurityAccessor) localEvent.getSource();
        List<SecurityAccessorOnDeviceType> securityAccessorOnDeviceTypeList = firmwareService.findSecurityAccessorForSignatureValidation(securityAccessor).find();
        if (!securityAccessorOnDeviceTypeList.isEmpty()) {
            throw new VetoDeleteSecurityAccessorException(getThesaurus(), securityAccessorOnDeviceTypeList);
        }
    }

    @Override
    public String getTopicMatcher() {
        return "com/elster/jupiter/pki/securityAccessor/VALIDATE_DELETE";
    }

    @Reference
    public void setFirmwareService(FirmwareService firmwareService) {
        this.firmwareService = firmwareService;
    }

    private Thesaurus getThesaurus() {
        return firmwareService.getThesaurus();
    }
}
