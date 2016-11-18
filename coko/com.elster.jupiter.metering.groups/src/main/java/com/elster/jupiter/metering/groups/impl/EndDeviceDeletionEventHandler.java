package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.List;

@Component(name = "com.elster.jupiter.metering.groups.endDeviceDeletionEventHandler", service = TopicHandler.class, immediate = true)
public class EndDeviceDeletionEventHandler implements TopicHandler {

    private volatile MeteringGroupsService meteringGroupsService;

    @SuppressWarnings("unused") // for osgi
    public EndDeviceDeletionEventHandler() {
    }

    @Inject
    public EndDeviceDeletionEventHandler(MeteringGroupsService meteringGroupsService) {
        this.meteringGroupsService = meteringGroupsService;
    }

    @Reference
    public void setMeteringGroupsService(MeteringGroupsService meteringGroupsService) {
        this.meteringGroupsService = meteringGroupsService;
    }

    @Override
    public void handle(LocalEvent localEvent) {
        EndDevice source = (EndDevice) localEvent.getSource();

        // end membership for each static group
        List<EnumeratedEndDeviceGroup> enumeratedEndDeviceGroups = meteringGroupsService.findEnumeratedEndDeviceGroupsContaining(source);
        for (EnumeratedEndDeviceGroup group : enumeratedEndDeviceGroups) {
            group.endMembership(source, source.getObsoleteTime().get());
            group.update();
        }
    }

    @Override
    public String getTopicMatcher() {
        return EventType.METER_DELETED.topic();
    }
}
