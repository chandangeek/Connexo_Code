package com.elster.jupiter.usagepoint.lifecycle.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.metering.UsagePoint;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

@Component(name = "UsagePointInitialStateChangeRequestHandler", service = TopicHandler.class, immediate = true)
public class UsagePointInitialStateChangeRequestHandler implements TopicHandler {

    private volatile ServerUsagePointLifeCycleService usagePointLifeCycleService;

    public UsagePointInitialStateChangeRequestHandler() {
    }

    @Inject
    public UsagePointInitialStateChangeRequestHandler(ServerUsagePointLifeCycleService usagePointLifeCycleService) {
        setUsagePointLifeCycleService(usagePointLifeCycleService);
    }

    @Reference
    public void setUsagePointLifeCycleService(ServerUsagePointLifeCycleService usagePointLifeCycleService) {
        this.usagePointLifeCycleService = usagePointLifeCycleService;
    }

    @Override
    public void handle(LocalEvent localEvent) {
        this.usagePointLifeCycleService.createUsagePointInitialStateChangeRequest((UsagePoint) localEvent.getSource());
    }

    @Override
    public String getTopicMatcher() {
        return "com/elster/jupiter/metering/usagepoint/CREATED";
    }
}
