/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservice.issue;

import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface WebServiceOpenIssue extends OpenIssue, WebServiceIssue {
    WebServiceHistoricalIssue close(IssueStatus status);
}
