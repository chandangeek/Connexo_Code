package com.energyict.mdc.issue.datacollection.rest.response;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.rest.IdWithNameInfo;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.issue.datacollection.rest.i18n.ComSessionSuccessIndicatorTranslationKeys;
import com.energyict.mdc.issue.datacollection.rest.i18n.ConnectionTaskSuccessIndicatorTranslationKeys;

import java.time.Instant;
import java.util.List;

public class IssueConnectionTaskInfo {
    public Long id;
    public Long version;

    public Instant latestAttempt;
    public Instant lastSuccessfulAttempt;

    public IdWithNameInfo latestResult;
    public IdWithNameInfo connectionMethod;
    public IdWithNameInfo latestStatus;

    public List<JournalEntryInfo> journals;

    public IssueConnectionTaskInfo() {}
}
