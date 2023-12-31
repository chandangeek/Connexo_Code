/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.config.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.mdm.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

@Component(name = "UsagePointStateDeletionEventValidationHandler",
        service = {TopicHandler.class},
        immediate = true)
public class UsagePointStateDeletionEventValidationHandler implements TopicHandler {

    private ServerUsagePointConfigurationService usagePointConfigurationService;
    private Thesaurus thesaurus;

    @SuppressWarnings("unused") // OSGI
    public UsagePointStateDeletionEventValidationHandler() {
    }

    @Inject
    public UsagePointStateDeletionEventValidationHandler(ServerUsagePointConfigurationService usagePointConfigurationService, NlsService nlsService) {
        setUsagePointConfigurationService(usagePointConfigurationService);
        setNlsService(nlsService);
    }

    @Override
    public void handle(LocalEvent localEvent) {
        State source = (State) localEvent.getSource();
        if (!usagePointConfigurationService.getValidationRuleSets(source).isEmpty()) {
            throw DeleteUsagePointLifeCycleObjectException.canNotDeleteActiveState(this.thesaurus);
        }
    }

    @Override
    public String getTopicMatcher() {
        return "com/elster/jupiter/usagepoint/lifecycle/state/BEFORE_DELETE";
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