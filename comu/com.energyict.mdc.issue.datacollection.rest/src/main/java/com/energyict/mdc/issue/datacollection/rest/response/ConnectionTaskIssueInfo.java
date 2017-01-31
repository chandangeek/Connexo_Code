/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datacollection.rest.response;

import com.elster.jupiter.rest.util.IdWithNameInfo;

import java.time.Instant;
import java.util.List;

public class ConnectionTaskIssueInfo {
    public Long id;
    public Long version;

    public Instant latestAttempt;
    public Instant lastSuccessfulAttempt;

    public IdWithNameInfo latestResult;
    public IdWithNameInfo connectionMethod;
    public IdWithNameInfo latestStatus;

    public List<JournalEntryInfo> journals;

    public ConnectionTaskIssueInfo() {}
}
