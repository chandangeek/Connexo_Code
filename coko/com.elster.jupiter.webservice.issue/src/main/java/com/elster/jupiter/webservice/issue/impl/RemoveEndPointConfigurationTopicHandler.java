/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservice.issue.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.CreationRuleProperty;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.webservice.issue.WebServiceIssueService;
import com.elster.jupiter.webservice.issue.impl.template.AuthFailureIssueCreationRuleTemplate;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

@Component(name = "com.elster.jupiter.webservice.issue.impl.RemoveEndPointConfigurationTopicHandler", service = TopicHandler.class, immediate = true)
public class RemoveEndPointConfigurationTopicHandler implements TopicHandler {
    private WebServiceIssueServiceImpl webServiceIssueService;
    private OrmService ormService;

    public RemoveEndPointConfigurationTopicHandler() {
        // for OSGi
    }

    @Inject
    public RemoveEndPointConfigurationTopicHandler(WebServiceIssueServiceImpl webServiceIssueService, OrmService ormService) {
        this.webServiceIssueService = webServiceIssueService;
        this.ormService = ormService;
    }

    @Override
    public void handle(LocalEvent localEvent) {
        EndPointConfiguration endPointConfiguration = (EndPointConfiguration) localEvent.getSource();
        boolean isUsed = ormService.getDataModel(IssueService.COMPONENT_NAME)
                .orElseThrow(() -> new IllegalStateException(DataModel.class.getSimpleName() + " of " + IssueService.COMPONENT_NAME + " isn't found"))
                .stream(CreationRuleProperty.class)
                .join(CreationRule.class)
                .join(IssueReason.class)
                .join(IssueType.class)
                .filter(Where.where("rule.reason.issueType.key").isEqualTo(WebServiceIssueService.ISSUE_TYPE_NAME))
                .filter(Where.where("name").isEqualTo(AuthFailureIssueCreationRuleTemplate.END_POINT_CONFIGURATIONS))
                .anyMatch(Where.where("value").matches("^(?:.*,)?" + endPointConfiguration.getId() + "(?:,.*)?$", ""));
        // the checked id can be at the beginning, middle or end, but if present, it must be separated with comma.

        if (isUsed) {
            throw new VetoEndPointConfigurationDeleteException(webServiceIssueService.thesaurus(), endPointConfiguration);
        }
    }

    @Reference
    public void setWebServiceIssueService(WebServiceIssueServiceImpl webServiceIssueService) {
        this.webServiceIssueService = webServiceIssueService;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.ormService = ormService;
    }

    @Override
    public String getTopicMatcher() {
        return "com/elster/jupiter/webservices/endpoint/VALIDATE_DELETE";
    }
}
