/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.config.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.InboundComPortPool;
import com.energyict.mdc.pluggable.PluggableClass;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;

/**
 * Handles delete events that are being sent when a PluggableClass
 * is being deleted and will check if a ComPortPool is using it.
 * If that is the case, the delete will be vetoed by throwing an exception.
 */
@Component(name="com.energyict.mdc.device.config.protocol.delete.eventhandler", service = TopicHandler.class, immediate = true)
public class ComPortPoolPluggableClassDeletionEventHandler implements TopicHandler {

    private static final String TOPIC = "com/energyict/mdc/pluggable/pluggableclass/DELETED";

    private volatile Thesaurus thesaurus;
    private volatile EngineConfigurationService engineConfigurationService;

    @Override
    public String getTopicMatcher() {
        return TOPIC;
    }

    @Override
    public void handle(LocalEvent event) {
        this.handleDeleteProtocolPluggableClass((PluggableClass) event.getSource());
    }

    private void handleDeleteProtocolPluggableClass(PluggableClass pluggableClass) {
        List<InboundComPortPool> inboundComPortPools = engineConfigurationService.findComPortPoolByDiscoveryProtocol(pluggableClass);
        if (!inboundComPortPools.isEmpty()) {
            throw new VetoDiscoveryProtocolPluggableClassDeletionBecauseStillUsedByComPortPoolException(this.thesaurus, pluggableClass, inboundComPortPools);
        }
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.setThesaurus(nlsService.getThesaurus(EngineConfigurationService.COMPONENT_NAME, Layer.DOMAIN));
    }

    private void setThesaurus(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @Reference
    public void setEngineConfigurationService(EngineConfigurationService engineConfigurationService) {
        this.engineConfigurationService = engineConfigurationService;
    }

}