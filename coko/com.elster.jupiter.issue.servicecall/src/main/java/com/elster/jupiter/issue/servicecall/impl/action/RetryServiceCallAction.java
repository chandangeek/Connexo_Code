/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.servicecall.impl.action;

import com.elster.jupiter.issue.servicecall.ServiceCallIssue;
import com.elster.jupiter.issue.servicecall.impl.i18n.TranslationKeys;
import com.elster.jupiter.issue.share.AbstractIssueAction;
import com.elster.jupiter.issue.share.IssueActionResult;
import com.elster.jupiter.issue.share.IssueActionResult.DefaultActionResult;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.servicecall.ServiceCall;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

public class RetryServiceCallAction extends AbstractIssueAction {

    private final IssueService issueService;

    @Inject
    public RetryServiceCallAction(DataModel dataModel, Thesaurus thesaurus, PropertySpecService propertySpecService, IssueService issueService) {
        super(dataModel, thesaurus, propertySpecService);
        this.issueService = issueService;
    }

    @Override
    public IssueActionResult execute(Issue issue) {
        DefaultActionResult result = new DefaultActionResult();
        ServiceCallIssue scIssue = (ServiceCallIssue) issue;
        ServiceCall serviceCall = scIssue.getServiceCall();
        if (!serviceCall.getState().isOpen()) {
            try {
                serviceCall.requestTransition(serviceCall.getType().getRetryState().get());
                result.success(getThesaurus().getFormat(TranslationKeys.RETRY_NOW_ACTION_SUCCEED).format());
                issue.setStatus(issueService.findStatus(IssueStatus.IN_PROGRESS).get());
                issue.update();
                return result;
            } catch (LocalizedException e) {
                result.fail(e.getLocalizedMessage());
            }
        } else {
            result.fail(getThesaurus().getFormat(TranslationKeys.RETRY_NOW_ACTION_FAIL_NOT_FINAL_STATE).format());
        }

        return result;
    }

    @Override
    public boolean isApplicable(Issue issue) {
        if (super.isApplicable(issue) && issue instanceof ServiceCallIssue) {
            ServiceCallIssue scIssue = (ServiceCallIssue) issue;
            return !scIssue.getStatus().isHistorical() && scIssue.getServiceCall() != null
                    && scIssue.getServiceCall().getType().getRetryState().isPresent()
                    && !scIssue.getServiceCall().getState().isOpen();
        }
        return false;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Collections.emptyList();
    }

    @Override
    public String getDisplayName() {
        return getThesaurus().getFormat(TranslationKeys.RETRY_NOW_ACTION).format();
    }
}
