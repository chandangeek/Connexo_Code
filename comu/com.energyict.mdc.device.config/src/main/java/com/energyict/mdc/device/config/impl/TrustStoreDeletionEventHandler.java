/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.TrustStore;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Listens for delete events of {@link com.elster.jupiter.pki.TrustStore}s
 * and will veto the deletion if the TrustStore is still referenced by any KeyAccessorType
 */
@Component(name="com.energyict.mdc.device.config.delete.truststore.eventhandler", service = TopicHandler.class, immediate = true)
public class TrustStoreDeletionEventHandler implements TopicHandler {

    private volatile ServerDeviceConfigurationService deviceConfigurationService;

    // OSGi
    public TrustStoreDeletionEventHandler() {
        super();
    }

    // For testing purposes only
    public TrustStoreDeletionEventHandler(ServerDeviceConfigurationService deviceConfigurationService) {
        this();
        this.setDeviceConfigurationService(deviceConfigurationService);
    }

    @Override
    public void handle(LocalEvent localEvent) {
        TrustStore source = (TrustStore) localEvent.getSource();
        if (this.deviceConfigurationService.usedByKeyAccessorType(source)) {
            throw new VetoDeleteTrustStoreException(getThesaurus(), source);
        }
    }

    private Thesaurus getThesaurus() {
        return deviceConfigurationService.getThesaurus();
    }

    @Override
    public String getTopicMatcher() {
        return "com/elster/jupiter/pki/truststore/VALIDATE_DELETE";
    }

    @Reference
    public void setDeviceConfigurationService(ServerDeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

}