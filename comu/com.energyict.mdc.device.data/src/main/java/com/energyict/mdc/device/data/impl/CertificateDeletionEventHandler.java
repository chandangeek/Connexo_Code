/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.VetoDeleteCertificateException;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.SecurityAccessor;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Listens for delete events of {@link com.elster.jupiter.pki.CertificateWrapper}s
 * and will veto the deletion if the {@link com.elster.jupiter.pki.CertificateWrapper} is still referenced by any {@link SecurityAccessor}
 */
@Component(name="com.energyict.mdc.device.config.delete.certificate.eventhandler", service = TopicHandler.class, immediate = true)
public class CertificateDeletionEventHandler implements TopicHandler {

    private volatile DeviceService deviceService;
    private volatile Thesaurus thesaurus;

    // OSGi
    public CertificateDeletionEventHandler() {
        super();
    }

    // For testing purposes only
    public CertificateDeletionEventHandler(DeviceService deviceService, NlsService nlsService) {
        this();
        this.setDeviceService(deviceService);
        this.setNlsService(nlsService);
    }

    @Override
    public void handle(LocalEvent localEvent) {
        CertificateWrapper source = (CertificateWrapper) localEvent.getSource();
        if (!deviceService.getAssociatedKeyAccessors(source).isEmpty()) {
            throw new VetoDeleteCertificateException(thesaurus, MessageSeeds.VETO_CERTIFICATE_REMOVAL_USED_ON_DEVICE);
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
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(DeviceDataServices.COMPONENT_NAME, Layer.DOMAIN);
    }

}
