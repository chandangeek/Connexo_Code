/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.SecurityAccessor;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.crlrequest.CrlRequestTaskPropertiesService;
import com.energyict.mdc.device.data.crlrequest.CrlRequestTaskProperty;
import com.elster.jupiter.tasks.RecurrentTask;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Optional;

/**
 * Listens for validate-delete events for {@link SecurityAccessor}
 * and will veto the deletion if the {@link SecurityAccessor} is still referenced by any task.
 */
@Component(name = "com.energyict.mdc.device.data.impl.SecurityAccessorRemovalEventHandler", service = TopicHandler.class, immediate = true)
public class SecurityAccessorRemovalEventHandler implements TopicHandler {
    private volatile CrlRequestTaskPropertiesService crlRequestTaskPropertiesService;
    private volatile Thesaurus thesaurus;

    public SecurityAccessorRemovalEventHandler() {
    }

    // For testing purposes only
    @Inject
    public SecurityAccessorRemovalEventHandler(CrlRequestTaskPropertiesService crlRequestTaskPropertiesService, NlsService nlsService) {
        this();
        setCrlRequestTaskPropertiesService(crlRequestTaskPropertiesService);
        setNlsService(nlsService);
    }

    @Reference
    public void setCrlRequestTaskPropertiesService(CrlRequestTaskPropertiesService crlRequestTaskPropertiesService) {
        this.crlRequestTaskPropertiesService = crlRequestTaskPropertiesService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(DeviceDataServices.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Override
    public void handle(LocalEvent localEvent) {
        SecurityAccessor source = (SecurityAccessor) localEvent.getSource();
        Optional<CrlRequestTaskProperty> crlRequestTaskProperty = crlRequestTaskPropertiesService.findCrlRequestTaskProperties()
                .stream()
                .filter(property -> property.getSecurityAccessor().getKeyAccessorType().getId() == source.getKeyAccessorType().getId())
                .findAny();
        if (crlRequestTaskProperty.isPresent()) {
            RecurrentTask recurrentTask = crlRequestTaskProperty.get().getRecurrentTask();
            throw new LocalizedException(thesaurus, MessageSeeds.VETO_SECURITY_ACCESSOR_REMOVAL_FROM_CRL_TASK, recurrentTask.getName()) {
            };
        }
    }

    @Override
    public String getTopicMatcher() {
        return "com/elster/jupiter/pki/securityAccessor/VALIDATE_DELETE";
    }
}
