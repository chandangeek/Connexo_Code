/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservice.issue.impl.template;

import com.elster.jupiter.issue.share.CreationRuleTemplate;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.webservice.issue.impl.TranslationKeys;
import com.elster.jupiter.webservice.issue.impl.WebServiceIssueServiceImpl;
import com.elster.jupiter.webservice.issue.impl.event.WebServiceEventDescription;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

@Component(name = "com.elster.jupiter.webservice.issue.impl.template.NoAcknowledgementIssueCreationRuleTemplate",
        property = {"name=" + NoAcknowledgementIssueCreationRuleTemplate.NAME},
        service = CreationRuleTemplate.class, immediate = true)
public class NoAcknowledgementIssueCreationRuleTemplate extends AbstractWebServiceIssueCreationRuleTemplate {
    public static final String NAME = "NoAcknowledgementIssueCreationRuleTemplate";

    public NoAcknowledgementIssueCreationRuleTemplate() {
        // for OSGi
        super();
    }

    @Inject
    public NoAcknowledgementIssueCreationRuleTemplate(WebServiceIssueServiceImpl webServiceIssueService,
                                                      IssueService issueService,
                                                      PropertySpecService propertySpecService,
                                                      EndPointConfigurationService endPointConfigurationService) {
        super(webServiceIssueService, issueService, propertySpecService, endPointConfigurationService);
    }

    @Override
    TranslationKeys getNameTranslationKey() {
        return TranslationKeys.NO_ACKNOWLEDGEMENT_ISSUE_RULE_TEMPLATE_NAME;
    }

    @Override
    TranslationKeys getDescriptionTranslationKey() {
        return TranslationKeys.NO_ACKNOWLEDGEMENT_ISSUE_RULE_TEMPLATE_DESCRIPTION;
    }

    @Override
    WebServiceEventDescription getEventType() {
        return WebServiceEventDescription.NO_ACKNOWLEDGEMENT;
    }

    @Override
    @Reference
    public void setIssueService(IssueService issueService) {
        super.setIssueService(issueService);
    }

    @Override
    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        super.setPropertySpecService(propertySpecService);
    }

    @Override
    @Reference
    public void setWebServiceIssueService(WebServiceIssueServiceImpl webServiceIssueService) {
        super.setWebServiceIssueService(webServiceIssueService);
    }

    @Override
    @Reference
    public void setEndPointConfigurationService(EndPointConfigurationService endPointConfigurationService) {
        super.setEndPointConfigurationService(endPointConfigurationService);
    }
}
