package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.groups.EnumeratedUsagePointGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.List;

@Component(name = "com.elster.jupiter.metering.groups.usagePointDeletionEventHandler", service = TopicHandler.class, immediate = true)
public class UsagePointDeletionEventHandler implements TopicHandler {

    private volatile MeteringGroupsService meteringGroupsService;

    @SuppressWarnings("unused") // for osgi
    public UsagePointDeletionEventHandler() {
    }

    @Inject
    public UsagePointDeletionEventHandler(MeteringGroupsService meteringGroupsService) {
        this.meteringGroupsService = meteringGroupsService;
    }

    @Reference
    public void setMeteringGroupsService(MeteringGroupsService meteringGroupsService) {
        this.meteringGroupsService = meteringGroupsService;
    }

    @Override
    public void handle(LocalEvent localEvent) {
        UsagePoint source = (UsagePoint) localEvent.getSource();

        // end membership for each static group
        List<EnumeratedUsagePointGroup> enumeratedUsagePointGroups = meteringGroupsService.findEnumeratedUsagePointGroupsContaining(source);
        for (EnumeratedUsagePointGroup group : enumeratedUsagePointGroups) {
            group.endMembership(source, source.getObsoleteTime().get());
            group.update();
        }
    }

    @Override
    public String getTopicMatcher() {
        return EventType.USAGEPOINT_DELETED.topic();
    }
}
