/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.config.impl;

import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.mdm.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.validation.ValidationRuleSet;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

@Component(name = "com.elster.jupiter.mdm.usagepoint.config.impl.MetrologyContractDeletionEventHandler", service = TopicHandler.class, immediate = true)
public class MetrologyContractDeletionEventHandler implements TopicHandler {

    private volatile Thesaurus thesaurus;
    private volatile UsagePointConfigurationService usagePointConfigurationService;

    public MetrologyContractDeletionEventHandler() {
    }

    @Inject
    public MetrologyContractDeletionEventHandler(UsagePointConfigurationService usagePointConfigurationService) {
        setUsagePointConfigurationService(usagePointConfigurationService);
    }

    @Reference
    @SuppressWarnings("unused")
    public void setUsagePointConfigurationService(UsagePointConfigurationService usagePointConfigurationService) {
        this.usagePointConfigurationService = usagePointConfigurationService;
    }

    @Override
    public void handle(LocalEvent localEvent) {
        MetrologyContract metrologyContract = (MetrologyContract) localEvent.getSource();
        for (EstimationRuleSet estimationRuleSet : usagePointConfigurationService.getEstimationRuleSets(metrologyContract)) {
            usagePointConfigurationService.removeEstimationRuleSet(metrologyContract, estimationRuleSet);
        }
        for (ValidationRuleSet validationRuleSet : usagePointConfigurationService.getValidationRuleSets(metrologyContract)) {
            usagePointConfigurationService.removeValidationRuleSet(metrologyContract, validationRuleSet);
        }
    }

    @Override
    public String getTopicMatcher() {
        return EventType.METROLOGY_CONTRACT_DELETED.topic();
    }
}
