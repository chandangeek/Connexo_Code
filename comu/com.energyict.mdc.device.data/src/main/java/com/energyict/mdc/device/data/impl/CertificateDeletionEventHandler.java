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
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.VetoDeleteCertificateException;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.SecurityAccessor;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;
import java.util.stream.Collectors;

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
        List<SecurityAccessor> accessors = deviceService.getAssociatedKeyAccessors(source);
        if (!accessors.isEmpty()) {
            List<String> deviceNames = accessors.stream()
                    .map(securityAccessor -> securityAccessor.getDevice().getName())
                    .collect(Collectors.toList());
            throw new VetoDeleteCertificateException(thesaurus, source, deviceNames);
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
        this.thesaurus = nlsService.getThesaurus(SecurityManagementService.COMPONENTNAME, Layer.DOMAIN);
    }

}
