/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.servicecall.impl.action;

import com.elster.jupiter.issue.servicecall.impl.i18n.TranslationKeys;
import com.elster.jupiter.issue.share.AbstractIssueAction;
import com.elster.jupiter.issue.share.IssueActionResult;
import com.elster.jupiter.issue.share.IssueActionResult.DefaultActionResult;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.issue.servicecall.ServiceCallIssue;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallFilter;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class RetryServiceCallAction extends AbstractIssueAction {

    @Inject
    public RetryServiceCallAction(DataModel dataModel, Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super(dataModel, thesaurus, propertySpecService);
    }

    @Override
    public IssueActionResult execute(Issue issue) {
        DefaultActionResult result = new DefaultActionResult();
        ServiceCallIssue scIssue = (ServiceCallIssue) issue;
        ServiceCall serviceCall = scIssue.getServiceCall();
        if (serviceCall.getState().isOpen()) {
            serviceCall.requestTransition(serviceCall.getType().getRetryState().get());
            result.success(getThesaurus().getFormat(TranslationKeys.RETRY_NOW_ACTION_SUCCEED).format());
            return result;
        } else {
//            result.nextAction(getThesaurus().getFormat(TranslationKeys.RETRY_NOW_ACTION_FAIL_TITLE).format());
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
