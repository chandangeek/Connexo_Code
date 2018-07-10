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
public interface IssueDataValidationService {

    String COMPONENT_NAME = "IUV";
    String ISSUE_TYPE_NAME = "usagepointdatavalidation";
    String DATA_VALIDATION_ISSUE_PREFIX = "UVI";
    String DATA_VALIDATION_ISSUE_REASON = "reason.cant.estimate.data";

    Optional<? extends IssueDataValidation> findIssue(long id);

    Optional<? extends IssueDataValidation> findAndLockIssueDataValidationByIdAndVersion(long id, long version);

    Optional<OpenIssueDataValidation> findOpenIssue(long id);

    Optional<HistoricalIssueDataValidation> findHistoricalIssue(long id);

    OpenIssueDataValidation createIssue(OpenIssue baseIssue, IssueEvent issueEvent);
    
    Finder<? extends IssueDataValidation> findAllDataValidationIssues(DataValidationIssueFilter filter);    

}
