/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.servicecall;

import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface OpenIssueServiceCall extends OpenIssue, ServiceCallIssue {
    
    HistoricalIssueServiceCall close(IssueStatus status);

}
