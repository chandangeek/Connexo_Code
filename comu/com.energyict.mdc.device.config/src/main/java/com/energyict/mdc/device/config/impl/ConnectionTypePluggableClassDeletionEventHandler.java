/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;

@Component(name="com.energyict.mdc.device.config.protocol.delete.connectiontypepluggableclass.eventhandler", service = TopicHandler.class, immediate = true)
public class ConnectionTypePluggableClassDeletionEventHandler implements TopicHandler {

    private volatile ServerDeviceConfigurationService deviceConfigurationService;

    @Override
    public void handle(LocalEvent localEvent) {
        ConnectionTypePluggableClass source = (ConnectionTypePluggableClass) localEvent.getSource();
        List<PartialConnectionTask> found = deviceConfigurationService.findByConnectionTypePluggableClass(source);
        if (!found.isEmpty()) {
            throw new VetoDeleteConnectionTypePluggableClassException(getThesaurus(), source, found);
        }
    }

    private Thesaurus getThesaurus() {
        return deviceConfigurationService.getThesaurus();
    }

    @Override
    public String getTopicMatcher() {
        return "com/energyict/mdc/protocol/pluggable/connectiontype/DELETED";
    }

    @Reference
    public void setDeviceConfigurationService(ServerDeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }
}
