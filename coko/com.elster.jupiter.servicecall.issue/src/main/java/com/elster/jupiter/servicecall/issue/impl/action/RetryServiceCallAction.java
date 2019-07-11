/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.issue.impl.action;

import com.elster.jupiter.issue.share.AbstractIssueAction;
import com.elster.jupiter.issue.share.IssueActionResult;
import com.elster.jupiter.issue.share.IssueActionResult.DefaultActionResult;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.issue.ServiceCallIssue;
import com.elster.jupiter.servicecall.issue.impl.i18n.TranslationKeys;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

public class RetryServiceCallAction extends AbstractIssueAction {

    private IssueService issueService;

    @Inject
    public RetryServiceCallAction(DataModel dataModel, Thesaurus thesaurus, IssueService issueService, PropertySpecService propertySpecService) {
        super(dataModel, thesaurus, propertySpecService);
        this.issueService = issueService;
    }

    @Override
    public IssueActionResult execute(Issue issue) {
        DefaultActionResult result = new DefaultActionResult();
        if (isApplicable(issue)){
//            issue.setStatus(issueService.findStatus(IssueStatus.IN_PROGRESS).get());
//            issue.update();
//            ScheduledConnectionTask task = (ScheduledConnectionTask)((IssueDataCollection) issue).getConnectionTask().get();
//            task.scheduleNow();
//            result.success(getThesaurus().getFormat(TranslationKeys.ACTION_RETRY_CONNECTION_SUCCESS).format());
        }
        return result;
    }

    @Override
    public boolean isApplicable(Issue issue) {
        if (issue != null && !issue.getStatus().isHistorical() && issue instanceof ServiceCallIssue){
            ServiceCallIssue scIssue = (ServiceCallIssue) issue;
            return !scIssue.getStatus().isHistorical() && scIssue.getServiceCall() != null && !scIssue.getServiceCall().getState().isOpen();
        }
        return false;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Collections.emptyList();
    }

    @Override
    public String getDisplayName() {
        return getThesaurus().getFormat(TranslationKeys.ACTION_RETRY_NOW).format();
    }
}
