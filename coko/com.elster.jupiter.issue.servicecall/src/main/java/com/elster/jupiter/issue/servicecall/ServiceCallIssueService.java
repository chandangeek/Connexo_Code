/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.servicecall;

import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.entity.OpenIssue;

import aQute.bnd.annotation.ProviderType;

import java.util.Optional;

@ProviderType
public interface ServiceCallIssueService {

    String COMPONENT_NAME = "ISC";
    String ISSUE_TYPE_NAME = "servicecall";
    String SERVICE_CALL_ISSUE_PREFIX = "SCI";

    Optional<? extends ServiceCallIssue> findIssue(long id);

    OpenServiceCallIssue createIssue(OpenIssue baseIssue, IssueEvent issueEvent);

    Optional<OpenServiceCallIssue> findOpenIssue(long id);

    Optional<HistoricalServiceCallIssue> findHistoricalIssue(long id);

}