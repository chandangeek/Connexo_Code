/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.pki.CertificateWrapper;
import com.energyict.mdc.device.data.DeviceService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Listens for delete events of {@link com.elster.jupiter.pki.CertificateWrapper}s
 * and will veto the deletion if the {@link com.elster.jupiter.pki.CertificateWrapper} is still referenced by any {@link com.energyict.mdc.device.data.KeyAccessor}
 */
@Component(name="com.energyict.mdc.device.config.delete.certificate.eventhandler", service = TopicHandler.class, immediate = true)
public class CertificateDeletionEventHandler implements TopicHandler {

    private DeviceService deviceService;
    private DeviceDataModelService deviceDataModelService;

    // OSGi
    public CertificateDeletionEventHandler() {
        super();
    }

    // For testing purposes only
    public CertificateDeletionEventHandler(DeviceService deviceService, DeviceDataModelService deviceDataModelService) {
        this();
        this.setDeviceService(deviceService);
        this.setDeviceDataModelService(deviceDataModelService);
    }

    @Override
    public void handle(LocalEvent localEvent) {
        CertificateWrapper source = (CertificateWrapper) localEvent.getSource();
        if (this.deviceService.usedByKeyAccessor(source)) {
            throw new VetoDeleteCertificateException(deviceDataModelService.thesaurus(), source);
        }
    }

    @Override
    public String getTopicMatcher() {
        return "com/elster/jupiter/pki/certificate/VALIDATE_DELETE";
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setDeviceDataModelService(DeviceDataModelService deviceDataModelService) {
        this.deviceDataModelService = deviceDataModelService;
    }

}