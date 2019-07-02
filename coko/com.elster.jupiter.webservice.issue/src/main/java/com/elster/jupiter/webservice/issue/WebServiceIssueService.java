/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservice.issue;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.entity.OpenIssue;

import aQute.bnd.annotation.ProviderType;

import java.util.Optional;

@ProviderType
public interface WebServiceIssueService {

    String COMPONENT_NAME = "WSI";
    String ISSUE_TYPE_NAME = "webservice";
    String WEB_SERVICE_ISSUE_REASON = "reason.network.issues";

    Optional<? extends WebServiceIssue> findIssue(long id);

    Optional<? extends WebServiceIssue> findAndLockWebServiceIssue(long id, long version);

    Optional<WebServiceOpenIssue> findOpenIssue(long id);

    Optional<WebServiceHistoricalIssue> findHistoricalIssue(long id);

    WebServiceOpenIssue createIssue(OpenIssue baseIssue, IssueEvent issueEvent);
    
    Finder<? extends WebServiceIssue> findAllWebServiceIssues(WebServiceIssueFilter filter);

}
