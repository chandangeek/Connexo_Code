/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.tasks.ComTask;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Listens for delete events of {@link ComTask}s
 * and will veto the deletion if the ComTask
 * is still used by at least one {@link com.energyict.mdc.device.config.DeviceConfiguration}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-01-21 (16:26)
 */
@Component(name="com.energyict.mdc.device.config.delete.comtask.eventhandler", service = TopicHandler.class, immediate = true)
@SuppressWarnings("unused")
public class ComTaskDeletionEventHandler implements TopicHandler {

    private volatile ServerDeviceConfigurationService deviceConfigurationService;

    // For OSGi purposes
    public ComTaskDeletionEventHandler() {
        super();
    }

    // For testing purposes only
    public ComTaskDeletionEventHandler(ServerDeviceConfigurationService deviceConfigurationService) {
        this();
        this.setDeviceConfigurationService(deviceConfigurationService);
    }

    @Override
    public void handle(LocalEvent localEvent) {
        ComTask source = (ComTask) localEvent.getSource();
        if (this.deviceConfigurationService.usedByDeviceConfigurations(source)) {
            throw new VetoDeleteComTaskException(getThesaurus(), source);
        }
    }

    private Thesaurus getThesaurus() {
        return deviceConfigurationService.getThesaurus();
    }

    @Override
    public String getTopicMatcher() {
        return "com/energyict/mdc/tasks/comtask/VALIDATE_DELETE";
    }

    @Reference
    @SuppressWarnings("unused")
    public void setDeviceConfigurationService(ServerDeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

}