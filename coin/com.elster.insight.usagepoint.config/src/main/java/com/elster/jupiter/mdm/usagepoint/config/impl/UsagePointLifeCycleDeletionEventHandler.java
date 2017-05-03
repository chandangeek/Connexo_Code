/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.config.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.mdm.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycle;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.time.Clock;


@Component(name = "UsagePointLifeCycleDeletionMDMVaidationEventHandler",
        service = {TopicHandler.class},
        immediate = true)
public class UsagePointLifeCycleDeletionEventHandler implements TopicHandler {

    private Clock clock;
    private ServerUsagePointConfigurationService usagePointConfigurationService;
    private Thesaurus thesaurus;

    @SuppressWarnings("unused") // OSGI
    public UsagePointLifeCycleDeletionEventHandler() {
    }

    @Inject
    public UsagePointLifeCycleDeletionEventHandler(Clock clock, ServerUsagePointConfigurationService usagePointConfigurationService, NlsService nlsService) {
        setClock(clock);
        setUsagePointConfigurationService(usagePointConfigurationService);
        setNlsService(nlsService);
    }

    @Override
    public void handle(LocalEvent localEvent) {
        UsagePointLifeCycle source = (UsagePointLifeCycle) localEvent.getSource();
        if (source.getStates().stream().anyMatch(state -> !usagePointConfigurationService.getValidationRuleSets(state).isEmpty())) {
            throw UsagePointLifeCycleDeleteObjectException.canNotDeleteActiveLifeCycle(this.thesaurus);
        }
    }

    @Override
    public String getTopicMatcher() {
        return "com/elster/jupiter/usagepoint/lifecycle/BEFORE_DELETE";
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setUsagePointConfigurationService(ServerUsagePointConfigurationService usagePointConfigurationService) {
        this.usagePointConfigurationService = usagePointConfigurationService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(UsagePointConfigurationService.COMPONENTNAME, Layer.DOMAIN);
    }
}