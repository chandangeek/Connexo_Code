/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datacollection.rest.response;


import com.elster.jupiter.rest.util.IdWithNameInfo;

import java.time.Instant;
import java.util.List;

public class CommunicationTaskIssueInfo {
    public Long id;
    public String name;

    public Instant latestAttempt;
    public Instant lastSuccessfulAttempt;

    public IdWithNameInfo latestResult;
    public IdWithNameInfo latestConnectionUsed;
    public IdWithNameInfo latestStatus;

    public List<JournalEntryInfo> journals;

    public CommunicationTaskIssueInfo() {}

}
