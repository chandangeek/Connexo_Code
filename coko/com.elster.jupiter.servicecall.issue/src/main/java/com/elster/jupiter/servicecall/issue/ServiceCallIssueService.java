/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.issue;

import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;

import aQute.bnd.annotation.ProviderType;

import java.util.Optional;

@ProviderType
public interface ServiceCallIssueService {

    String COMPONENT_NAME = "ISC";
    String ISSUE_TYPE_NAME = "servicecall";
    String SERVICE_CALL_ISSUE_PREFIX = "SCI";

    Optional<? extends IssueServiceCall> findIssue(long id);

    OpenIssueServiceCall createIssue(OpenIssue baseIssue, IssueEvent issueEvent);

    Optional<OpenIssueServiceCall> findOpenIssue(long id);

    Optional<HistoricalIssueServiceCall> findHistoricalIssue(long id);

    void createIssue(ServiceCall isuIssueType, DefaultState newState);

}