package com.energyict.mdc.issue.datavalidation;

import java.util.Optional;

import com.elster.jupiter.issue.share.entity.Issue;

public interface IssueDataValidationService {

    public static final String COMPONENT_NAME = "IDV";
    public static final String ISSUE_TYPE_NAME = "datavalidation";
    public static final String DATA_VALIDATION_ISSUE_REASON = "reason.cant.estimate.data";

    Optional<DataValidationIssue> findIssue(long id);

    Optional<OpenDataValidationIssue> findOpenIssue(long id);

    Optional<HistoricalDataValidationIssue> findHistoricalIssue(long id);

    OpenDataValidationIssue createIssue(Issue baseIssue);
}
