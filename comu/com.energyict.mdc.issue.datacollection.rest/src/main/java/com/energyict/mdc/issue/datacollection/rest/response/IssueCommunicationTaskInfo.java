package com.energyict.mdc.issue.datacollection.rest.response;

import com.energyict.mdc.common.rest.IdWithNameInfo;
import com.energyict.mdc.tasks.ComTask;

import java.time.Instant;
import java.util.List;

public class IssueCommunicationTaskInfo {
    public Long id;
    public String name;

    public Instant latestAttempt;
    public Instant lastSuccessfulAttempt;

    public IdWithNameInfo latestResult;
    public IdWithNameInfo latestConnectionUsed;
    public IdWithNameInfo latestStatus;

    public List<JournalEntryInfo> journals;

    public IssueCommunicationTaskInfo() {}

}
