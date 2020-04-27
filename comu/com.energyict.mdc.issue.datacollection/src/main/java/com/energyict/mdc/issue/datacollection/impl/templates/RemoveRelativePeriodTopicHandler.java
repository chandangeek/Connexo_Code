/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.issue.datacollection.impl.templates;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TimeService;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.impl.IssueDataCollectionUtil;
import com.energyict.mdc.issue.datacollection.impl.event.VetoRelativePeriodDeleteException;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.List;

@Component(name = "com.energyict.mdc.issue.datacollection.RemoveRelativePeriodTopicHandler", service = TopicHandler.class, immediate = true)
public class RemoveRelativePeriodTopicHandler implements TopicHandler {

    private IssueService issueService;
    private Thesaurus thesaurus;

    public RemoveRelativePeriodTopicHandler() {
    }

    @Inject
    public RemoveRelativePeriodTopicHandler(IssueService issueService, NlsService nlsService) {
        setIssueService(issueService);
        setNlsService(nlsService);
    }
    @Override
    public void handle(LocalEvent localEvent) {
        RelativePeriod relativePeriod = (RelativePeriod) localEvent.getSource();
        List<CreationRule> issueCreationRules = IssueDataCollectionUtil.getIssueCreationRules(issueService);
        boolean deviceTypeInUse = issueCreationRules.stream()
                .map(rule -> (BasicDataCollectionRuleTemplate.RelativePeriodWithCountInfo)rule.getProperties().get(BasicDataCollectionRuleTemplate.THRESHOLD))
                .anyMatch(info -> info.getRelativePeriodId() == relativePeriod.getId());

        if(deviceTypeInUse) {
            throw new VetoRelativePeriodDeleteException(thesaurus, relativePeriod);
        }

    }

    @Override
    public String getTopicMatcher() {
        return "com/elster/jupiter/time/relativeperiod/DELETED";
    }


    @Reference
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(IssueDataCollectionService.COMPONENT_NAME, Layer.DOMAIN)
                .join(nlsService.getThesaurus(IssueDataCollectionService.COMPONENT_NAME, Layer.DOMAIN));;
    }
}
