/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.issue;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.entity.OpenIssue;

import aQute.bnd.annotation.ProviderType;

import java.util.Optional;

@ProviderType
public interface IssueServiceCallService {

    String COMPONENT_NAME = "ISC";
    String ISSUE_TYPE_NAME = "servicecall";
    String SERVICE_CALL_ISSUE_PREFIX = "SEI";
    String DATA_VALIDATION_ISSUE_REASON = "reason.service.call.failed";

    Optional<? extends IssueServiceCall> findIssue(long id);

    Optional<? extends IssueServiceCall> findAndLockIssueDataValidationByIdAndVersion(long id, long version);

    OpenIssueServiceCall createIssue(OpenIssue baseIssue, IssueEvent issueEvent);

    Finder<? extends IssueServiceCall> findAllDataValidationIssues(ServiceCallIssueFilter filter);

}