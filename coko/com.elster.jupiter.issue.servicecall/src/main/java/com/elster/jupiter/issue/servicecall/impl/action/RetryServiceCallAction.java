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

    public static final String FIRMWARE_CAMPAIGN_NAME = "FirmwareCampaignServiceCallHandler";
    public static final String FIRMWARE_ITEM_NAME = "FirmwareCampaignItemServiceCallHandler";

    @Inject
    public RetryServiceCallAction(DataModel dataModel, Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super(dataModel, thesaurus, propertySpecService);
    }

    @Override
    public IssueActionResult execute(Issue issue) {
        DefaultActionResult result = new DefaultActionResult();
        if (isApplicable(issue)){
            ServiceCallIssue scIssue = (ServiceCallIssue) issue;
            ServiceCall serviceCall = scIssue.getServiceCall();
            validateState(serviceCall);
            if (serviceCall.getParent().isPresent()) {
                serviceCall.requestTransition(serviceCall.getType().getRetryState().get());
                serviceCall.getParent().get().requestTransition(DefaultState.ONGOING);
            } else {
                serviceCall.requestTransition(serviceCall.getType().getRetryState().get());
            }
        }
        return result;
    }

    private void validateState(ServiceCall serviceCall) {
        if (serviceCall.getState().isOpen()) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_ACCEPTABLE)
                    .entity("The service call is not in the final state")
                    .build());
        }
    }

    @Override
    public boolean isApplicable(Issue issue) {
        if (issue != null && !issue.getStatus().isHistorical() && issue instanceof ServiceCallIssue){
            ServiceCallIssue scIssue = (ServiceCallIssue) issue;
            if (FIRMWARE_CAMPAIGN_NAME.equals(scIssue.getServiceCall().getType().getName()) ||
                    FIRMWARE_ITEM_NAME.equals(scIssue.getServiceCall().getType().getName())) {
                return !scIssue.getStatus().isHistorical() && scIssue.getServiceCall() != null
                        && scIssue.getServiceCall().getType().getRetryState().isPresent()
                        && !scIssue.getServiceCall().getState().isOpen();
            }
            return false;
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
