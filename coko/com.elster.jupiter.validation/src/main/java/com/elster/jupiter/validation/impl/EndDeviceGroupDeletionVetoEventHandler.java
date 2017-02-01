/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EventType;
import com.elster.jupiter.metering.groups.GroupEventData;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.validation.ValidationService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

@Component(name = "com.elster.jupiter.validation.impl.delete.queryenddevicegroup.eventhandler", service = TopicHandler.class, immediate = true)
public class EndDeviceGroupDeletionVetoEventHandler implements TopicHandler {

    private volatile ValidationService validationService;
    private volatile Thesaurus thesaurus;
    public final static String COMPONENT_NAME = "VAL";

    public EndDeviceGroupDeletionVetoEventHandler() {
    }

    @Inject
    public EndDeviceGroupDeletionVetoEventHandler(ValidationService validationService, Thesaurus thesaurus) {
        setValidationService(validationService);
        this.thesaurus = thesaurus;
    }

    @Reference
    public void setValidationService(ValidationService validationService) {
        this.validationService = validationService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.SERVICE);
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
        return !validationService.findByDeviceGroup(endDeviceGroup, 0, 1).isEmpty();
    }

    @Override
    public String getTopicMatcher() {
        return EventType.ENDDEVICEGROUP_VALIDATE_DELETED.topic();
    }
}
