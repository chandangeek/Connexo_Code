/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.engine.config.ComPortPool;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;

@Component(name="com.energyict.mdc.device.config.delete.comportpool.eventhandler", service = TopicHandler.class, immediate = true)
@SuppressWarnings("unused")
public class ComPortPoolDeletionEventHandler implements TopicHandler {

    private volatile ServerDeviceConfigurationService deviceConfigurationService;

    @Override
    public void handle(LocalEvent localEvent) {
        ComPortPool source = (ComPortPool) localEvent.getSource();
        List<PartialConnectionTask> found = this.deviceConfigurationService.findByComPortPool(source);
        if (!found.isEmpty()) {
            throw new VetoDeleteComPortPoolException(getThesaurus(), source, found);
        }
    }

    private Thesaurus getThesaurus() {
        return deviceConfigurationService.getThesaurus();
    }

    @Override
    public String getTopicMatcher() {
        return "com/energyict/mdc/engine/config/comportpool/VALIDATE_DELETE";
    }

    @Reference
    @SuppressWarnings("unused")
    public void setDeviceConfigurationService(ServerDeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

}