/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.autoreschedule.impl;

import com.elster.jupiter.customtask.CustomTaskService;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EventType;
import com.elster.jupiter.metering.groups.GroupEventData;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

@Component(name = "com.elster.jupiter.autoreschedule.delete.enddevicegroup.eventhandler", service = TopicHandler.class, immediate = true)
public class EndDeviceGroupDeletionVetoEventHandler implements TopicHandler {

    private volatile CustomTaskService customTaskService;
    private volatile Thesaurus thesaurus;

    public EndDeviceGroupDeletionVetoEventHandler() {
    }

    @Inject
    public EndDeviceGroupDeletionVetoEventHandler(CustomTaskService customTaskService, Thesaurus thesaurus) {
        setCustomTaskService(customTaskService);
        this.thesaurus = thesaurus;
    }

    @Reference
    @SuppressWarnings("unused")
    public void setCustomTaskService(CustomTaskService customTaskService) {
        this.customTaskService = customTaskService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus("DES", Layer.SERVICE);
    }

    @Override
    public void handle(LocalEvent localEvent) {
        GroupEventData eventSource = (GroupEventData) localEvent.getSource();
        EndDeviceGroup endDeviceGroup = (EndDeviceGroup) eventSource.getGroup();

        if (isInUse(endDeviceGroup)) {
            throw new VetoDeleteDeviceGroupException(thesaurus, endDeviceGroup);
        }
    }

    private boolean isInUse(EndDeviceGroup endDeviceGroup) {
        return !customTaskService.findPropertyByDeviceGroupName(endDeviceGroup.getName(), 0, 1).isEmpty();
    }

    @Override
    public String getTopicMatcher() {
        return EventType.ENDDEVICEGROUP_VALIDATE_DELETED.topic();
    }
}

