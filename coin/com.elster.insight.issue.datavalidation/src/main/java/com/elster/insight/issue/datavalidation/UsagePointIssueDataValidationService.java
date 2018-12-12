/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.insight.issue.datavalidation;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.entity.OpenIssue;

import aQute.bnd.annotation.ProviderType;

import java.util.Optional;

@ProviderType
public interface UsagePointIssueDataValidationService {

    String COMPONENT_NAME = "IUV";
    String ISSUE_TYPE_NAME = "usagepointdatavalidation";
    String DATA_VALIDATION_ISSUE_PREFIX = "UVI";
    String DATA_VALIDATION_ISSUE_REASON = "reason.cant.estimate.usagepoint.data";

    Optional<? extends UsagePointIssueDataValidation> findIssue(long id);

    Optional<? extends UsagePointIssueDataValidation> findAndLockIssueDataValidationByIdAndVersion(long id, long version);

    Optional<UsagePointOpenIssueDataValidation> findOpenIssue(long id);

    Optional<UsagePointHistoricalIssueDataValidation> findHistoricalIssue(long id);

    UsagePointOpenIssueDataValidation createIssue(OpenIssue baseIssue, IssueEvent issueEvent);
    
    Finder<? extends UsagePointIssueDataValidation> findAllDataValidationIssues(UsagePointDataValidationIssueFilter filter);

}
