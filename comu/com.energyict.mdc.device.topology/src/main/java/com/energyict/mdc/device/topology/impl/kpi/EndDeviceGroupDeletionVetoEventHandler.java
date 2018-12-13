/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl.kpi;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EventType;
import com.elster.jupiter.metering.groups.GroupEventData;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.device.topology.kpi.RegisteredDevicesKpiService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

@Component(name = "com.elster.jupiter.validation.impl.delete.queryenddevicegroup.eventhandler", service = TopicHandler.class, immediate = true)
public class EndDeviceGroupDeletionVetoEventHandler implements TopicHandler {

    private volatile RegisteredDevicesKpiService rdKpiService;
    private volatile Thesaurus thesaurus;

    public EndDeviceGroupDeletionVetoEventHandler() {
    }

    @Inject
    public EndDeviceGroupDeletionVetoEventHandler(RegisteredDevicesKpiService validationService, Thesaurus thesaurus) {
        setValidationService(validationService);
        this.thesaurus = thesaurus;
    }

    @Reference
    public void setValidationService(RegisteredDevicesKpiService rdKpiService) {
        this.rdKpiService = rdKpiService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(TopologyService.COMPONENT_NAME, Layer.SERVICE);
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
        return !rdKpiService.findByDeviceGroup(endDeviceGroup, 0, 1).isEmpty();
    }

    @Override
    public String getTopicMatcher() {
        return EventType.ENDDEVICEGROUP_VALIDATE_DELETED.topic();
    }
}
